package com.aftertime.aftertimefault.modules.dungeon;

import com.aftertime.aftertimefault.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class InvincibleTimer implements HudRenderCallback {
    private int bonzoTime = 0;
    private int spiritTime = 0;
    private int phoenixTime = 0;

    private String procText = " ";
    private long procTextEndTime = 0;
    private final MinecraftClient client;

    public InvincibleTimer() {
        this.client = MinecraftClient.getInstance();
    }

    // New interface method required by the updated HudRenderCallback
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        // RenderTickCounter doesn't expose tickDelta getter in this environment; just call the helper without it
        this.onHudRender(drawContext, 0f);
    }

    // Keep a local, non-interface helper method for the old float-based logic
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        if (!isModuleEnabled()) return;

        float scale = ModConfig.invincibleScale <= 0 ? 1.0f : ModConfig.invincibleScale;

        TextRenderer textRenderer = client.textRenderer;

        int[] layoutPos = parseLayoutPosition();
        int baseX = layoutPos[0];
        int baseY = layoutPos[1];

        int bonzoX = ModConfig.bonzoX != 0 ? ModConfig.bonzoX : baseX;
        int bonzoY = ModConfig.bonzoY != 0 ? ModConfig.bonzoY : baseY;

        int spiritX = ModConfig.spiritX != 0 ? ModConfig.spiritX : baseX;
        int spiritY = ModConfig.spiritY != 0 ? ModConfig.spiritY : baseX + 15;

        int phoenixX = ModConfig.phoenixX != 0 ? ModConfig.phoenixX : baseX;
        int phoenixY = ModConfig.phoenixY != 0 ? ModConfig.phoenixY : baseY + 30;

        int procX = ModConfig.procX != 0 ? ModConfig.procX : baseX;
        int procY = ModConfig.procY != 0 ? ModConfig.procY : baseY + 45;

        drawScaledText(drawContext, textRenderer, "§9Bonzo: " + getStatusText(bonzoTime), bonzoX, bonzoY, scale);
        drawScaledText(drawContext, textRenderer, "§fSpirit: " + getStatusText(spiritTime), spiritX, spiritY, scale);
        drawScaledText(drawContext, textRenderer, "§cPhoenix: " + getStatusText(phoenixTime), phoenixX, phoenixY, scale);

        if (!procText.equals(" ")) {
            drawScaledText(drawContext, textRenderer, procText, procX, procY, scale);
        }
    }

    // New message handler used by the registered lambda
    private void handleChatMessage(Text message, boolean overlay) {
        if (!isModuleEnabled()) return;

        String messageText = message.getString();

        // Debug: log all chat messages to help diagnose detection issues
        System.out.println("[InvincibleTimer] Received chat: " + messageText + " | overlay=" + overlay);

        if (overlay) return;

        long currentTime = System.currentTimeMillis();

        // Updated to match actual Hypixel message formats
        if (messageText.contains("Your Bonzo's Mask saved your life!") || messageText.contains("Bonzo's Mask") && messageText.contains("saved your life")) {
            bonzoTime = 3600;
            procText = "§9Bonzo Mask Procced";
            procTextEndTime = currentTime + 1500;
            System.out.println("[InvincibleTimer] Bonzo proc detected!");
        } else if (messageText.contains("Your Spirit Mask saved your life!") || messageText.contains("Spirit Mask") && messageText.contains("saved your life")) {
            spiritTime = 600;
            procText = "§fSpirit Mask Procced";
            procTextEndTime = currentTime + 1500;
            System.out.println("[InvincibleTimer] Spirit proc detected!");
        } else if (messageText.contains("Your Phoenix Pet saved you") || messageText.contains("Phoenix") && messageText.contains("saved you")) {
            phoenixTime = 1200;
            procText = "§cPhoenix Procced";
            procTextEndTime = currentTime + 1500;
            System.out.println("[InvincibleTimer] Phoenix proc detected!");
        }
    }

    public void onClientTick() {
        if (!isModuleEnabled()) return;

        if (bonzoTime > 0) bonzoTime--;
        if (spiritTime > 0) spiritTime--;
        if (phoenixTime > 0) phoenixTime--;

        if (System.currentTimeMillis() > procTextEndTime) {
            procText = " ";
        }
    }

    private void drawScaledText(DrawContext drawContext, TextRenderer textRenderer, String text, int x, int y, float scale) {
        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().scale(scale, scale);

        int scaledX = (int) (x / scale);
        int scaledY = (int) (y / scale);

        drawContext.drawText(textRenderer, Text.literal(text), scaledX, scaledY, 0xFFFFFFFF, true);
        drawContext.getMatrices().popMatrix();
    }

    private String getStatusText(int time) {
        return time <= 0 ? "§aREADY" : "§6" + String.format("%.1f", time / 20f);
    }

    private int[] parseLayoutPosition() {
        int[] result = new int[]{10, 50};
        try {
            String layout = ModConfig.invincibleTimerLayout;
            if (layout != null && !layout.isEmpty()) {
                String[] parts = layout.split(",");
                if (parts.length >= 2) {
                    result[0] = Integer.parseInt(parts[0].trim());
                    result[1] = Integer.parseInt(parts[1].trim());
                }
            }
        }  catch (Exception e) {
            // ignore parsing errors
        }
        return result;
    }

    private boolean isModuleEnabled() {
        return ModConfig.enableInvincibleTimer;
    }

    public static void register() {
        InvincibleTimer instance = new InvincibleTimer();
        HudRenderCallback.EVENT.register(instance);
        // Register GAME message listener (not CHAT) to catch system messages like proc notifications
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            instance.handleChatMessage(message, overlay);
        });
        // Register client tick event so timers count down
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            instance.onClientTick();
        });
    }
}
