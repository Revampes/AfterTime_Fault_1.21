package com.aftertime.aftertimefault.UI.elements;

import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropdownBox extends UIElement {
    private final List<String> options;
    private int selectedIndex;
    private final String title;
    private boolean expanded = false;
    private Runnable onChange;

    public DropdownBox(int x, int y, int width, int height, String title, String[] options, int initialIndex, Runnable onChange) {
        super(x, y, width, height);
        this.title = title;
        this.onChange = onChange;
        this.options = new ArrayList<>(Arrays.asList(options));
        this.selectedIndex = Math.max(0, Math.min(initialIndex, options.length - 1));
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw title
        int th = TextRender.height(textRenderer);
        TextRender.draw(textRenderer, context, title, x, y - (th + 4), 0xFFFFFFFF);

        // Draw main dropdown box
        int bgColor = hovered ? 0xFF555555 : 0xFF444444;
        RenderUtil.fill(context, x, y, x + width, y + height, bgColor);

        // Draw border
        RenderUtil.fill(context, x, y, x + width, y + 1, 0xFF000000);
        RenderUtil.fill(context, x, y + height - 1, x + width, y + height, 0xFF000000);
        RenderUtil.fill(context, x, y, x + 1, y + height, 0xFF000000);
        RenderUtil.fill(context, x + width - 1, y, x + width, y + height, 0xFF000000);

        // Draw selected option
        if (selectedIndex >= 0 && selectedIndex < options.size()) {
            String selectedText = options.get(selectedIndex);
            TextRender.draw(textRenderer, context, selectedText, x + 5, y + (height - th) / 2, 0xFFFFFFFF);
        }

        // Draw dropdown arrow
        String arrow = expanded ? "^" : "v";
        int aw = TextRender.width(textRenderer, arrow);
        TextRender.draw(textRenderer, context, arrow, x + width - 5 - aw, y + (height - th) / 2, 0xFFFFFFFF);
    }

    @Override
    public void drawOverlay(DrawContext context, int mouseX, int mouseY) {
        if (!visible || !expanded) return;

        int th = TextRender.height(textRenderer);
        // Draw expanded options as overlay so it sits above other elements
        int optionY = y + height;
        for (int i = 0; i < options.size(); i++) {
            boolean optionHovered = mouseX >= x && mouseX <= x + width &&
                    mouseY >= optionY && mouseY <= optionY + height;

            int optionBgColor = optionHovered ? 0xFF666666 : 0xFF555555;
            if (i == selectedIndex) {
                optionBgColor = optionHovered ? 0xFF446644 : 0xFF335533;
            }

            RenderUtil.fill(context, x, optionY, x + width, optionY + height, optionBgColor);
            RenderUtil.fill(context, x, optionY, x + width, optionY + 1, 0xFF000000);

            TextRender.draw(textRenderer, context, options.get(i), x + 5, optionY + (height - th) / 2, 0xFFFFFFFF);
            optionY += height;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!visible) return false;

        // Only toggle expansion when clicking the main box
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            expanded = !expanded;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int button) {
        if (!visible || !expanded) return false;

        // Handle clicks on options
        if (button == 0) {
            int optionY = y + height;

            for (int i = 0; i < options.size(); i++) {
                if (mouseX >= x && mouseX <= x + width &&
                        mouseY >= optionY && mouseY <= optionY + height) {
                    selectedIndex = i;
                    if (onChange != null) onChange.run();
                    expanded = false;
                    return true;
                }
                optionY += height;
            }
            // Clicked outside options/main: close and consume to prevent misclicks
            if (!(mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)) {
                expanded = false;
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {}

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (expanded && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            expanded = false;
        }
    }

    @Override
    public boolean hasOverlayOpen() { return expanded; }

    @Override
    public void closeOverlay() { expanded = false; }

    public int getSelectedIndex() { return selectedIndex; }

    public String getSelectedOption() {
        return (selectedIndex >= 0 && selectedIndex < options.size()) ? options.get(selectedIndex) : "";
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = Math.max(0, Math.min(index, options.size() - 1));
        if (onChange != null) onChange.run();
    }

    public void setOnChange(Runnable onChange) { this.onChange = onChange; }

    public boolean isExpanded() { return expanded; }

    @Override
    public int getTopPadding() {
        return TextRender.height(textRenderer) + 4;
    }
}
