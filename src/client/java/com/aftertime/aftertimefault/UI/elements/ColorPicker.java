package com.aftertime.aftertimefault.UI.elements;

import com.aftertime.aftertimefault.UI.utils.RenderUtil;
import com.aftertime.aftertimefault.UI.utils.TextRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ColorPicker extends UIElement {
    private Color color;
    private final String title;
    private boolean picking = false;
    private Runnable onChange;

    private float hue = 0f;
    private float sat = 1f;
    private float val = 1f;
    private float alpha = 1f;

    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    public ColorPicker(int x, int y, int width, int height, String title, Color initialColor, Runnable onChange) {
        super(x, y, width, height);
        this.title = title;
        this.onChange = onChange;
        setColor(initialColor);
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = isMouseOver(mouseX, mouseY);

        // Draw title above the color box
        int th = TextRender.height(textRenderer);
        TextRender.draw(textRenderer, context, title, x, y - (th + 4), 0xFFFFFFFF);

        // Draw checkerboard background for alpha
        drawCheckerboard(context, x, y, width, height, 3, 0xFFAAAAAA, 0xFF666666);

        // Draw color preview
        int mcColor = getMCColor();
        RenderUtil.fill(context, x, y, x + width, y + height, mcColor);

        // Draw border
        int borderColor = hovered ? 0xFFFFFF00 : 0xFF000000;
        RenderUtil.fill(context, x, y, x + width, y + 1, borderColor);
        RenderUtil.fill(context, x, y + height - 1, x + width, y + height, borderColor);
        RenderUtil.fill(context, x, y, x + 1, y + height, borderColor);
        RenderUtil.fill(context, x + width - 1, y, x + width, y + height, borderColor);
    }

    @Override
    public void drawOverlay(DrawContext context, int mouseX, int mouseY) {
        if (!visible || !picking) return;

        // Layout overlay
        int svW = 120;
        int svH = 90;
        int hueW = 12;
        int alphaH = 12;
        int padding = 6;

        int boxW = padding + svW + padding + hueW + padding;
        int boxH = padding + svH + padding + alphaH + padding;

        int overlayX = x;
        int overlayY = y + height + 2;

        Screen currentScreen = client.currentScreen;
        int screenW = currentScreen != null ? currentScreen.width : 854;
        int screenH = currentScreen != null ? currentScreen.height : 480;

        if (overlayX + boxW > screenW - 4) overlayX = Math.max(4, screenW - boxW - 4);
        if (overlayY + boxH > screenH - 4) overlayY = Math.max(4, y - boxH - 4);

        // Draw overlay background
        RenderUtil.fill(context, overlayX, overlayY, overlayX + boxW, overlayY + boxH, 0xF0101010);
        RenderUtil.fill(context, overlayX, overlayY, overlayX + boxW, overlayY + 1, 0xFF000000);
        RenderUtil.fill(context, overlayX, overlayY + boxH - 1, overlayX + boxW, overlayY + boxH, 0xFF000000);
        RenderUtil.fill(context, overlayX, overlayY, overlayX + 1, overlayY + boxH, 0xFF000000);
        RenderUtil.fill(context, overlayX + boxW - 1, overlayY, overlayX + boxW, overlayY + boxH, 0xFF000000);

        // Regions
        int svX = overlayX + padding;
        int svY = overlayY + padding;
        int hueX = svX + svW + padding;
        int hueY = svY;
        int alphaX = svX;
        int alphaY = svY + svH + padding;
        int alphaW = svW;

        // Draw SV square - highly optimized with reduced sample points
        int vSteps = 18; // Reduced from 90 to 18 (5x reduction)
        int sSteps = 24; // Reduced from 120 to 24 (5x reduction)

        for (int vy = 0; vy < vSteps; vy++) {
            float v1 = 1.0f - (vy / (float) vSteps);
            float v2 = 1.0f - ((vy + 1) / (float) vSteps);
            int y1 = svY + (vy * svH / vSteps);
            int y2 = svY + ((vy + 1) * svH / vSteps);

            for (int sx = 0; sx < sSteps; sx++) {
                float s1 = sx / (float) sSteps;
                float s2 = (sx + 1) / (float) sSteps;
                int x1 = svX + (sx * svW / sSteps);
                int x2 = svX + ((sx + 1) * svW / sSteps);

                // Average the 4 corners for this cell
                float vAvg = (v1 + v2) / 2.0f;
                float sAvg = (s1 + s2) / 2.0f;

                int rgb = Color.HSBtoRGB(hue, sAvg, vAvg);
                int a = ((int) (alpha * 255) << 24);
                int color = a | (rgb & 0x00FFFFFF);

                RenderUtil.fill(context, x1, y1, x2, y2, color);
            }
        }

        // SV crosshair
        int cx = svX + Math.round(sat * (svW - 1));
        int cy = svY + Math.round((1 - val) * (svH - 1));
        drawCrosshair(context, cx, cy);

        // Draw hue bar - optimized with fewer steps
        int hueSteps = 18; // Reduced from 90 to 18
        for (int i = 0; i < hueSteps; i++) {
            float h = i / (float) hueSteps;
            int y1 = hueY + (i * svH / hueSteps);
            int y2 = hueY + ((i + 1) * svH / hueSteps);
            int rgb = Color.HSBtoRGB(h, 1f, 1f);
            RenderUtil.fill(context, hueX, y1, hueX + hueW, y2, 0xFF000000 | (rgb & 0x00FFFFFF));
        }

        // Hue selector
        int hy = hueY + Math.round(hue * (svH - 1));
        RenderUtil.fill(context, hueX - 1, hy - 1, hueX + hueW + 1, hy + 1, 0xFFFFFFFF);
        RenderUtil.fill(context, hueX, hy, hueX + hueW, hy + 1, 0xFF000000);

        // Alpha bar with checkerboard
        drawCheckerboard(context, alphaX, alphaY, alphaW, alphaH, 4, 0xFFBBBBBB, 0xFF888888);

        // Draw alpha gradient - optimized with chunks
        int alphaSteps = 24; // Reduced from 120 to 24
        int rgb = Color.HSBtoRGB(hue, sat, val);
        for (int i = 0; i < alphaSteps; i++) {
            float a = i / (float) alphaSteps;
            int x1 = alphaX + (i * alphaW / alphaSteps);
            int x2 = alphaX + ((i + 1) * alphaW / alphaSteps);
            int alphaVal = (int) (a * 255);
            int color = (alphaVal << 24) | (rgb & 0x00FFFFFF);
            RenderUtil.fill(context, x1, alphaY, x2, alphaY + alphaH, color);
        }

        // Alpha selector
        int ax = alphaX + Math.round(alpha * (alphaW - 1));
        RenderUtil.fill(context, ax - 1, alphaY - 1, ax + 1, alphaY + alphaH + 1, 0xFFFFFFFF);
        RenderUtil.fill(context, ax, alphaY, ax + 1, alphaY + alphaH, 0xFF000000);

        // Handle dragging updates
        boolean lmbDown = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (!lmbDown) {
            draggingSV = draggingHue = draggingAlpha = false;
        } else {
            if (draggingSV || (mouseX >= svX && mouseX <= svX + svW && mouseY >= svY && mouseY <= svY + svH)) {
                draggingSV = true;
                float ns = (mouseX - svX) / (float) (svW - 1);
                float nv = 1.0f - (mouseY - svY) / (float) (svH - 1);
                ns = clamp01(ns);
                nv = clamp01(nv);
                if (ns != sat || nv != val) {
                    sat = ns;
                    val = nv;
                    updateColorFromHSV();
                }
            } else if (draggingHue || (mouseX >= hueX && mouseX <= hueX + hueW && mouseY >= hueY && mouseY <= hueY + svH)) {
                draggingHue = true;
                float nh = (mouseY - hueY) / (float) (svH - 1);
                nh = clamp01(nh);
                if (nh != hue) {
                    hue = nh;
                    updateColorFromHSV();
                }
            } else if (draggingAlpha || (mouseX >= alphaX && mouseX <= alphaX + alphaW && mouseY >= alphaY && mouseY <= alphaY + alphaH)) {
                draggingAlpha = true;
                float na = (mouseX - alphaX) / (float) (alphaW - 1);
                na = clamp01(na);
                if (na != alpha) {
                    alpha = na;
                    updateColorFromHSV();
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!visible) return false;
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            picking = !picking;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int button) {
        if (!visible || !picking) return false;

        int svW = 120, svH = 90, hueW = 12, alphaH = 12, padding = 6;
        int boxW = padding + svW + padding + hueW + padding;
        int boxH = padding + svH + padding + alphaH + padding;
        int overlayX = x;
        int overlayY = y + height + 2;

        Screen currentScreen = client.currentScreen;
        int screenW = currentScreen != null ? currentScreen.width : 854;
        int screenH = currentScreen != null ? currentScreen.height : 480;

        if (overlayX + boxW > screenW - 4) overlayX = Math.max(4, screenW - boxW - 4);
        if (overlayY + boxH > screenH - 4) overlayY = Math.max(4, y - boxH - 4);

        int svX = overlayX + padding;
        int svY = overlayY + padding;
        int hueX = svX + svW + padding;
        int hueY = svY;
        int alphaX = svX;
        int alphaY = svY + svH + padding;
        int alphaW = svW;

        if (button == 0) {
            if (mouseX >= svX && mouseX <= svX + svW && mouseY >= svY && mouseY <= svY + svH) {
                draggingSV = true;
                float ns = (mouseX - svX) / (float) (svW - 1);
                float nv = 1.0f - (mouseY - svY) / (float) (svH - 1);
                sat = clamp01(ns);
                val = clamp01(nv);
                updateColorFromHSV();
                return true;
            }
            if (mouseX >= hueX && mouseX <= hueX + hueW && mouseY >= hueY && mouseY <= hueY + svH) {
                draggingHue = true;
                float nh = (mouseY - hueY) / (float) (svH - 1);
                hue = clamp01(nh);
                updateColorFromHSV();
                return true;
            }
            if (mouseX >= alphaX && mouseX <= alphaX + alphaW && mouseY >= alphaY && mouseY <= alphaY + alphaH) {
                draggingAlpha = true;
                float na = (mouseX - alphaX) / (float) (alphaW - 1);
                alpha = clamp01(na);
                updateColorFromHSV();
                return true;
            }
            picking = false;
            draggingSV = draggingHue = draggingAlpha = false;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        draggingSV = draggingHue = draggingAlpha = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (picking && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            picking = false;
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsv[0];
        this.sat = hsv[1];
        this.val = hsv[2];
        this.alpha = color.getAlpha() / 255.0f;
        if (onChange != null) onChange.run();
    }

    private void updateColorFromHSV() {
        int rgb = Color.HSBtoRGB(hue, sat, val);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        this.color = new Color(r, g, b, Math.round(alpha * 255));
        if (onChange != null) onChange.run();
    }

    public int getMCColor() {
        return (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    @Override
    public boolean hasOverlayOpen() {
        return picking;
    }

    @Override
    public void closeOverlay() {
        picking = false;
        draggingSV = draggingHue = draggingAlpha = false;
    }

    @Override
    public int getTopPadding() {
        return 12;
    }

    private static float clamp01(float f) {
        return f < 0 ? 0 : (f > 1 ? 1 : f);
    }

    private void drawCheckerboard(DrawContext context, int px, int py, int w, int h, int cell, int c1, int c2) {
        for (int yy = 0; yy < h; yy += cell) {
            for (int xx = 0; xx < w; xx += cell) {
                boolean alt = ((xx / cell) + (yy / cell)) % 2 == 0;
                int col = alt ? c1 : c2;
                RenderUtil.fill(context, px + xx, py + yy, px + Math.min(xx + cell, w), py + Math.min(yy + cell, h), col);
            }
        }
    }

    private void drawCrosshair(DrawContext context, int cx, int cy) {
        int r = 3;
        int color1 = 0xFFFFFFFF;
        int color2 = 0xFF000000;
        RenderUtil.fill(context, cx - r - 1, cy, cx + r + 2, cy + 1, color2);
        RenderUtil.fill(context, cx, cy - r - 1, cx + 1, cy + r + 2, color2);
        RenderUtil.fill(context, cx - r, cy, cx + r + 1, cy + 1, color1);
        RenderUtil.fill(context, cx, cy - r, cx + 1, cy + r + 1, color1);
    }
}
