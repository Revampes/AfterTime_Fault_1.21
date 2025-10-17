package com.aftertime.aftertimefault.modules.skyblock;

import com.aftertime.aftertimefault.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Random;

public class AutoFish {
    // State
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private KeyBinding toggleKeyBinding;

    private boolean autoFishActive = false; // whether we currently have a hook out
    private int hookTick = -1;              // ticks since hook thrown; -1 if no hook
    private int hookThrownCooldown = 0;     // cool down between auto throws
    private boolean nextTickThrow = false;  // after reeling, throw again next tick

    // Fish bite detection
    private boolean fishHooked = false;
    private int tickAfterHook = 0;
    private long lastFishDetectionTime = 0L;

    // Sneak handling
    private boolean holdSneakActive = false; // tracking if we are holding sneak due to cfgSneak

    // AutoShift feature
    private final Random rng = new Random();
    private int autoshiftCountdownTicks = -1;    // ticks until next auto shift tap
    private int autoshiftPressTicks = 0;         // remaining ticks to keep sneak pressed for tap

    private static AutoFish instance;

    public static AutoFish getInstance() {
        if (instance == null) {
            instance = new AutoFish();
        }
        return instance;
    }

    public AutoFish() {
        instance = this;
        register();
    }

    private void register() {
        // Register key binding
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aftertimefault.autofish.toggle",
                GLFW.GLFW_KEY_UNKNOWN,
                "category.aftertimefault.general"
        ));

        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTick());

        // Register world load/unload events
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetState());

        // Register sound event
        SoundPlayCallback.EVENT.register(this::onSound);

        // Register render event for timer
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> onRender(drawContext));

        // Register render event for fish detection (runs every frame for faster detection)
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            // Only do fish bite detection here; UI rendering is already handled above
            checkForFishBiteRender(); // Check during render for fastest detection
        });

        // Register item use callback for manual interaction detection
        UseItemCallback.EVENT.register((player, world, hand) -> {
            onInteract();
            return ActionResult.PASS;
        });
    }

    // Cached config helpers
    private boolean isModuleEnabled() {
        return ModConfig.enabledAutoFish;
    }

    private boolean cfgSneak() { return ModConfig.autofishSneakHold; }
    private boolean cfgThrowIfNoHook() { return ModConfig.autofishThrowIfNoHook; }
    private int cfgThrowCooldownTicks() { return Math.max(0, ModConfig.autofishThrowCooldownS) * 20; }
    private boolean cfgRethrow() { return ModConfig.autofishRethrow; }
    private int cfgRethrowCooldownTicks() { return Math.max(1, ModConfig.autofishRethrowTimeoutS) * 20; }
    private boolean cfgSlug() { return ModConfig.autofishSlugMode; }
    private boolean cfgMessage() { return ModConfig.autofishMessages; }
    private boolean cfgShowTimer() { return ModConfig.autofishShowTimer; }
    private int cfgTimerX() { return ModConfig.autofishTimerX; }
    private int cfgTimerY() { return ModConfig.autofishTimerY; }
    private boolean cfgAutoShift() { return ModConfig.enabledAutoShift; }
    private int cfgAutoShiftIntervalS() { return Math.max(1, ModConfig.autofishAutoShiftIntervalS); }
    private int cfgHotkey() {
        try {
            String name = ModConfig.autofishHotkeyName;
            if (name == null) return 0;
            name = name.trim();
            if (name.isEmpty() || name.equalsIgnoreCase("none")) return 0;
            // Convert key name to GLFW key code (simplified - you might need a mapping)
            return getKeyCodeFromName(name);
        } catch (Throwable ignored) { return 0; }
    }

    private int getKeyCodeFromName(String name) {
        // Simplified key mapping - you might want to expand this
        return switch (name.toUpperCase()) {
            case "F" -> GLFW.GLFW_KEY_F;
            case "R" -> GLFW.GLFW_KEY_R;
            case "G" -> GLFW.GLFW_KEY_G;
            case "H" -> GLFW.GLFW_KEY_H;
            case "V" -> GLFW.GLFW_KEY_V;
            case "B" -> GLFW.GLFW_KEY_B;
            default -> GLFW.GLFW_KEY_UNKNOWN;
        };
    }

    private void resetState() {
        autoFishActive = false;
        hookTick = -1;
        hookThrownCooldown = 0;
        nextTickThrow = false;
        fishHooked = false;
        tickAfterHook = 0;
        lastFishDetectionTime = 0L;
        // release sneak we held, but don't override user input
        if (holdSneakActive) releaseSneakIfNotPhysicallyDown();
        holdSneakActive = false;
        autoshiftCountdownTicks = -1;
        autoshiftPressTicks = 0;
        restoreSound();
    }

    private void onClientTick() {
        if (mc.world == null || mc.player == null) {
            resetState();
            return;
        }

        // Handle toggle key
        if (toggleKeyBinding.wasPressed()) {
            boolean newState = !ModConfig.enabledAutoFish;
            ModConfig.enabledAutoFish = newState;
            say("Toggle: " + (newState ? "Enabled" : "Disabled"));
            if (!newState) {
                resetState();
            }
        }

        boolean enabled = isModuleEnabled();

        // Maintain hookTick progression
        if (hookTick >= 0) hookTick++;

        if (!enabled) {
            // If disabled, make sure sound restored and state cleared
            restoreSound();
            // also finish any autoshift press without overriding user input
            if (autoshiftPressTicks > 0) releaseSneakIfNotPhysicallyDown();
            autoshiftPressTicks = 0;
            return;
        }

        // Sneak-hold (only if enabled via config)
        if (cfgSneak()) {
            KeyBinding.setKeyPressed(mc.options.sneakKey.getDefaultKey(), true);
            holdSneakActive = true;
        } else if (holdSneakActive) {
            // Was previously holding due to cfg; stop now but preserve user's physical key if down
            releaseSneakIfNotPhysicallyDown();
            holdSneakActive = false;
        }

        // Ensure we only operate when holding a fishing rod
        if (!isHoldingRod()) {
            // If we were tapping sneak for AutoShift, stop it cleanly
            if (autoshiftPressTicks > 0) {
                releaseSneakIfNotPhysicallyDown();
                autoshiftPressTicks = 0;
            }
            autoshiftCountdownTicks = -1; // pause/reset countdown until rod held again
            return;
        }

        // AutoShift: only when enabled AND holding fishing rod; periodically tap sneak
        if (cfgAutoShift()) {
            if (autoshiftCountdownTicks < 0) autoshiftCountdownTicks = computeNextAutoShiftDelayTicks();
            if (autoshiftPressTicks > 0) {
                // keep pressed during tap
                KeyBinding.setKeyPressed(mc.options.sneakKey.getDefaultKey(), true);
                autoshiftPressTicks--;
                if (autoshiftPressTicks == 0) {
                    releaseSneakIfNotPhysicallyDown();
                }
            } else if (autoshiftCountdownTicks-- <= 0) {
                // start a new tap (2 ticks)
                autoshiftPressTicks = 2;
                autoshiftCountdownTicks = computeNextAutoShiftDelayTicks();
            }
        } else {
            // not enabled: ensure we are not doing taps
            if (autoshiftPressTicks > 0) {
                releaseSneakIfNotPhysicallyDown();
                autoshiftPressTicks = 0;
            }
            autoshiftCountdownTicks = -1;
        }

        // Auto throw if no hook out
        if (cfgThrowIfNoHook()) {
            if (!isHookThrown()) {
                if (hookThrownCooldown >= cfgThrowCooldownTicks()) {
                    // Maximize player sounds to hear splash reliably while active
                    boostSoundTemporarily();
                    useRod();
                    autoFishActive = true;
                    hookTick = 0;
                    hookThrownCooldown = 0;
                    if (cfgMessage()) say("Auto Fish: Threw hook");
                } else {
                    hookThrownCooldown++;
                }
            } else {
                hookThrownCooldown = 0;
                autoFishActive = true;
            }
        }

        // Handle fish hooked state
        if (fishHooked) {
            if (tickAfterHook == 0) {
                // Immediately reel in
                useRod();
                if (cfgMessage()) say("Auto Fish: Fish hooked! Reeling in...");
            }
            if (tickAfterHook == (int)cfgThrowCooldownTicks()) {
                // Re-throw after delay
                useRod();
                autoFishActive = true;
                hookTick = 0;
                fishHooked = false;
                tickAfterHook = 0;
                if (cfgMessage()) say("Auto Fish: Re-thrown");
            }
            tickAfterHook++;
            if (tickAfterHook > 40) {
                // Safety reset after 2 seconds
                fishHooked = false;
                tickAfterHook = 0;
            }
        }

        // Rethrow on timeout
        if (cfgRethrow() && isHookThrown() && hookTick > cfgRethrowCooldownTicks() && !fishHooked) {
            // Reel now, then throw next tick
            useRod(); // reel
            nextTickThrow = true;
            autoFishActive = false;
            hookTick = -1;
            if (cfgMessage()) say("Auto Fish: Rethrow (timeout)");
        }

        // If requested, do the throw this tick after a reel
        if (nextTickThrow && isHoldingRod() && !fishHooked) {
            useRod(); // throw again
            autoFishActive = true;
            hookTick = 0;
            nextTickThrow = false;
            if (cfgMessage()) say("Auto Fish: Re-thrown");
        }
    }

    private int computeNextAutoShiftDelayTicks() {
        int base = cfgAutoShiftIntervalS();
        int min = Math.max(1, base - 3);
        int max = base + 3;
        int seconds = rng.nextInt(max - min + 1) + min; // inclusive range
        return seconds * 20;
    }

    private void releaseSneakIfNotPhysicallyDown() {
        try {
            int code = mc.options.sneakKey.getDefaultKey().getCode();
            boolean physicallyDown = GLFW.glfwGetKey(mc.getWindow().getHandle(), code) == GLFW.GLFW_PRESS;
            if (!physicallyDown) {
                KeyBinding.setKeyPressed(mc.options.sneakKey.getDefaultKey(), false);
            }
        } catch (Throwable ignored) { }
    }

    // Splash sound detection to reel in
    private void onSound(SoundInstance sound) {
        if (mc.world == null || mc.player == null) return;
        if (!isModuleEnabled() || cfgSlug()) return; // slug mode ignores splash
        try {
            if (sound == null) return;

            long now = System.currentTimeMillis();
            // basic debounce vs multiple sound system invocations in same tick
            if (now - lastFishDetectionTime < 250L) return;

            // Check for splash sound - sound IDs may differ in 1.21
            String soundId = sound.getId().toString();
            if (!soundId.contains("splash") && !soundId.contains("entity.fishing_bobber.splash")) return;

            Vec3d soundPos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());

            // Look for our fish hook near the splash
            Box around = new Box(
                    soundPos.subtract(0.4, 0.4, 0.4),
                    soundPos.add(0.4, 0.4, 0.4)
            );

            List<FishingBobberEntity> entities = mc.world.getEntitiesByClass(
                    FishingBobberEntity.class,
                    around,
                    hook -> hook.getOwner() == mc.player
            );

            if (!entities.isEmpty()) {
                // Always reel on matching splash, even if the hook was cast manually
                useRod(); // reel
                nextTickThrow = true; // schedule re-throw next tick for consistency
                autoFishActive = false;
                hookTick = -1;
                lastFishDetectionTime = now;
                if (cfgMessage()) say("Auto Fish: Reel on splash");
            }
        } catch (Throwable ignored) { }
    }

    // Check for fish bite by looking at bobber entity custom name (called during render for fastest detection)
    private void checkForFishBiteRender() {
        if (mc.world == null || mc.player == null) return;
        if (!isModuleEnabled() || cfgSlug()) return;

        try {
            long currentTime = System.currentTimeMillis();
            // Add 500ms cooldown to prevent duplicate detections
            if (currentTime - lastFishDetectionTime < 500L) return;

            // Check all entities for our fishing bobber with "!!!" in name (fish hooked indicator)
            for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
                // Check if entity has custom name with "!!!"
                if (entity.hasCustomName()) {
                    String name = entity.getCustomName().getString();
                    if (name.contains("!!!")) {
                        // Found an entity with "!!!" - check if it's a fishing bobber
                        if (entity instanceof FishingBobberEntity) {
                            FishingBobberEntity bobber = (FishingBobberEntity) entity;

                            // Check if this is our bobber
                            if (bobber.getOwner() == mc.player) {
                                // Fish is hooked! Set the flag and let onClientTick handle reeling
                                if (!fishHooked) {
                                    fishHooked = true;
                                    tickAfterHook = 0;
                                    lastFishDetectionTime = currentTime;
                                    if (cfgMessage()) say("Auto Fish: Fish detected!");
                                }
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) { }
    }

    // Check for fish bite by looking at bobber entity custom name
    private void checkForFishBite() {
        // This method is no longer used - detection moved to render event for faster response
    }

    // Manual interaction detection
    private void onInteract() {
        try {
            if (isHoldingRod() && autoFishActive) {
                autoFishActive = false;
                if (cfgMessage()) say("Auto Fish: Manual interaction");
            }
        } catch (Throwable ignored) { }
    }

    // Draw simple timer HUD
    private void onRender(net.minecraft.client.gui.DrawContext drawContext) {
        if (!isModuleEnabled() || !cfgShowTimer()) return;
        if (mc.player == null || mc.world == null) return;

        int x = cfgTimerX();
        int y = cfgTimerY();
        String label = "Hook: " + (hookTick < 0 ? "--" : String.format("%.1fs", hookTick / 20.0));

        drawContext.drawText(mc.textRenderer, Text.literal(label), x, y, 0xFFFFFFFF, true);
    }

    // Helpers
    private boolean isHoldingRod() {
        ItemStack main = mc.player.getMainHandStack();
        ItemStack off = mc.player.getOffHandStack();
        return (main != null && main.getItem() == Items.FISHING_ROD) || (off != null && off.getItem() == Items.FISHING_ROD);
    }

    private boolean isHookThrown() {
        // Search for player's fishing bobber
        List<FishingBobberEntity> hooks = mc.world.getEntitiesByClass(
                FishingBobberEntity.class,
                new Box(
                        mc.player.getPos().subtract(100, 100, 100),
                        mc.player.getPos().add(100, 100, 100)
                ),
                hook -> hook.getOwner() == mc.player
        );
        return !hooks.isEmpty();
    }

    private void useRod() {
        try {
            if (mc.interactionManager != null && mc.player != null) {
                // Find which hand has the fishing rod
                net.minecraft.util.Hand hand = net.minecraft.util.Hand.MAIN_HAND;
                if (mc.player.getOffHandStack().getItem() == Items.FISHING_ROD) {
                    hand = net.minecraft.util.Hand.OFF_HAND;
                }
                mc.interactionManager.interactItem(mc.player, hand);
            }
        } catch (Throwable ignored) {}
    }

    private void say(String msg) {
        try {
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§6[Auto Fish] §7" + msg), false);
            }
        } catch (Throwable ignored) {}
    }

    private void boostSoundTemporarily() {
        // Sound boosting can be implemented if needed for better splash detection
        // Currently not essential for basic functionality
    }

    private void restoreSound() {
        // Restore sound settings if modified by boostSoundTemporarily
    }

    public void shutdown() {
        resetState();
    }
}
