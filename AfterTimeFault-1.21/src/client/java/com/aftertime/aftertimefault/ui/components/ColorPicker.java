package com.aftertime.aftertimefault.ui.components;

import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class ColorPicker extends UIComponent {
    private int color;
    private final Text name;
    private boolean picking = false;
    private final ColorChangeCallback onColorChange;

    public interface ColorChangeCallback {
        void onColorChange(int color);
    }

    public ColorPicker(int x, int y, int width, int height, Text name, int initialColor, ColorChangeCallback onColorChange) {
        super(x, y, width, height);
        this.name = name;
        this.color = initialColor;
        this.onColorChange = onColorChange;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Draw color preview
        context.fill(x, y, x + width, y + height, color);
        context.drawBorder(x, y, width, height, isHovered(mouseX, mouseY) ? UITheme.ACCENT_COLOR : UITheme.BUTTON_COLOR);

        // Draw name
        context.drawText(mc.textRenderer, name, x + width + 5, y + (height - 8) / 2, UITheme.TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && enabled && isHovered(mouseX, mouseY)) {
            picking = true;
            // Here you would typically open a color picker dialog
            return true;
        }
        return false;
    }

    public void setColor(int color) {
        this.color = color;
        if (onColorChange != null) {
            onColorChange.onColorChange(color);
        }
    }

    public int getColor() {
        return color;
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }
}