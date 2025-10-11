package com.aftertime.aftertimefault.ui.components;

import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;

public class SettingsButton extends UIComponent {
    private final Runnable onClick;

    public SettingsButton(int x, int y, int width, int height, Runnable onClick) {
        super(x, y, width, height);
        this.onClick = onClick;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Draw button background
        int color = isHovered(mouseX, mouseY) ? UITheme.BUTTON_HOVER_COLOR : UITheme.BUTTON_COLOR;
        context.fill(x, y, x + width, y + height, color);
        context.drawBorder(x, y, width, height, 0xFF666666);

        // Draw triple bar icon (≡)
        String icon = "≡";
        int textWidth = mc.textRenderer.getWidth(icon);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;

        context.drawText(mc.textRenderer, icon, textX, textY, UITheme.TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("SettingsButton.mouseClicked at: " + mouseX + ", " + mouseY);
        System.out.println("SettingsButton bounds: " + x + ", " + y + " to " + (x + width) + ", " + (y + height));
        System.out.println("SettingsButton visible: " + visible + ", enabled: " + enabled);

        if (visible && enabled && isHovered(mouseX, mouseY)) {
            System.out.println("SettingsButton clicked!");
            if (onClick != null) onClick.run();
            return true;
        }
        return false;
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }
}