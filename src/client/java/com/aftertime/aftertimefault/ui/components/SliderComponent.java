package com.aftertime.aftertimefault.ui.components;

import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class SliderComponent extends UIComponent {
    private double value;
    private final double min, max;
    private final Text name;
    private boolean dragging = false;
    private final ValueChangeCallback onValueChange;

    public interface ValueChangeCallback {
        void onValueChange(double value);
    }

    public SliderComponent(int x, int y, int width, int height, Text name, double min, double max, double initialValue, ValueChangeCallback onValueChange) {
        super(x, y, width, height);
        this.name = name;
        this.min = min;
        this.max = max;
        this.value = (initialValue - min) / (max - min);
        this.onValueChange = onValueChange;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Draw slider name
        context.drawText(mc.textRenderer, name, x, y - 12, UITheme.TEXT_COLOR, false);

        // Draw background
        context.fill(x, y + height / 2 - 2, x + width, y + height / 2 + 2, UITheme.BUTTON_COLOR);

        // Draw slider
        int sliderX = x + (int)(value * width);
        context.fill(sliderX - 3, y, sliderX + 3, y + height, UITheme.ACCENT_COLOR);

        // Draw value text
        String valueText = "Value: " + (int)(min + value * (max - min));
        context.drawText(mc.textRenderer, valueText, x, y + height + 5, 0xFFAAAAAA, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && enabled && isHovered(mouseX, mouseY)) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    private void updateValue(double mouseX) {
        value = Math.max(0, Math.min(1, (mouseX - x) / width));
        if (onValueChange != null) {
            onValueChange.onValueChange(min + value * (max - min));
        }
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }
}