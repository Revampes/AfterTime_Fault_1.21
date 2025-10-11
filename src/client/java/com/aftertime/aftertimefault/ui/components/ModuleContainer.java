package com.aftertime.aftertimefault.ui.components;

import com.aftertime.aftertimefault.ui.themes.UITheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ModuleContainer extends UIComponent {
    private static final int HEADER_HEIGHT = 30;
    private static final int SUB_ROW_HEIGHT = 20;
    private static final int SUB_ROW_SPACING = 35; // height + vertical spacing between rows
    private static final int SUB_TOP_PADDING = 20; // increased extra gap between header and first subsetting

    private final Text name;
    private final Text description;
    private boolean toggled; // represents the module's on/off state
    private boolean expanded = false;
    private final Runnable onToggle;
    private final List<UIComponent> subSettings = new ArrayList<>();
    private final SettingsButton settingsButton;

    public ModuleContainer(int x, int y, int width, Text name, Text description, boolean initialValue, Runnable onToggle) {
        super(x, y, width, HEADER_HEIGHT);
        this.name = name;
        this.description = description;
        this.toggled = initialValue; // keep initial toggle state
        this.onToggle = onToggle;
        this.settingsButton = new SettingsButton(x + 5, y + 7, 12, 12, () -> {
            expanded = !expanded;
            updateHeight();
        });

        // Ensure the module is visible and interactive by default (independent of toggle state)
        this.visible = true;
        this.setEnabled(true);
        this.settingsButton.setVisible(true);
        this.settingsButton.setEnabled(true);
    }

    public void addSubSetting(UIComponent setting) {
        subSettings.add(setting);
        setting.setVisible(true);
        setting.setEnabled(true);
        if (expanded) {
            updateHeight();
        }
    }

    private void updateHeight() {
        if (expanded) {
            this.height = HEADER_HEIGHT + SUB_TOP_PADDING + (subSettings.size() * SUB_ROW_SPACING);
        } else {
            this.height = HEADER_HEIGHT;
        }
    }

    private void layoutSubSettings() {
        if (!expanded) return;
        int subY = y + HEADER_HEIGHT + SUB_TOP_PADDING; // start further down to add extra gap
        for (UIComponent setting : subSettings) {
            setting.setPosition(x + 20, subY);
            setting.setSize(width - 30, SUB_ROW_HEIGHT);
            subY += SUB_ROW_SPACING;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) {
            return;
        }

        // Draw module background spanning entire container height
        int bgColor = isHovered(mouseX, mouseY) ? 0x80333333 : 0x80222222;
        context.fill(x, y, x + width, y + height, bgColor);

        // Draw settings button (triple bar)
        settingsButton.setPosition(x + 5, y + 7);
        settingsButton.render(context, mouseX, mouseY, delta);

        // Draw module name and description
        context.drawText(mc.textRenderer, name, x + 25, y + 5, UITheme.TEXT_COLOR, false);
        context.drawText(mc.textRenderer, description, x + 25, y + 15, 0xFFAAAAAA, false);

        // Draw toggle box (right side)
        int boxSize = 16;
        int boxX = x + width - 80;
        int boxY = y + 7;

        int boxColor = isHovered(mouseX, mouseY) ? UITheme.BUTTON_HOVER_COLOR : UITheme.BUTTON_COLOR;
        context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, boxColor);
        context.drawBorder(boxX, boxY, boxSize, boxSize, 0xFF666666);

        if (toggled) {
            context.fill(boxX + 3, boxY + 3, boxX + boxSize - 3, boxY + boxSize - 3, UITheme.ACCENT_COLOR);
            context.drawText(mc.textRenderer, "✓", boxX + 5, boxY + 4, 0xFF000000, false);
        }

        String statusText = toggled ? "Enabled" : "Disabled";
        int statusColor = toggled ? 0xFF00FF00 : 0xFFFF0000;
        context.drawText(mc.textRenderer, statusText, boxX + boxSize + 5, boxY + 4, statusColor, false);

        // Draw sub-settings if expanded
        if (expanded) {
            layoutSubSettings();

            // Row hover background spanning full module width, behind each subsetting
            for (UIComponent setting : subSettings) {
                // Many controls (e.g., slider) draw a label at y-12; include that area in hover
                int rowTop = setting.y - 14;      // cover label above control
                int rowBottom = setting.y + setting.height + 4; // a bit past control bottom
                boolean rowHover = mouseX >= x && mouseX <= x + width && mouseY >= rowTop && mouseY <= rowBottom;
                if (rowHover) {
                    context.fill(x, rowTop, x + width, rowBottom, UITheme.BUTTON_HOVER_COLOR);
                }
            }

            // Now render subsetting controls on top
            for (UIComponent setting : subSettings) {
                setting.render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) {
            return false;
        }

        // Ensure settings button has up-to-date position before using it
        settingsButton.setPosition(x + 5, y + 7);

        // Check settings button click
        if (settingsButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Check toggle box click
        int boxX = x + width - 80;
        int boxY = y + 7;
        int boxSize = 16;

        boolean inToggleBox = mouseX >= boxX && mouseX <= boxX + boxSize &&
                mouseY >= boxY && mouseY <= boxY + boxSize;

        if (inToggleBox) {
            this.toggled = !this.toggled;
            if (onToggle != null) onToggle.run();
            return true;
        }

        // Check sub-settings clicks
        if (expanded) {
            layoutSubSettings();
            for (UIComponent setting : subSettings) {
                if (setting.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (expanded) {
            layoutSubSettings();
            for (UIComponent setting : subSettings) {
                if (setting.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (expanded) {
            layoutSubSettings();
            for (UIComponent setting : subSettings) {
                if (setting.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (expanded) {
            for (UIComponent setting : subSettings) {
                if (setting.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (expanded) {
            for (UIComponent setting : subSettings) {
                if (setting.charTyped(chr, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isExpanded() {
        return expanded;
    }
}
