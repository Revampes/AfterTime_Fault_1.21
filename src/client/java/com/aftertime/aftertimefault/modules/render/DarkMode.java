package com.aftertime.aftertimefault.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import com.aftertime.aftertimefault.config.ModConfig;

public class DarkMode implements HudRenderCallback {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public DarkMode() {
        // no-op: we read directly from ModConfig static fields
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.enableDarkMode) return;
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();
        int color = (ModConfig.darkModeOpacity << 24); // ARGB: only alpha, black color
        context.fill(0, 0, width, height, color);
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new DarkMode());
    }
}