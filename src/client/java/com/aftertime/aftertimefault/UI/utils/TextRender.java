package com.aftertime.aftertimefault.UI.utils;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class TextRender {
    private TextRender() {}

    // Global font scaling factor
    public static float SCALE = 1.0f;

    // Toggle for using custom TTF renderer
    public static boolean USE_TTF = false;

    // Optional TrueType renderer
    private static final TTFFontRenderer TTF = TTFFontRenderer.tryCreate();

    public static int width(TextRenderer fr, String s) {
        if (s == null) return 0;
        if (USE_TTF && TTF != null) {
            return Math.round(TTF.getStringWidth(s) * SCALE);
        }
        if (fr == null) return 0;
        return Math.round(fr.getWidth(s) * SCALE);
    }

    public static int height(TextRenderer fr) {
        if (USE_TTF && TTF != null) {
            return Math.max(1, Math.round(TTF.getFontHeight() * SCALE));
        }
        if (fr == null) return 0;
        return Math.max(1, Math.round(fr.fontHeight * SCALE));
    }

    public static void draw(TextRenderer fr, DrawContext context, String s, int x, int y, int color) {
        if (s == null) return;
        if (USE_TTF && TTF != null) {
            TTF.drawString(context, s, x * SCALE, y * SCALE, color, false);
        } else if (fr != null) {
            context.drawText(fr, s, (int)(x * SCALE), (int)(y * SCALE), color, false);
        }
    }

    public static void drawWithShadow(TextRenderer fr, DrawContext context, String s, int x, int y, int color) {
        if (s == null) return;
        if (USE_TTF && TTF != null) {
            TTF.drawString(context, s, x * SCALE, y * SCALE, color, true);
        } else if (fr != null) {
            context.drawText(fr, s, (int)(x * SCALE), (int)(y * SCALE), color, true);
        }
    }
}
