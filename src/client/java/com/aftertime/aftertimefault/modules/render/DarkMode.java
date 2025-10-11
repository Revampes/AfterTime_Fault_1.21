package com.aftertime.aftertimefault.modules.render;

import com.aftertime.aftertimefault.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class DarkMode implements HudRenderCallback {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final ModConfig config;

    public DarkMode(ModConfig config) {
        this.config = config;
    }

    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!config.enableDarkMode) return;
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();
        int color = (config.darkModeOpacity << 24); // ARGB: only alpha, black color
        context.fill(0, 0, width, height, color);
    }

    public static void register(ModConfig config) {
        HudRenderCallback.EVENT.register(new DarkMode(config));
    }
}