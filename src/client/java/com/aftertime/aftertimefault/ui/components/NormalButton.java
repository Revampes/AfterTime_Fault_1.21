package com.aftertime.aftertimefault.ui.components;

import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class NormalButton extends UIComponent {
    private final Text text;
    private final Runnable onClick;

    public NormalButton(int x, int y, int width, int height, Text text, Runnable onClick) {
        super(x, y, width, height);
        this.text = text;
        this.onClick = onClick;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        int color = isHovered(mouseX, mouseY) ? UITheme.BUTTON_HOVER_COLOR : UITheme.BUTTON_COLOR;
        context.fill(x, y, x + width, y + height, color);
        context.drawBorder(x, y, width, height, UITheme.ACCENT_COLOR);

        int textWidth = mc.textRenderer.getWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;

        context.drawText(mc.textRenderer, text, textX, textY, UITheme.TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && enabled && isHovered(mouseX, mouseY) && onClick != null) {
            onClick.run();
            return true;
        }
        return false;
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }
}