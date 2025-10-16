package com.aftertime.aftertimefault.UI.categories;

import com.aftertime.aftertimefault.UI.elements.*;
import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModulePanel {
    private final String moduleName;
    private final String description;
    private int x, y, width;
    private boolean expanded = false;
    private final List<UIElement> elements = new ArrayList<>();
    private ToggleButton toggleButton;
    private final int baseHeight = 20;
    private final int verticalGap = 4;

    public ModulePanel(String moduleName, String description, int x, int y, int width, boolean initialToggleState) {
        this.moduleName = moduleName;
        this.description = description;
        this.x = x;
        this.y = y;
        this.width = width;

        toggleButton = new ToggleButton(x, y, width, baseHeight, moduleName, description, initialToggleState,
            () -> System.out.println(moduleName + " toggled: " + toggleButton.isToggled()));
    }

    public void draw(DrawContext context, int mouseX, int mouseY) {
        // Draw module background
        int bgColor = expanded ? 0x80444444 : 0x80333333;
        RenderUtil.fill(context, x, y, x + width, y + getHeight(), bgColor);

        // Draw toggle button
        toggleButton.draw(context, mouseX, mouseY);

        // Subsettings when expanded
        if (expanded) {
            for (UIElement element : elements) {
                element.draw(context, mouseX, mouseY);
            }
        }
    }

    public void drawOverlays(DrawContext context, int mouseX, int mouseY) {
        // Tooltip for the toggle button on hover
        if (toggleButton.isMouseOver(mouseX, mouseY)) {
            drawTooltip(context, mouseX, mouseY, description);
        }
        if (!expanded) return;
        for (UIElement element : elements) {
            element.drawOverlay(context, mouseX, mouseY);
        }
    }

    private void drawTooltip(DrawContext context, int mouseX, int mouseY, String text) {
        int th = TextRender.height(toggleButton.textRenderer);
        int tooltipWidth = TextRender.width(toggleButton.textRenderer, text) + 8;
        int screenW = toggleButton.client.getWindow().getScaledWidth();
        int screenH = toggleButton.client.getWindow().getScaledHeight();
        int tooltipX = Math.min(mouseX + 5, screenW - tooltipWidth - 5);
        int tooltipY = Math.min(mouseY + 5, screenH - th - 8 - 5);
        int tooltipH = th + 8;

        RenderUtil.fill(context, tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipH, 0xE0000000);
        TextRender.draw(toggleButton.textRenderer, context, text, tooltipX + 4, tooltipY + (tooltipH - th) / 2, 0xFFFFFFFF);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Check toggle button (left click)
        if (toggleButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        // Right click on toggle button to expand/collapse
        if (toggleButton.isMouseOver(mouseX, mouseY) && mouseButton == 1) {
            expanded = !expanded;
            return true;
        }

        // Check subsetting elements if expanded
        if (expanded) {
            for (UIElement element : elements) {
                if (element.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) {
        if (!expanded) return false;
        // Prefer elements with overlays open first
        for (UIElement element : elements) {
            if (element.hasOverlayOpen()) {
                if (element.mouseClickedOverlay(mouseX, mouseY, mouseButton)) return true;
            }
        }
        // Then allow elements to open overlays if click falls into their overlay activation areas
        for (UIElement element : elements) {
            if (element.mouseClickedOverlay(mouseX, mouseY, mouseButton)) return true;
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        toggleButton.mouseReleased(mouseX, mouseY, mouseButton);

        if (expanded) {
            for (UIElement element : elements) {
                element.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            for (UIElement element : elements) {
                element.keyTyped(typedChar, keyCode);
            }
        }
    }

    public int getHeight() {
        if (!expanded) return baseHeight;
        int sum = baseHeight + 3;
        for (int i = 0; i < elements.size(); i++) {
            UIElement el = elements.get(i);
            sum += el.getOuterHeight();
            if (i < elements.size() - 1) sum += verticalGap;
        }
        return sum + 3;
    }

    public void addCheckBox(String title, boolean initialValue, Runnable onChange) {
        CheckBox checkBox = new CheckBox(0, 0, width - 10, 16, title, initialValue, onChange);
        elements.add(checkBox);
        relayoutElements();
    }

    public void addSlider(String title, float min, float max, float initialValue, Runnable onChange) {
        Slider slider = new Slider(0, 0, width - 10, 16, title, min, max, initialValue);
        slider.setOnChange(v -> onChange.run());
        elements.add(slider);
        relayoutElements();
    }

    public void addSlider(String title, float min, float max, float initialValue, Consumer<Float> onChange) {
        Slider slider = new Slider(0, 0, width - 10, 16, title, min, max, initialValue);
        slider.setOnChange(onChange);
        elements.add(slider);
        relayoutElements();
    }

    public void addColorPicker(String title, java.awt.Color initialColor, Runnable onChange) {
        addColorPickerReturn(title, initialColor, onChange);
    }

    public ColorPicker addColorPickerReturn(String title, java.awt.Color initialColor, Runnable onChange) {
        ColorPicker colorPicker = new ColorPicker(0, 0, width - 10, 16, title, initialColor, onChange);
        elements.add(colorPicker);
        relayoutElements();
        return colorPicker;
    }

    public void addTextInput(String placeholder, int maxLength, Runnable onChange) {
        addTextInputReturn(placeholder, maxLength, onChange);
    }

    public TextInputField addTextInputReturn(String placeholder, int maxLength, Runnable onChange) {
        TextInputField textInput = new TextInputField(0, 0, width - 10, 16, placeholder, maxLength, onChange);
        elements.add(textInput);
        relayoutElements();
        return textInput;
    }

    public void addKeyBindInput(String title, String initialKey, Runnable onChange) {
        KeyBindInput keyBind = new KeyBindInput(0, 0, width - 10, 16, title, initialKey, onChange);
        elements.add(keyBind);
        relayoutElements();
    }

    public KeyBindInput addKeyBindInputReturn(String title, String initialKey, Runnable onChange) {
        KeyBindInput keyBind = new KeyBindInput(0, 0, width - 10, 16, title, initialKey, onChange);
        elements.add(keyBind);
        relayoutElements();
        return keyBind;
    }

    public void addKeyBindInput(String title, int initialKeyCode, Runnable onChange) {
        KeyBindInput keyBind = new KeyBindInput(0, 0, width - 10, 16, title, initialKeyCode, onChange);
        elements.add(keyBind);
        relayoutElements();
    }

    public void addNormalButton(String label, Runnable onClick) {
        NormalButton button = new NormalButton(0, 0, width - 10, 16, label, onClick);
        elements.add(button);
        relayoutElements();
    }

    public void addDropdown(String title, String[] options, int initialIndex, Runnable onChange) {
        addDropdownReturn(title, options, initialIndex, onChange);
    }

    public DropdownBox addDropdownReturn(String title, String[] options, int initialIndex, Runnable onChange) {
        DropdownBox dropdown = new DropdownBox(0, 0, width - 10, 16, title, options, initialIndex, onChange);
        elements.add(dropdown);
        relayoutElements();
        return dropdown;
    }

    private void relayoutElements() {
        int currentY = y + baseHeight + 3;
        for (UIElement element : elements) {
            currentY += element.getTopPadding();
            element.setPosition(x + 5, currentY);
            element.setSize(width - 10, element.getHeight());
            currentY += element.getHeight() + verticalGap;
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        toggleButton.setPosition(x, y);
        relayoutElements();
    }

    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        toggleButton.setPosition(x, y);
        toggleButton.setSize(width, baseHeight);
        relayoutElements();
    }

    public void closeOverlays() {
        for (UIElement element : elements) {
            if (element.hasOverlayOpen()) element.closeOverlay();
        }
    }

    public boolean hasAnyOverlayOpen() {
        for (UIElement element : elements) {
            if (element.hasOverlayOpen()) return true;
        }
        return false;
    }

    public boolean isExpanded() { return expanded; }
    public String getModuleName() { return moduleName; }
    public ToggleButton getToggleButton() { return toggleButton; }
    public boolean isEnabled() { return toggleButton.isToggled(); }
}
