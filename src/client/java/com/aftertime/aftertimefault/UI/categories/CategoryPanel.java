package com.aftertime.aftertimefault.UI.categories;

import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {
    private final String categoryName;
    private int x, y, width, height;
    private final List<ModulePanel> modules = new ArrayList<>();
    private int scrollOffset = 0;
    private final MinecraftClient client = MinecraftClient.getInstance();

    public CategoryPanel(String categoryName, int x, int y, int width, int height) {
        this.categoryName = categoryName;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(DrawContext context, int mouseX, int mouseY) {
        // Draw category background (opaque grey)
        RenderUtil.fill(context, x, y, x + width, y + height, 0xFF444444);

        // Draw category title bar background for better visibility
        RenderUtil.fill(context, x, y, x + width, y + 20, 0xFF555555);

        // Draw bottom border for title bar
        RenderUtil.fill(context, x, y + 20, x + width, y + 21, 0xFF000000);

        // Draw category title (centered and more visible)
        int titleW = TextRender.width(client.textRenderer, categoryName);
        int titleH = TextRender.height(client.textRenderer);
        int titleX = x + (width - titleW) / 2;
        int titleY = y + (20 - titleH) / 2;
        TextRender.draw(client.textRenderer, context, categoryName, titleX, titleY, 0xFFFFFFFF);

        // Draw modules
        int moduleY = y + 25 - scrollOffset;
        for (ModulePanel module : modules) {
            if (moduleY + module.getHeight() > y && moduleY < y + height) {
                module.setBounds(x + 5, moduleY, width - 10);
                module.draw(context, mouseX, mouseY);
            }
            moduleY += module.getHeight();
        }

        // Draw scroll bar if needed
        drawScrollBar(context, mouseX, mouseY);
    }

    public void drawOverlays(DrawContext context, int mouseX, int mouseY) {
        int moduleY = y + 25 - scrollOffset;
        for (ModulePanel module : modules) {
            if (moduleY + module.getHeight() > y && moduleY < y + height) {
                module.drawOverlays(context, mouseX, mouseY);
            }
            moduleY += module.getHeight();
        }
    }

    private void drawScrollBar(DrawContext context, int mouseX, int mouseY) {
        int totalHeight = getTotalHeight();
        if (totalHeight <= height - 25) return;

        float visibleRatio = (float)(height - 25) / totalHeight;
        int scrollbarHeight = (int)((height - 25) * visibleRatio);
        int scrollbarY = y + 25 + (int)(scrollOffset * ((height - 25 - scrollbarHeight) / (float)(totalHeight - (height - 25))));

        RenderUtil.fill(context, x + width - 5, scrollbarY, x + width - 2, scrollbarY + scrollbarHeight, 0xFF888888);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        int moduleY = y + 25 - scrollOffset;
        for (ModulePanel module : modules) {
            if (moduleY + module.getHeight() > y && moduleY < y + height) {
                if (module.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
            moduleY += module.getHeight();
        }
        return false;
    }

    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) {
        int moduleY = y + 25 - scrollOffset;
        boolean handled = false;
        for (ModulePanel module : modules) {
            if (moduleY + module.getHeight() > y && moduleY < y + height) {
                if (module.mouseClickedOverlay(mouseX, mouseY, mouseButton)) {
                    handled = true;
                    break;
                }
            }
            moduleY += module.getHeight();
        }
        return handled;
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (ModulePanel module : modules) {
            module.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        for (ModulePanel module : modules) {
            module.keyTyped(typedChar, keyCode);
        }
    }

    public void handleMouseScroll(int delta) {
        scrollOffset += delta * 10;
        scrollOffset = Math.max(0, Math.min(scrollOffset, getTotalHeight() - (height - 25)));
    }

    private int getTotalHeight() {
        int total = 0;
        for (ModulePanel module : modules) {
            total += module.getHeight();
        }
        return total;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void addModule(ModulePanel module) {
        modules.add(module);
    }

    public int getWidth() { return width; }
    public int getX() { return x; }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean hasAnyOverlayOpen() {
        for (ModulePanel module : modules) {
            if (module.hasAnyOverlayOpen()) return true;
        }
        return false;
    }

    public int getPreferredHeight() {
        return 25 + getTotalHeight();
    }
}
