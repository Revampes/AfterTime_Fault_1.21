package com.aftertime.aftertimefault.modules.skyblock;

import com.aftertime.aftertimefault.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class AutoFish {
    private static AutoFish instance;
    private final MinecraftClient mc = MinecraftClient.getInstance();

    // State tracking
    private boolean isEnabled = false;
    private int throwCooldown = 0;
    private int rethrowTimeout = 0;
    private int autoShiftTimer = 0;
    private int catchCooldown = 0;
    private int bobberStableTime = 0; // Time bobber has been stable in water
    private long startTime = 0;
    private int fishCaughtCount = 0;

    // Keybind
    private KeyBinding toggleKey;

    public static AutoFish getInstance() {
        if (instance == null) {
            instance = new AutoFish();
        }
        return instance;
    }

    public AutoFish() {
        instance = this;
        registerKeybind();
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
        ClientTickEvents.END_CLIENT_TICK.register(client -> renderHud());
    }

    private void registerKeybind() {
        String keyName = ModConfig.autofishHotkeyName.isEmpty() ? "F" : ModConfig.autofishHotkeyName;
        int keyCode = getKeyCode(keyName);

        toggleKey = new KeyBinding(
            "Toggle Auto Fish",
            InputUtil.Type.KEYSYM,
            keyCode,
            "AfterTimeFault"
        );

        KeyBindingHelper.registerKeyBinding(toggleKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                toggleAutoFish();
            }
        });
    }

    private int getKeyCode(String keyName) {
        return switch (keyName.toUpperCase()) {
            case "F" -> GLFW.GLFW_KEY_F;
            case "G" -> GLFW.GLFW_KEY_G;
            case "H" -> GLFW.GLFW_KEY_H;
            case "X" -> GLFW.GLFW_KEY_X;
            case "Z" -> GLFW.GLFW_KEY_Z;
            case "V" -> GLFW.GLFW_KEY_V;
            case "B" -> GLFW.GLFW_KEY_B;
            default -> GLFW.GLFW_KEY_F;
        };
    }

    private void toggleAutoFish() {
        if (!ModConfig.enabledAutoFish) {
            return;
        }

        isEnabled = !isEnabled;

        if (isEnabled) {
            startTime = System.currentTimeMillis();
            fishCaughtCount = 0;
            if (ModConfig.autofishMessages) {
                sendMessage("§a[AutoFish] Enabled!");
            }
        } else {
            disable();
            if (ModConfig.autofishMessages) {
                sendMessage("§c[AutoFish] Disabled!");
            }
        }
    }

    private void onTick() {
        if (!ModConfig.enabledAutoFish || !isEnabled || mc.player == null || mc.world == null) {
            return;
        }

        // Check if holding fishing rod
        if (!isHoldingFishingRod()) {
            return;
        }

        // Update timers
        if (throwCooldown > 0) throwCooldown--;
        if (catchCooldown > 0) catchCooldown--;
        if (rethrowTimeout > 0) rethrowTimeout--;

        // Auto shift
        if (ModConfig.enabledAutoShift) {
            autoShiftTimer++;
            int shiftInterval = ModConfig.autofishAutoShiftIntervalS * 20; // Convert to ticks
            if (autoShiftTimer >= shiftInterval) {
                autoShiftTimer = 0;
                mc.options.sneakKey.setPressed(!mc.options.sneakKey.isPressed());
            }
        }

        // Sneak hold
        if (ModConfig.autofishSneakHold) {
            mc.options.sneakKey.setPressed(true);
        }

        // Get fishing bobber
        FishingBobberEntity bobber = mc.player.fishHook;

        // Check for catch
        if (bobber != null) {
            // Reset rethrow timeout when bobber exists
            rethrowTimeout = ModConfig.autofishRethrowTimeoutS * 20;

            // Check if bobber is stable in water
            if (bobber.isTouchingWater() && !bobber.isOnGround()) {
                bobberStableTime++;
            } else {
                bobberStableTime = 0;
            }

            // Only start detecting after bobber has been stable for at least 1 second (20 ticks)
            if (catchCooldown == 0 && bobberStableTime > 20 && isCaught(bobber)) {
                catchFish();
            }
        } else {
            // No bobber - reset stability timer
            bobberStableTime = 0;

            // No bobber - should we throw?
            if (ModConfig.autofishThrowIfNoHook && throwCooldown == 0) {
                throwRod();
            }

            // Rethrow timeout check
            if (ModConfig.autofishRethrow && rethrowTimeout == 0 && throwCooldown == 0) {
                throwRod();
            }
        }
    }

    private boolean isHoldingFishingRod() {
        if (mc.player == null) return false;
        return mc.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem;
    }

    private boolean isCaught(FishingBobberEntity bobber) {
        // The bobber must be in water and stable before we check for bites
        if (bobber.isRemoved() || !bobber.isTouchingWater()) return false;

        // In Minecraft, when a fish bites:
        // 1. The bobber makes a splash sound (handled by game)
        // 2. The bobber gets pulled DOWN with significant force
        // 3. Or an entity is hooked

        double velocityY = bobber.getVelocity().y;

        // When floating normally, velocity is very small (-0.05 to 0.05)
        // When a fish bites, it pulls down HARD (< -0.4)
        // We use -0.35 as threshold to avoid false positives
        if (velocityY < -0.35) {
            return true;
        }

        // Check if an entity is hooked (fish already on line)
        return bobber.getHookedEntity() != null;
    }

    private void catchFish() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Play sound
        mc.getSoundManager().play(
            PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
        );

        // Reel in
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);

        // Update stats
        fishCaughtCount++;

        // Reset stability tracker
        bobberStableTime = 0;

        // Set cooldowns
        catchCooldown = 10; // 0.5 seconds
        throwCooldown = ModConfig.autofishThrowCooldownS * 20;

        // Rethrow immediately if enabled
        if (ModConfig.autofishRethrow) {
            // Schedule throw after cooldown
            rethrowTimeout = 0;
        }

        if (ModConfig.autofishMessages && !ModConfig.autofishSlugMode) {
            sendMessage("§e[AutoFish] Fish caught! Total: " + fishCaughtCount);
        }
    }

    private void throwRod() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Throw rod
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);

        // Set cooldown
        throwCooldown = ModConfig.autofishThrowCooldownS * 20;
        rethrowTimeout = ModConfig.autofishRethrowTimeoutS * 20;
    }

    private void renderHud() {
        // HUD rendering is handled by InGameHudMixin
    }

    public void renderHudOverlay(DrawContext context) {
        if (!isEnabled || mc.player == null) {
            return;
        }

        // Calculate time elapsed
        long elapsed = System.currentTimeMillis() - startTime;
        long seconds = elapsed / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        // Build display text
        String timeText = String.format("§aAutoFish: §f%02d:%02d", minutes, seconds);
        String countText = String.format("§eFish: §f%d", fishCaughtCount);

        int x = ModConfig.autofishTimerX;
        int y = ModConfig.autofishTimerY;

        // Draw with shadow
        context.drawTextWithShadow(mc.textRenderer, timeText, x, y, 0xFFFFFF);
        context.drawTextWithShadow(mc.textRenderer, countText, x, y + 10, 0xFFFFFF);

        // Show status
        if (mc.player.fishHook != null) {
            context.drawTextWithShadow(mc.textRenderer, "§bStatus: §fWaiting...", x, y + 20, 0xFFFFFF);
        } else if (throwCooldown > 0) {
            context.drawTextWithShadow(mc.textRenderer, "§cCooldown: §f" + (throwCooldown / 20) + "s", x, y + 20, 0xFFFFFF);
        }
    }

    private void disable() {
        throwCooldown = 0;
        rethrowTimeout = 0;
        autoShiftTimer = 0;
        bobberStableTime = 0;

        // Release keys
        if (mc.options != null) {
            if (ModConfig.autofishSneakHold || ModConfig.enabledAutoShift) {
                mc.options.sneakKey.setPressed(false);
            }
        }
    }

    private void sendMessage(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(message), false);
        }
    }

    public static void register() {
        getInstance();
    }
}

