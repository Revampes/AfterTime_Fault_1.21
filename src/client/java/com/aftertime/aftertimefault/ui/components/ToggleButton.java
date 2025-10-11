package com.aftertime.aftertimefault.ui.components;

import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class ToggleButton extends UIComponent {
    private boolean toggled;
    private final Text name;
    private final Text description;
    private final Runnable onToggle;

    public ToggleButton(int x, int y, int width, int height, Text name, Text description, boolean initialValue, Runnable onToggle) {
        super(x, y, width, height);
        this.name = name;
        this.description = description;
        this.toggled = initialValue;
        this.onToggle = onToggle;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Draw module name
        context.drawText(mc.textRenderer, name, x, y, UITheme.TEXT_COLOR, false);

        // Draw description (smaller and lighter) with proper spacing
        if (description != null) {
            context.drawText(mc.textRenderer, description, x, y + 12, 0xFFAAAAAA, false);
        }

        // Draw toggle box (small square on the right side)
        int boxSize = 16;
        int boxX = x + width - 80; // Position from right edge
        int boxY = y;

        // Box background
        int boxColor = isHovered(mouseX, mouseY) ? UITheme.BUTTON_HOVER_COLOR : UITheme.BUTTON_COLOR;
        context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, boxColor);
        context.drawBorder(boxX, boxY, boxSize, boxSize, 0xFF666666);

        // Checkmark when toggled
        if (toggled) {
            // Draw a checkmark or filled box
            context.fill(boxX + 3, boxY + 3, boxX + boxSize - 3, boxY + boxSize - 3, UITheme.ACCENT_COLOR);

            // Optional: Draw a checkmark symbol
            context.drawText(mc.textRenderer, "✓", boxX + 5, boxY + 4, 0xFF000000, false);
        }

        // Draw status text
        String statusText = toggled ? "Enabled" : "Disabled";
        int statusColor = toggled ? 0xFF00FF00 : 0xFFFF0000;
        context.drawText(mc.textRenderer, statusText, boxX + boxSize + 5, boxY + 4, statusColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && enabled && isHovered(mouseX, mouseY)) {
            // Check if click is on the toggle box area
            int boxX = x + width - 80;
            int boxY = y;
            int boxSize = 16;

            if (mouseX >= boxX && mouseX <= boxX + boxSize && mouseY >= boxY && mouseY <= boxY + boxSize) {
                toggled = !toggled;
                if (onToggle != null) onToggle.run();
                return true;
            }
        }
        return false;
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }

    // Override isHovered to only consider the toggle box area for clicking
    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        int boxX = x + width - 80;
        int boxY = y;
        int boxSize = 16;
        return mouseX >= boxX && mouseX <= boxX + boxSize && mouseY >= boxY && mouseY <= boxY + boxSize;
    }
}