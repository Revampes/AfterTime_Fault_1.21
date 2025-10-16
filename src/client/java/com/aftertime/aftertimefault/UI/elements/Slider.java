package com.aftertime.aftertimefault.UI.elements;

import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.gui.DrawContext;

import java.util.Locale;
import java.util.function.Consumer;

public class Slider extends UIElement {
    private float value;
    private final float minValue, maxValue;
    private final String title;
    private boolean dragging = false;
    private Consumer<Float> onChange;

    public Slider(int x, int y, int width, int height, String label, float minValue, float maxValue, float initialValue) {
        super(x, y, width, height);
        this.title = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = initialValue;
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY) {
        if (!visible) return;

        if (dragging) {
            updateValue(mouseX);
        }

        hovered = isMouseOver(mouseX, mouseY);

        // Title above the bar
        int th = TextRender.height(textRenderer);
        int topPad = getTopPadding();
        TextRender.draw(textRenderer, context, title, x, y - topPad + 1, 0xFFFFFFFF);

        // Draw bar background
        int bgColor = 0xFF444444;
        RenderUtil.fill(context, x, y, x + width, y + height, bgColor);

        // Draw filled portion
        float range = (maxValue - minValue);
        float percentage = (range == 0f) ? 1f : (value - minValue) / range;
        percentage = Math.max(0f, Math.min(1f, percentage));
        int fillWidth = Math.max(0, Math.min(width, (int) (width * percentage)));
        int fillColor = 0xFF66AAFF;
        RenderUtil.fill(context, x, y, x + fillWidth, y + height, fillColor);

        // Draw numeric value centered inside the bar
        String valueText = formatValue(value);
        int textW = TextRender.width(textRenderer, valueText);
        int textX = x + (width - textW) / 2;
        int textY = y + (height - th) / 2;
        int valueColor = 0xFFFFFFFF;
        TextRender.draw(textRenderer, context, valueText, textX, textY, valueColor);

        // Draw handle
        int handleX = x + fillWidth - 2;
        handleX = Math.max(x - 2, Math.min(x + width - 2, handleX));
        RenderUtil.fill(context, handleX, y - 2, handleX + 4, y + height + 2, 0xFFFFFFFF);
    }

    private String formatValue(float v) {
        boolean integerMode = isInteger(minValue) && isInteger(maxValue);
        if (integerMode) {
            return String.valueOf(Math.round(v));
        }
        return String.format(Locale.US, "%.2f", v);
    }

    private boolean isInteger(float f) {
        return Math.abs(f - Math.round(f)) < 1e-6f;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;

        if (mouseButton == 0) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (dragging) {
            updateValue(mouseX);
        }
        dragging = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    public void updateValue(int mouseX) {
        float prev = this.value;
        float newValue = minValue + ((mouseX - x) / (float)width) * (maxValue - minValue);
        value = Math.max(minValue, Math.min(maxValue, newValue));
        if (onChange != null && Math.abs(prev - value) > 0.001f) {
            onChange.accept(value);
        }
    }

    public float getValue() { return value; }
    public void setValue(float value) { this.value = value; }
    public void setOnChange(Consumer<Float> onChange) { this.onChange = onChange; }

    @Override
    public int getTopPadding() {
        return TextRender.height(textRenderer) + 2;
    }
}
