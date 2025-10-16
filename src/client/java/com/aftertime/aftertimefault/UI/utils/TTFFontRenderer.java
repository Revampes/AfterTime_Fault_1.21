package com.aftertime.aftertimefault.UI.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.CRC32;

final class TTFFontRenderer {
    private static final String RESOURCE_PATH = "assets/examplegui/font/font.ttf";
    private static final float BASE_PIXEL_SIZE = 9f;
    private static final int CACHE_LIMIT = 256;
    private static final int PADDING = 2;

    private final MinecraftClient client = MinecraftClient.getInstance();

    private Font awtFont;

    private static final class CacheEntry {
        final int width;
        final int height;
        final int ascent;
        final NativeImageBackedTexture texture;
        final Identifier identifier;
        CacheEntry(int width, int height, int ascent, NativeImageBackedTexture texture, Identifier identifier) {
            this.width = width;
            this.height = height;
            this.ascent = ascent;
            this.texture = texture;
            this.identifier = identifier;
        }
    }

    private final Map<String, CacheEntry> cache = new LinkedHashMap<>(CACHE_LIMIT + 1, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            if (size() > CACHE_LIMIT) {
                eldest.getValue().texture.close();
                return true;
            }
            return false;
        }
    };

    static TTFFontRenderer tryCreate() {
        TTFFontRenderer r = new TTFFontRenderer();
        return r.init() ? r : null;
    }

    private boolean init() {
        InputStream is = null;
        try {
            is = TTFFontRenderer.class.getClassLoader().getResourceAsStream(RESOURCE_PATH);
            if (is == null) return false;

            Font base = Font.createFont(Font.TRUETYPE_FONT, is);
            awtFont = base.deriveFont(Font.PLAIN, BASE_PIXEL_SIZE);
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            if (is != null) try { is.close(); } catch (IOException ignored) {}
        }
    }

    int getStringWidth(String s) {
        if (s == null || s.isEmpty()) return 0;
        String clean = sanitize(s);
        CacheEntry ce = cache.get(clean);
        if (ce != null) return ce.width;

        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        applyHints(g);
        g.setFont(awtFont);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(clean) + PADDING;
        g.dispose();
        return Math.max(0, w);
    }

    int getFontHeight() {
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        applyHints(g);
        g.setFont(awtFont);
        FontMetrics fm = g.getFontMetrics();
        int h = fm.getAscent() + fm.getDescent() + 1 + PADDING / 2;
        g.dispose();
        return Math.max(1, h);
    }

    void drawString(DrawContext context, String s, float x, float y, int color, boolean shadow) {
        if (s == null || s.isEmpty()) return;
        String clean = sanitize(s);
        CacheEntry ce = cache.computeIfAbsent(clean, this::renderStringToTexture);
        if (ce == null) return;

        // Draw shadow if requested
    }

    private CacheEntry renderStringToTexture(String s) {
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        applyHints(g);
        g.setFont(awtFont);
        FontMetrics fm = g.getFontMetrics();
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int w = Math.max(1, fm.stringWidth(s)) + PADDING;
        int h = Math.max(1, ascent + descent + 1 + PADDING / 2);
        g.dispose();

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        applyHints(g2);
        g2.setFont(awtFont);
        g2.setColor(Color.WHITE);
        g2.drawString(s, 1, 1 + ascent);
        g2.dispose();

        try {
            NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, w, h, false);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = img.getRGB(x, y);
                    nativeImage.setColor(x, y, argb);
                }
            }

            String key = dynamicKey(s);
            Identifier id = Identifier.of("examplegui", "dynamic/" + key);
            Supplier<String> idSupplier = () -> id.toString();
            NativeImageBackedTexture texture = new NativeImageBackedTexture(idSupplier, nativeImage);

            client.getTextureManager().registerTexture(id, texture);
            
            nativeImage.close();
            
            return new CacheEntry(w, h, ascent, texture, id);
        } catch (Exception e) {
            return null;
        }
    }

    private static void applyHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private static String sanitize(String s) {
        int len = s.length();
        StringBuilder out = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '§') {
                i++;
                continue;
            }
            if (c < 0x20) continue;
            out.append(c);
        }
        return out.toString();
    }

    private static String dynamicKey(String s) {
        CRC32 crc = new CRC32();
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        crc.update(bytes, 0, bytes.length);
        long v = crc.getValue();
        return Long.toHexString(v) + "_" + s.length();
    }
}
