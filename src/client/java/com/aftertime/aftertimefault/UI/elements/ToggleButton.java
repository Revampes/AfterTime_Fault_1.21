package com.aftertime.aftertimefault.UI.elements;

import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.gui.DrawContext;

public class ToggleButton extends UIElement {
    private boolean toggled;
    private final String label;
    private final String description;
    private Runnable onToggle;

    public ToggleButton(int x, int y, int width, int height, String label, String description, boolean initialValue, Runnable onToggle) {
        super(x, y, width, height);
        this.label = label;
        this.description = description;
        this.toggled = initialValue;
        this.onToggle = onToggle;
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw button background
        int color;
        if (toggled) {
            color = hovered ? 0xFF20B2AA : 0xFF008080;
        } else {
            color = hovered ? 0xFF666666 : 0xFF444444;
        }

        RenderUtil.fill(context, x, y, x + width, y + height, color);

        // Draw label centered
        int textColor = toggled ? 0xFFFFFFFF : 0xFFCCCCCC;
        int th = TextRender.height(textRenderer);
        int textX = x + (width - TextRender.width(textRenderer, label)) / 2;
        int textY = y + (height - th) / 2;
        TextRender.draw(textRenderer, context, label, textX, textY, textColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY) || mouseButton != 0) return false;

        toggled = !toggled;
        if (onToggle != null) onToggle.run();
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    public boolean isToggled() { return toggled; }
    public void setToggled(boolean toggled) { this.toggled = toggled; }
    public String getDescription() { return description; }
    public void setOnToggle(Runnable onToggle) { this.onToggle = onToggle; }
}
