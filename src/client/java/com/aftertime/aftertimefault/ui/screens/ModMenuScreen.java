package com.aftertime.aftertimefault.ui.screens;

import com.aftertime.aftertimefault.Main;
import com.aftertime.aftertimefault.ui.categories.CategoryManager;
import com.aftertime.aftertimefault.ui.categories.ModCategory;
import com.aftertime.aftertimefault.ui.components.*;
import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ModMenuScreen extends Screen {
    private static final int HEADER_HEIGHT = 30;
    private static final int FOOTER_HEIGHT = 25;
    private static final int CATEGORY_BUTTON_WIDTH = 100;
    private static final int CATEGORY_BUTTON_HEIGHT = 20;
    private static final int PADDING = 10;

    private String currentCategory = "Render";
    private List<NormalButton> categoryButtons = new ArrayList<>();
    private boolean backgroundRendered = false;
    private ScrollableContainer scrollContainer;

    public ModMenuScreen() {
        super(Text.literal(Main.MOD_NAME + " Settings"));
    }

    @Override
    protected void init() {
        super.init();

        categoryButtons.clear();

        // Create category buttons on the left side
        String[] categories = CategoryManager.getCategoryNames();
        for (int i = 0; i < categories.length; i++) {
            final String categoryName = categories[i];
            int buttonY = HEADER_HEIGHT + PADDING + (i * (CATEGORY_BUTTON_HEIGHT + PADDING));

            NormalButton button = new NormalButton(
                    PADDING,
                    buttonY,
                    CATEGORY_BUTTON_WIDTH,
                    CATEGORY_BUTTON_HEIGHT,
                    Text.literal(categoryName),
                    () -> {
                        currentCategory = categoryName;
                        this.clearAndInit();
                    }
            );

            categoryButtons.add(button);
        }

        // Initialize scroll container
        int contentX = CATEGORY_BUTTON_WIDTH + (PADDING * 2);
        int contentY = HEADER_HEIGHT + PADDING;
        int contentWidth = width - contentX - PADDING;
        int contentHeight = height - HEADER_HEIGHT - FOOTER_HEIGHT - (PADDING * 2);

        System.out.println("Scroll container: " + contentX + ", " + contentY + " size: " + contentWidth + "x" + contentHeight);

        scrollContainer = new ScrollableContainer(contentX, contentY, contentWidth, contentHeight);

        // Initialize the current category
        ModCategory category = CategoryManager.getCategory(currentCategory);
        if (category != null) {
            category.initialize(Main.getConfig());

            // Add category components to scroll container with proper positioning
            // Start components after the category header (approx 40 pixels for header)
            var components = category.getComponents();
            int startY = 40; // Start after the category header
            for (var component : components) {
                // Position components to fill the container width
                component.setPosition(10, startY);
                component.setSize(contentWidth - 20, component.height); // Account for padding

                System.out.println("Positioning component at: " + component.x + ", " + component.y +
                        " size: " + component.width + "x" + component.height);

                scrollContainer.addComponent(component);
                startY += component.height + 15; // Space between components
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background only once per frame
        if (!backgroundRendered) {
            this.renderBackground(context, mouseX, mouseY, delta);
            backgroundRendered = true;
        }

        // Render header
        context.fill(0, 0, width, HEADER_HEIGHT, UITheme.HEADER_COLOR);
        context.drawCenteredTextWithShadow(
                textRenderer,
                Main.MOD_NAME,
                width / 2,
                (HEADER_HEIGHT - 8) / 2,
                UITheme.TEXT_COLOR
        );

        // Render footer
        context.fill(0, height - FOOTER_HEIGHT, width, height, UITheme.FOOTER_COLOR);
        String footerText = "Author: " + Main.AUTHOR + " Version: " + Main.VERSION;
        context.drawCenteredTextWithShadow(
                textRenderer,
                footerText,
                width / 2,
                height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 8) / 2,
                UITheme.TEXT_COLOR
        );

        // Render category buttons
        for (NormalButton button : categoryButtons) {
            button.render(context, mouseX, mouseY, delta);
        }

        // Render current category content background
        int contentX = CATEGORY_BUTTON_WIDTH + (PADDING * 2);
        int contentY = HEADER_HEIGHT + PADDING;
        int contentWidth = width - contentX - PADDING;
        int contentHeight = height - HEADER_HEIGHT - FOOTER_HEIGHT - (PADDING * 2);

        context.fill(contentX, contentY, contentX + contentWidth, contentY + contentHeight, 0x80444444);

        // Render category header (this is separate from the scroll container)
        ModCategory category = CategoryManager.getCategory(currentCategory);
        if (category != null) {
            context.drawText(
                    textRenderer,
                    category.getDisplayName(),
                    contentX + 15,
                    contentY + 10, // Position relative to content area, not scroll container
                    UITheme.TEXT_COLOR,
                    false
            );

            context.drawText(
                    textRenderer,
                    category.getDescription(),
                    contentX + 15,
                    contentY + 22, // Position relative to content area, not scroll container
                    0xFFAAAAAA,
                    false
            );
        }

        // Render scroll container (components start at Y=0 inside the container)
        scrollContainer.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, UITheme.BACKGROUND_COLOR);
    }

    // Forward all input events to the scroll container
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("=== ModMenuScreen mouseClicked ===");
        System.out.println("Screen click at: " + mouseX + ", " + mouseY);

        // Handle category button clicks first
        for (NormalButton categoryButton : categoryButtons) {
            if (categoryButton.mouseClicked(mouseX, mouseY, button)) {
                System.out.println("Category button handled click");
                return true;
            }
        }

        // Then handle scroll container clicks
        if (scrollContainer.mouseClicked(mouseX, mouseY, button)) {
            System.out.println("Scroll container handled click");
            return true;
        }

        System.out.println("No one handled the click");
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrollContainer.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollContainer.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (scrollContainer.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (scrollContainer.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (scrollContainer.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        backgroundRendered = false;
        super.close();
    }
}