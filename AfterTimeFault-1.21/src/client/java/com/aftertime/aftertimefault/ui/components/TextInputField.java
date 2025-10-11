package com.aftertime.aftertimefault.ui.components;

import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class TextInputField extends UIComponent {
    private String text = "";
    private final Text placeholder;
    private boolean focused = false;
    private final TextChangeCallback onTextChange;

    public interface TextChangeCallback {
        void onTextChange(String text);
    }

    public TextInputField(int x, int y, int width, int height, Text placeholder, TextChangeCallback onTextChange) {
        super(x, y, width, height);
        this.placeholder = placeholder;
        this.onTextChange = onTextChange;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        int borderColor = focused ? UITheme.ACCENT_COLOR : UITheme.BUTTON_COLOR;
        context.fill(x, y, x + width, y + height, 0xFF000000);
        context.drawBorder(x, y, width, height, borderColor);

        String displayText = text.isEmpty() ? placeholder.getString() : text;
        int textColor = text.isEmpty() ? 0xFF888888 : UITheme.TEXT_COLOR;

        context.drawText(mc.textRenderer, displayText, x + 4, y + (height - 8) / 2, textColor, false);

        // Draw cursor if focused
        if (focused && System.currentTimeMillis() % 1000 < 500) {
            int cursorX = x + 4 + mc.textRenderer.getWidth(text);
            context.fill(cursorX, y + 2, cursorX + 1, y + height - 2, UITheme.TEXT_COLOR);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && enabled && isHovered(mouseX, mouseY)) {
            focused = true;
            return true;
        }
        focused = false;
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return false;

        // Handle key input (simplified - you'd want more robust text input handling)
        if (keyCode == 259) { // Backspace
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
                if (onTextChange != null) onTextChange.onTextChange(text);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (focused && chr >= 32 && chr != 127) { // Printable characters only
            text += chr;
            if (onTextChange != null) onTextChange.onTextChange(text);
            return true;
        }
        return false;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
}