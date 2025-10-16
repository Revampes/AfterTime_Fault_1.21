package com.aftertime.aftertimefault.UI.elements;

import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.gui.DrawContext;

public class NormalButton extends UIElement {
    private String label;
    private Runnable onClick;
    private boolean pressed = false;

    public NormalButton(int x, int y, int width, int height, String label, Runnable onClick) {
        super(x, y, width, height);
        this.label = label;
        this.onClick = onClick;
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw button background
        int bgColor;
        if (pressed) {
            bgColor = 0xFF333333;
        } else if (hovered) {
            bgColor = 0xFF555555;
        } else {
            bgColor = 0xFF444444;
        }

        RenderUtil.fill(context, x, y, x + width, y + height, bgColor);

        // Draw border
        int borderColor = hovered ? 0xFFFFFF00 : 0xFF000000;
        RenderUtil.fill(context, x, y, x + width, y + 1, borderColor);
        RenderUtil.fill(context, x, y + height - 1, x + width, y + height, borderColor);
        RenderUtil.fill(context, x, y, x + 1, y + height, borderColor);
        RenderUtil.fill(context, x + width - 1, y, x + width, y + height, borderColor);

        // Draw label
        int textColor = pressed ? 0xFF888888 : (hovered ? 0xFFFFFF00 : 0xFFFFFFFF);
        int th = TextRender.height(textRenderer);
        int textX = x + (width - TextRender.width(textRenderer, label)) / 2;
        int textY = y + (height - th) / 2;
        TextRender.draw(textRenderer, context, label, textX, textY, textColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY) || mouseButton != 0) return false;

        pressed = true;
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (pressed && isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            if (onClick != null) onClick.run();
        }
        pressed = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {}
}
