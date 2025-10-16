package com.aftertime.aftertimefault.UI.elements;

import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindInput extends UIElement {
    private int keyCode;
    private String keyName;
    private final String title;
    private boolean listening = false;
    private Runnable onChange;

    public KeyBindInput(int x, int y, int width, int height, String title, String initialKey, Runnable onChange) {
        super(x, y, width, height);
        this.title = title;
        this.onChange = onChange;
        setKeyByName(initialKey);
    }

    public KeyBindInput(int x, int y, int width, int height, String title, int initialKeyCode, Runnable onChange) {
        super(x, y, width, height);
        this.title = title;
        this.onChange = onChange;
        setKeyCode(initialKeyCode);
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw title ABOVE the keybind box
        int th = TextRender.height(textRenderer);
        TextRender.draw(textRenderer, context, title, x, y - (th + 2), 0xFFFFFFFF);

        // Draw button background
        int bgColor;
        if (listening) {
            bgColor = 0xFF00FF00;
        } else if (hovered) {
            bgColor = 0xFF555555;
        } else {
            bgColor = 0xFF444444;
        }

        RenderUtil.fill(context, x, y, x + width, y + height, bgColor);

        // Draw border
        int borderColor = listening ? 0xFFFFFF00 : 0xFF000000;
        RenderUtil.fill(context, x, y, x + width, y + 1, borderColor);
        RenderUtil.fill(context, x, y + height - 1, x + width, y + height, borderColor);
        RenderUtil.fill(context, x, y, x + 1, y + height, borderColor);
        RenderUtil.fill(context, x + width - 1, y, x + width, y + height, borderColor);

        // Draw key name
        String displayText = listening ? "Press a key..." : keyName;
        int textColor = listening ? 0xFF000000 : 0xFFFFFFFF;
        int textX = x + (width - TextRender.width(textRenderer, displayText)) / 2;
        int textY = y + (height - th) / 2;
        TextRender.draw(textRenderer, context, displayText, textX, textY, textColor);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;

        listening = !listening;
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!listening || !visible) return;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            setKeyCode(0);
        } else if (keyCode != GLFW.GLFW_KEY_ENTER) {
            setKeyCode(keyCode);
        }

        listening = false;
    }

    private void setKeyByName(String keyName) {
        this.keyName = keyName;

        // Handle empty or "None" keys
        if (keyName == null || keyName.isEmpty() || keyName.equalsIgnoreCase("None")) {
            this.keyCode = 0;
            this.keyName = "None";
            return;
        }

        // If it's already a translation key (starts with "key."), use it directly
        if (keyName.startsWith("key.")) {
            try {
                this.keyCode = InputUtil.fromTranslationKey(keyName).getCode();
            } catch (IllegalArgumentException e) {
                // Invalid translation key, default to None
                this.keyCode = 0;
                this.keyName = "None";
            }
            return;
        }

        // Otherwise, convert simple key name to translation key format
        String translationKey = "key.keyboard." + keyName.toLowerCase();
        try {
            InputUtil.Key key = InputUtil.fromTranslationKey(translationKey);
            this.keyCode = key.getCode();
            this.keyName = key.getLocalizedText().getString();
        } catch (IllegalArgumentException e) {
            // If that fails, try to find the key by name manually
            // This handles special cases like "RSHIFT" -> "key.keyboard.right.shift"
            String specialKey = convertSpecialKeyName(keyName);
            try {
                InputUtil.Key key = InputUtil.fromTranslationKey(specialKey);
                this.keyCode = key.getCode();
                this.keyName = key.getLocalizedText().getString();
            } catch (IllegalArgumentException e2) {
                // Still failed, default to None
                this.keyCode = 0;
                this.keyName = "None";
            }
        }
    }

    private String convertSpecialKeyName(String keyName) {
        // Convert common key name variations to proper translation keys
        String lower = keyName.toLowerCase();
        switch (lower) {
            case "rshift":
            case "rightshift":
                return "key.keyboard.right.shift";
            case "lshift":
            case "leftshift":
                return "key.keyboard.left.shift";
            case "rctrl":
            case "rightctrl":
                return "key.keyboard.right.control";
            case "lctrl":
            case "leftctrl":
                return "key.keyboard.left.control";
            case "ralt":
            case "rightalt":
                return "key.keyboard.right.alt";
            case "lalt":
            case "leftalt":
                return "key.keyboard.left.alt";
            case "space":
                return "key.keyboard.space";
            case "enter":
            case "return":
                return "key.keyboard.enter";
            case "esc":
            case "escape":
                return "key.keyboard.escape";
            default:
                return "key.keyboard." + lower;
        }
    }

    private void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
        this.keyName = keyCode == 0 ? "None" : InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
        if (onChange != null) onChange.run();
    }

    public int getKeyCode() { return keyCode; }
    public String getKeyName() { return keyName; }
    public boolean isListening() { return listening; }
    public void setListening(boolean listening) { this.listening = listening; }
    public void setOnChange(Runnable onChange) { this.onChange = onChange; }

    @Override
    public int getTopPadding() {
        return 12;
    }
}
