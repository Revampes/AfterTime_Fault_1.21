package com.aftertime.aftertimefault.UI.elements;

import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class TextInputField extends UIElement {
    private String text;
    private final String placeholder;
    private boolean focused = false;
    private int cursorPosition;
    private final int maxLength;
    private Runnable onChange;

    public TextInputField(int x, int y, int width, int height, String placeholder, int maxLength, Runnable onChange) {
        super(x, y, width, height);
        this.text = "";
        this.placeholder = placeholder;
        this.maxLength = maxLength;
        this.onChange = onChange;
        this.cursorPosition = 0;
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw background
        int bgColor = focused ? 0xFF555555 : (hovered ? 0xFF444444 : 0xFF333333);
        RenderUtil.fill(context, x, y, x + width, y + height, bgColor);

        // Draw border
        int borderColor = focused ? 0xFF00FF00 : 0xFF000000;
        RenderUtil.fill(context, x, y, x + width, y + 1, borderColor);
        RenderUtil.fill(context, x, y + height - 1, x + width, y + height, borderColor);
        RenderUtil.fill(context, x, y, x + 1, y + height, borderColor);
        RenderUtil.fill(context, x + width - 1, y, x + width, y + height, borderColor);

        // Draw text or placeholder
        String displayText = text.isEmpty() ? placeholder : text;
        int textColor = text.isEmpty() ? 0xFF888888 : 0xFFFFFFFF;

        // Calculate text position with cursor
        String textBeforeCursor = displayText.substring(0, Math.min(cursorPosition, displayText.length()));
        int textWidth = TextRender.width(textRenderer, textBeforeCursor);
        int textX = x + 4;
        int th = TextRender.height(textRenderer);
        int textY = y + (height - th) / 2;

        // Ensure text doesn't overflow
        String visibleText = displayText;
        while (TextRender.width(textRenderer, visibleText) > width - 8 && !visibleText.isEmpty()) {
            visibleText = visibleText.substring(1);
        }

        TextRender.draw(textRenderer, context, visibleText, textX, textY, textColor);

        // Draw cursor if focused
        if (focused && System.currentTimeMillis() % 1000 < 500) {
            int cursorX = textX + textWidth;
            RenderUtil.fill(context, cursorX, textY, cursorX + 1, textY + th, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;

        focused = true;
        cursorPosition = text.length();
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!focused || !visible) return;

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!text.isEmpty() && cursorPosition > 0) {
                text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
                if (onChange != null) onChange.run();
            }
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (cursorPosition < text.length()) {
                text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
                if (onChange != null) onChange.run();
            }
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (cursorPosition > 0) cursorPosition--;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (cursorPosition < text.length()) cursorPosition++;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            focused = false;
        } else if (isValidCharacter(typedChar) && text.length() < maxLength) {
            text = text.substring(0, cursorPosition) + typedChar + text.substring(cursorPosition);
            cursorPosition++;
            if (onChange != null) onChange.run();
        }
    }

    private boolean isValidCharacter(char c) {
        return c >= 32 && c <= 126;
    }

    public String getText() { return text; }
    public void setText(String text) {
        this.text = text;
        this.cursorPosition = Math.min(cursorPosition, text.length());
        if (onChange != null) onChange.run();
    }

    public boolean isFocused() { return focused; }
    public void setFocused(boolean focused) { this.focused = focused; }
    public void setOnChange(Runnable onChange) { this.onChange = onChange; }
}
