package com.aftertime.aftertimefault.ui.components;

import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public class DropdownBox extends UIComponent {
    private final List<String> options;
    private int selectedIndex = 0;
    private boolean expanded = false;
    private final Text name;
    private final SelectionChangeCallback onSelectionChange;

    public interface SelectionChangeCallback {
        void onSelectionChange(int index, String option);
    }

    public DropdownBox(int x, int y, int width, int height, Text name, String[] options, int initialIndex, SelectionChangeCallback onSelectionChange) {
        super(x, y, width, height);
        this.name = name;
        this.options = Arrays.asList(options);
        this.selectedIndex = initialIndex;
        this.onSelectionChange = onSelectionChange;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Draw main box
        int color = isHovered(mouseX, mouseY) ? UITheme.BUTTON_HOVER_COLOR : UITheme.BUTTON_COLOR;
        context.fill(x, y, x + width, y + height, color);

        String selectedText = options.get(selectedIndex);
        context.drawText(mc.textRenderer, name, x - mc.textRenderer.getWidth(name) - 5, y + (height - 8) / 2, UITheme.TEXT_COLOR, false);
        context.drawText(mc.textRenderer, selectedText, x + 5, y + (height - 8) / 2, UITheme.TEXT_COLOR, false);
        context.drawText(mc.textRenderer, "▼", x + width - 12, y + (height - 8) / 2, UITheme.TEXT_COLOR, false);

        // Draw expanded options
        if (expanded) {
            int optionHeight = height;
            for (int i = 0; i < options.size(); i++) {
                if (i == selectedIndex) continue;
                int optionY = y + height + (i * optionHeight);
                context.fill(x, optionY, x + width, optionY + optionHeight, color);
                context.drawText(mc.textRenderer, options.get(i), x + 5, optionY + (optionHeight - 8) / 2, UITheme.TEXT_COLOR, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;

        if (isHovered(mouseX, mouseY)) {
            expanded = !expanded;
            return true;
        }

        if (expanded) {
            int optionHeight = height;
            for (int i = 0; i < options.size(); i++) {
                if (i == selectedIndex) continue;
                int optionY = y + height + (i * optionHeight);
                if (mouseX >= x && mouseX <= x + width && mouseY >= optionY && mouseY <= optionY + optionHeight) {
                    selectedIndex = i;
                    expanded = false;
                    if (onSelectionChange != null) onSelectionChange.onSelectionChange(i, options.get(i));
                    return true;
                }
            }
            expanded = false;
        }

        return false;
    }

    public String getSelectedOption() { return options.get(selectedIndex); }
    public int getSelectedIndex() { return selectedIndex; }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }
}