package com.aftertime.aftertimefault.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

public class DungeonUtils {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    /**
     * Check if the player is currently in a dungeon.
     * This checks the scoreboard for dungeon indicators.
     */
    public static boolean isInDungeon() {
        ClientPlayerEntity player = MC.player;
        if (player == null) return false;

        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) return false;

        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;

        String displayName = objective.getDisplayName().getString();

        // Check for common dungeon scoreboard titles
        return displayName.contains("The Catacombs") ||
               displayName.contains("Dungeon") ||
               displayName.contains("Floor");
    }

    /**
     * Send a title to the player's screen.
     */
    public static void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        ClientPlayerEntity player = MC.player;
        if (player == null) return;

        MC.inGameHud.setTitle(Text.of(title));
        if (subtitle != null && !subtitle.isEmpty()) {
            MC.inGameHud.setSubtitle(Text.of(subtitle));
        }
        MC.inGameHud.setTitleTicks(fadeIn, stay, fadeOut);
    }

    /**
     * Send a chat message to the player.
     */
    public static void sendChatMessage(String message) {
        ClientPlayerEntity player = MC.player;
        if (player == null) return;

        player.sendMessage(Text.of(message), false);
    }
}
