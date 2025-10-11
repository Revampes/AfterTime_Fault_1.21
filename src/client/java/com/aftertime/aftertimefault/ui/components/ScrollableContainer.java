package com.aftertime.aftertimefault.ui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

import java.util.ArrayList;
import java.util.List;

public class ScrollableContainer implements Element, Selectable {
    private static final int V_SPACING = 15;

    private int x, y, width, height;
    private int scrollY = 0;
    private int contentHeight = 0;
    private boolean scrolling = false;
    private boolean focused = false;
    private final List<UIComponent> components = new ArrayList<>();

    public ScrollableContainer(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void addComponent(UIComponent component) {
        components.add(component);
        updateContentHeight();
    }

    public void clearComponents() {
        components.clear();
        scrollY = 0;
        contentHeight = 0;
    }

    private void updateContentHeight() {
        contentHeight = 0;
        for (UIComponent component : components) {
            contentHeight = Math.max(contentHeight, component.y + component.height);
        }
        System.out.println("Content height updated: " + contentHeight);
    }

    // Stack components vertically in their add order, preserving initial top offset
    private void relayout() {
        if (components.isEmpty()) {
            contentHeight = 0;
            return;
        }

        int baseY = Integer.MAX_VALUE;
        for (UIComponent c : components) {
            baseY = Math.min(baseY, c.y);
        }

        int runningY = baseY;
        for (UIComponent c : components) {
            c.y = runningY;
            runningY += c.height + V_SPACING;
        }

        // Content height is distance from baseY to end of last component
        contentHeight = runningY - baseY;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Recompute layout (positions and content height) to reflect dynamic heights
        relayout();

        // Enable scissor test to clip content to the container bounds
        context.enableScissor(x, y, x + width, y + height);

        // Render all components with scroll and container offset applied
        for (UIComponent component : components) {
            if (isComponentVisible(component)) {
                // Store original position (container-local)
                int originalX = component.x;
                int originalY = component.y;

                // Apply container origin and scroll offset for rendering
                component.x = x + originalX;
                component.y = y + originalY - scrollY;

                // Render the component with screen-relative coordinates
                component.render(context, mouseX, mouseY, delta);

                // Restore original (container-local) position immediately
                component.x = originalX;
                component.y = originalY;
            }
        }

        context.disableScissor();

        // Render scrollbar if needed
        if (contentHeight > height) {
            renderScrollbar(context);
        }
    }

    private boolean isComponentVisible(UIComponent component) {
        int componentTop = component.y - scrollY;
        int componentBottom = componentTop + component.height;
        return componentBottom >= 0 && componentTop <= height;
    }

    private void renderScrollbar(DrawContext context) {
        int scrollbarWidth = 6;
        int scrollbarX = x + width - scrollbarWidth - 2;

        // Calculate scrollbar thumb size and position
        float visibleRatio = contentHeight <= 0 ? 1.0f : (float) height / contentHeight;
        int thumbHeight = Math.max(20, (int) (height * visibleRatio));
        int maxScroll = Math.max(0, contentHeight - height);
        float scrollProgress = maxScroll > 0 ? (float) scrollY / maxScroll : 0;
        int thumbY = y + (int) ((height - thumbHeight) * scrollProgress);

        // Draw scrollbar track
        context.fill(scrollbarX, y, scrollbarX + scrollbarWidth, y + height, 0x80000000);

        // Draw scrollbar thumb
        int thumbColor = scrolling ? 0xFF666666 : 0xFF444444;
        context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, thumbColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("ScrollableContainer mouseClicked at: " + mouseX + ", " + mouseY);

        // Ensure layout is up to date for hit testing
        relayout();

        if (!isMouseOver(mouseX, mouseY)) {
            System.out.println("Click outside container");
            return false;
        }

        // Check scrollbar click
        if (contentHeight > height) {
            int scrollbarX = x + width - 8;
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + 8) {
                scrolling = true;
                System.out.println("Scrollbar clicked");
                return true;
            }
        }

        System.out.println("Container bounds: " + x + ", " + y + " to " + (x + width) + ", " + (y + height));

        // Forward click to components (translate each to screen-space temporarily)
        for (UIComponent component : components) {
            if (isComponentVisible(component)) {
                int originalX = component.x;
                int originalY = component.y;

                // Transform to screen-space
                component.x = x + originalX;
                component.y = y + originalY - scrollY;

                boolean handled = component.mouseClicked(mouseX, mouseY, button);

                // Restore
                component.x = originalX;
                component.y = originalY;

                if (handled) {
                    System.out.println("Component handled click!");
                    return true;
                }
            }
        }

        System.out.println("No component handled the click");
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        System.out.println("ScrollableContainer mouseReleased");
        scrolling = false;

        // Ensure layout is up to date for hit testing
        relayout();

        if (!isMouseOver(mouseX, mouseY)) return false;

        for (UIComponent component : components) {
            if (isComponentVisible(component)) {
                int originalX = component.x;
                int originalY = component.y;

                // Transform to screen-space
                component.x = x + originalX;
                component.y = y + originalY - scrollY;

                boolean handled = component.mouseReleased(mouseX, mouseY, button);

                // Restore
                component.x = originalX;
                component.y = originalY;

                if (handled) return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrolling) {
            int maxScroll = Math.max(0, contentHeight - height);
            float scrollProgress = Math.max(0, Math.min(1, (float) (mouseY - y) / height));
            scrollY = (int) (scrollProgress * maxScroll);
            return true;
        }

        // Ensure layout is up to date for hit testing
        relayout();

        if (!isMouseOver(mouseX, mouseY)) return false;

        for (UIComponent component : components) {
            if (isComponentVisible(component)) {
                int originalX = component.x;
                int originalY = component.y;

                // Transform to screen-space
                component.x = x + originalX;
                component.y = y + originalY - scrollY;

                boolean handled = component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

                // Restore
                component.x = originalX;
                component.y = originalY;

                if (handled) return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        int scrollAmount = (int) (-verticalAmount * 20);
        scrollY = Math.max(0, Math.min(Math.max(0, contentHeight - height), scrollY + scrollAmount));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (UIComponent component : components) {
            if (component.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (UIComponent component : components) {
            if (component.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return false;
    }

    // Required Element interface methods
    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    // Required Selectable interface methods
    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {}

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}

