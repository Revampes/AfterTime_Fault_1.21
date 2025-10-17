package com.aftertime.aftertimefault.modules.dungeon;

import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.utils.DungeonUtils;
import com.aftertime.aftertimefault.utils.RenderUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;

import java.util.HashMap;
import java.util.Map;

public class KeyHighlight {
    private static boolean bloodOpened = false;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Map<String, Boolean> keyTracking = new HashMap<>();

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(KeyHighlight::onRenderWorld);
        ClientEntityEvents.ENTITY_LOAD.register(KeyHighlight::onEntitySpawn);
    }

    private static void onEntitySpawn(Entity entity, net.minecraft.world.World world) {
        if (!ModConfig.enableKeyHighlighter || bloodOpened) return;

        if (entity instanceof ArmorStandEntity armorStand) {
            String name = armorStand.getDisplayName() != null
                ? armorStand.getDisplayName().getString()
                : "";
            String keyId = armorStand.getUuidAsString();

            if (name.contains("Wither Key") && !keyTracking.containsKey(keyId)) {
                keyTracking.put(keyId, true);
                DungeonUtils.sendTitle("§6Wither Key Dropped!", "", 10, 40, 10);
                DungeonUtils.sendChatMessage("§6Wither Key Dropped!");
            } else if (name.contains("Blood Key") && !keyTracking.containsKey(keyId)) {
                keyTracking.put(keyId, true);
                DungeonUtils.sendTitle("§cBlood Key Dropped!", "", 10, 40, 10);
                DungeonUtils.sendChatMessage("§cBlood Key Dropped!");
            }
        }
    }

    private static void onRenderWorld(WorldRenderContext context) {
        if (!ModConfig.enableKeyHighlighter || bloodOpened) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ArmorStandEntity armorStand) {
                String name = armorStand.getDisplayName() != null
                    ? armorStand.getDisplayName().getString()
                    : "";

                if (name.contains("Wither Key")) {
                    // Get colors from config
                    float r = ((ModConfig.witherKeyColor >> 16) & 0xFF) / 255.0f;
                    float g = ((ModConfig.witherKeyColor >> 8) & 0xFF) / 255.0f;
                    float b = (ModConfig.witherKeyColor & 0xFF) / 255.0f;

                    RenderUtils.drawEntityEspBox(
                            armorStand.getX(), armorStand.getY() + 0.5, armorStand.getZ(),
                            0.8, 1.5,  // Width and height
                            r, g, b,   // Color from config
                            1.0f,      // alpha
                            2.0f,      // line width
                            false       // depth test - false to see through walls
                    );
                } else if (name.contains("Blood Key")) {
                    // Get colors from config
                    float r = ((ModConfig.bloodKeyColor >> 16) & 0xFF) / 255.0f;
                    float g = ((ModConfig.bloodKeyColor >> 8) & 0xFF) / 255.0f;
                    float b = (ModConfig.bloodKeyColor & 0xFF) / 255.0f;

                    RenderUtils.drawEntityEspBox(
                            armorStand.getX(), armorStand.getY() + 0.5, armorStand.getZ(),
                            0.8, 1.5,  // Width and height
                            r, g, b,   // Color from config
                            1.0f,      // alpha
                            2.0f,      // line width
                            false       // depth test - false to see through walls
                    );
                }
            }
        }
    }

    public static void resetState() {
        bloodOpened = false;
        keyTracking.clear();
    }
}
