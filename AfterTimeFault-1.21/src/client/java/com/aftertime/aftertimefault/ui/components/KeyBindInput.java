package com.aftertime.aftertimefault.ui.components;

import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class KeyBindInput extends UIComponent {
    private int keyCode = -1;
    private final Text name;
    private boolean listening = false;
    private final KeyBindChangeCallback onKeyChange;

    public interface KeyBindChangeCallback {
        void onKeyChange(int keyCode);
    }

    public KeyBindInput(int x, int y, int width, int height, Text name, int initialKey, KeyBindChangeCallback onKeyChange) {
        super(x, y, width, height);
        this.name = name;
        this.keyCode = initialKey;
        this.onKeyChange = onKeyChange;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        int color = listening ? UITheme.ACCENT_COLOR :
                isHovered(mouseX, mouseY) ? UITheme.BUTTON_HOVER_COLOR : UITheme.BUTTON_COLOR;

        // FIXED: Changed x + height to y + height
        context.fill(x, y, x + width, y + height, color);

        String keyText = listening ? "Press a key..." :
                keyCode == -1 ? "None" :
                        "Key: " + keyCode;

        context.drawText(mc.textRenderer, name, x + 5, y + (height - 8) / 2, UITheme.TEXT_COLOR, false);
        context.drawText(mc.textRenderer, keyText, x + width - mc.textRenderer.getWidth(keyText) - 5, y + (height - 8) / 2, UITheme.TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && enabled && isHovered(mouseX, mouseY)) {
            listening = true;
            return true;
        }
        listening = false;
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            this.keyCode = keyCode;
            listening = false;
            if (onKeyChange != null) onKeyChange.onKeyChange(keyCode);
            return true;
        }
        return false;
    }

    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }
}