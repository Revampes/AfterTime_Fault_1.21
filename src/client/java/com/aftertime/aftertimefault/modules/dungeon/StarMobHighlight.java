package com.aftertime.aftertimefault.modules.dungeon;

import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.utils.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.regex.Pattern;

public class StarMobHighlight {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Pattern STARRED_PATTERN = Pattern.compile(".*\u00a76\u272f.*\u00a7c\u2764.*");

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(StarMobHighlight::onRenderWorld);
    }

    private static void onRenderWorld(WorldRenderContext context) {
        if (!ModConfig.enableStarMobHighlighter || mc.world == null) {
            return;
        }

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ArmorStandEntity armorStand) {
                String name = armorStand.getDisplayName() != null
                    ? armorStand.getDisplayName().getString()
                    : "";

                if (isStarredMob(name)) {
                    Entity mob = getMobEntity(armorStand);
                    if (mob != null) {
                        // Get colors from config
                        float r = ((ModConfig.starMobColor >> 16) & 0xFF) / 255.0f;
                        float g = ((ModConfig.starMobColor >> 8) & 0xFF) / 255.0f;
                        float b = (ModConfig.starMobColor & 0xFF) / 255.0f;

                        RenderUtils.drawEntityBox(
                                mob,
                                r, g, b,
                                1.0f, // alpha
                                2.0f, // line width
                                false, // depth test
                                0.0f  // partialTicks
                        );
                    }
                }
            } else if (entity instanceof PlayerEntity player) {
                String displayName = player.getDisplayName() != null
                    ? player.getDisplayName().getString()
                    : "";

                if ("Shadow Assassin".equals(displayName)) {
                    // Get colors from config
                    float r = ((ModConfig.shadowAssassinColor >> 16) & 0xFF) / 255.0f;
                    float g = ((ModConfig.shadowAssassinColor >> 8) & 0xFF) / 255.0f;
                    float b = (ModConfig.shadowAssassinColor & 0xFF) / 255.0f;

                    RenderUtils.drawEntityBox(
                            player,
                            r, g, b,
                            1.0f, // alpha
                            2.0f, // line width
                            false, // depth test
                            0.0f  // partialTicks
                    );
                }
            }
        }
    }

    private static boolean isStarredMob(String name) {
        boolean matches = STARRED_PATTERN.matcher(name).matches();
        // Also check for simpler patterns in case formatting is different
        if (!matches && name.contains("✯")) {
            matches = true;
        }
        return matches;
    }

    private static Entity getMobEntity(ArmorStandEntity armorStand) {
        // Find the nearest non-armorstand entity below the armor stand
        Box searchBox = armorStand.getBoundingBox().expand(1.0, 2.0, 1.0);

        for (Entity entity : mc.world.getOtherEntities(armorStand, searchBox)) {
            if (!(entity instanceof ArmorStandEntity) &&
                !(entity instanceof WitherEntity && entity.isInvisible()) &&
                entity != mc.player) {
                return entity;
            }
        }
        return null;
    }
}
