package com.aftertime.aftertimefault.UI.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class UIElement {
    public MinecraftClient client;
    public TextRenderer textRenderer;
    protected int x, y, width, height;
    protected boolean visible = true;
    protected boolean hovered = false;

    public UIElement(int x, int y, int width, int height) {
        this.client = MinecraftClient.getInstance();
        this.textRenderer = client.textRenderer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void draw(DrawContext context, int mouseX, int mouseY);
    public abstract boolean mouseClicked(int mouseX, int mouseY, int mouseButton);
    public abstract void mouseReleased(int mouseX, int mouseY, int mouseButton);
    public abstract void keyTyped(char typedChar, int keyCode);

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void drawOverlay(DrawContext context, int mouseX, int mouseY) { /* no-op */ }
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) { return false; }
    public boolean hasOverlayOpen() { return false; }
    public void closeOverlay() { /* no-op */ }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setSize(int width, int height) { this.width = width; this.height = height; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isVisible() { return visible; }

    public int getHeight() { return this.height; }
    public int getTopPadding() { return 0; }
    public int getOuterHeight() { return getTopPadding() + getHeight(); }
}
