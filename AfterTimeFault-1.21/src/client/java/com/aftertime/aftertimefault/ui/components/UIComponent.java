package com.aftertime.aftertimefault.ui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class UIComponent {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    public int x;
    public int y;
    public int width;
    public int height;
    protected boolean visible = true;
    protected boolean enabled = true;

    public UIComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);
    public abstract boolean mouseReleased(double mouseX, double mouseY, int button);
    public abstract boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
    public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);
    public abstract boolean charTyped(char chr, int modifiers);

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    // Getters and setters
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setSize(int width, int height) { this.width = width; this.height = height; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}