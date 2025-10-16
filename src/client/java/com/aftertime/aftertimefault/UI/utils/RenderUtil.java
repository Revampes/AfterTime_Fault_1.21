package com.aftertime.aftertimefault.UI.utils;

import net.minecraft.client.gui.DrawContext;

public final class RenderUtil {
    private RenderUtil() {}

    /**
     * Draws a filled rectangle with the specified color.
     * Color is in ARGB format (0xAARRGGBB).
     */
    public static void fill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        // Use DrawContext's built-in fill method
        context.fill(x1, y1, x2, y2, color);
    }
}
