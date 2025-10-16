package com.aftertime.aftertimefault.UI;

import com.aftertime.aftertimefault.UI.categories.CategoryPanel;
import com.aftertime.aftertimefault.UI.config.ModConfigIO;
import com.aftertime.aftertimefault.UI.config.UIConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModGUI extends Screen {
    private final List<CategoryPanel> categories = new ArrayList<>();
    private final int categoryWidth = 100;
    private final int padding = 5;

    public ModGUI() {
        super(Text.literal("ExampleGUI Config"));
    }

    @Override
    public void init() {
        categories.clear();

        // Build categories dynamically from annotated ModConfig
        Map<String, CategoryPanel> categoryMap = UIConfigManager.createUICategories();

        // Respect insertion order from LinkedHashMap returned by UIConfigManager
        int i = 0;
        for (Map.Entry<String, CategoryPanel> entry : categoryMap.entrySet()) {
            CategoryPanel panel = entry.getValue();
            int x = padding + i * (categoryWidth + padding);
            int y = padding;
            int preferred = panel.getPreferredHeight();
            int maxH = this.height - padding * 2;
            int height = Math.min(preferred, maxH);
            panel.setBounds(x, y, categoryWidth, height);
            categories.add(panel);
            i++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update category panel bounds
        for (int i = 0; i < categories.size(); i++) {
            CategoryPanel panel = categories.get(i);
            int x = padding + i * (categoryWidth + padding);
            int y = padding;
            int preferred = panel.getPreferredHeight();
            int maxH = this.height - padding * 2;
            int h = Math.min(preferred, maxH);
            panel.setBounds(x, y, categoryWidth, h);
        }

        // Draw base content - pass DrawContext directly
        for (CategoryPanel category : categories) {
            category.draw(context, mouseX, mouseY);
        }

        // Draw overlays on top - pass DrawContext directly
        for (CategoryPanel category : categories) {
            category.drawOverlays(context, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Route overlay clicks first
        for (CategoryPanel category : categories) {
            if (category.mouseClickedOverlay((int) mouseX, (int) mouseY, button)) {
                return true;
            }
        }

        for (CategoryPanel category : categories) {
            if (category.mouseClicked((int) mouseX, (int) mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel category : categories) {
            category.mouseReleased((int) mouseX, (int) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Only handle special keys here (backspace, delete, arrows, enter, escape)
        // Regular characters will be handled in charTyped()
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE ||
            keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT ||
            keyCode == GLFW.GLFW_KEY_ENTER) {
            for (CategoryPanel category : categories) {
                category.keyTyped((char) 0, keyCode);
            }
        }

        if (keyCode == 256) { // ESC
            if (this.client != null) {
                this.client.setScreen(null);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        // Handle regular character input here
        for (CategoryPanel category : categories) {
            category.keyTyped(chr, 0);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // If any overlay is open, avoid scrolling
        boolean anyOverlayOpen = false;
        for (CategoryPanel category : categories) {
            if (category.hasAnyOverlayOpen()) {
                anyOverlayOpen = true;
                break;
            }
        }
        if (anyOverlayOpen) return false;

        for (CategoryPanel category : categories) {
            if (category.isMouseOver((int) mouseX, (int) mouseY)) {
                category.handleMouseScroll(verticalAmount > 0 ? -1 : 1);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        // Persist ModConfig when leaving the UI
        ModConfigIO.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
