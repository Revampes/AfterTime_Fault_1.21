package com.aftertime.aftertimefault.modules.render;

import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.events.HudRenderEventBus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class DarkMode {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.enableDarkMode) return;
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();
        int color = (ModConfig.darkModeOpacity << 24); // ARGB: only alpha, black color
        context.fill(0, 0, width, height, color);
    }

    public static void register() {
        DarkMode instance = new DarkMode();
        HudRenderEventBus.register(instance::onHudRender);
    }
}