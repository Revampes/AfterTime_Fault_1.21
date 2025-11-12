package com.aftertime.aftertimefault.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

public class PlayerUtil {
    public static void useItem() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.interactionManager != null) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}

