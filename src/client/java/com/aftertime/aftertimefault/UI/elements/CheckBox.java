package com.aftertime.aftertimefault.UI.elements;

import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.gui.DrawContext;

public class CheckBox extends UIElement {
    private boolean checked;
    private final String label;
    private final Runnable onChange;

    public CheckBox(int x, int y, int width, int height, String label, boolean initialValue, Runnable onChange) {
        super(x, y, width, height);
        this.label = label;
        this.checked = initialValue;
        this.onChange = onChange;
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw checkbox background
        int bgColor = hovered ? 0xFF555555 : 0xFF444444;
        RenderUtil.fill(context, x, y, x + height, y + height, bgColor);

        // Draw checkmark if checked
        if (checked) {
            RenderUtil.fill(context, x + 3, y + 3, x + height - 3, y + height - 3, 0xFF00FF00);
        }

        // Draw border
        RenderUtil.fill(context, x, y, x + height, y + 1, 0xFF000000);
        RenderUtil.fill(context, x, y + height - 1, x + height, y + height, 0xFF000000);
        RenderUtil.fill(context, x, y, x + 1, y + height, 0xFF000000);
        RenderUtil.fill(context, x + height - 1, y, x + height, y + height, 0xFF000000);

        // Draw label
        int textColor = hovered ? 0xFFFFFFAA : 0xFFFFFFFF;
        int th = TextRender.height(textRenderer);
        int textY = y + (height - th) / 2;
        TextRender.draw(textRenderer, context, label, x + height + 5, textY, textColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY) || mouseButton != 0) return false;

        checked = !checked;
        if (onChange != null) onChange.run();
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}
