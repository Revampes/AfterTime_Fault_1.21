package com.aftertime.aftertimefault.modules.skyblock;

import com.aftertime.aftertimefault.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
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
    private long lastDebugTime = 0L;

    // Debug counter
    private int soundCallbackCount = 0;
    private long lastSoundCallbackTime = 0L;

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

        // Register item use callback for manual interaction detection
        UseItemCallback.EVENT.register((player, world, hand) -> {
            onInteract();
            return ActionResult.PASS;
        });

        debug("AutoFish registered successfully!");
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

    private void resetState() {
        debug("Resetting state");
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
            debug("AutoFish toggled: " + newState);
            if (!newState) {
                resetState();
            }
        }

        boolean enabled = isModuleEnabled();

        // Periodic debug output
        long now = System.currentTimeMillis();
        if (enabled && now - lastDebugTime > 5000) {
            debug("Status: enabled=" + enabled + ", hookThrown=" + isHookThrown() + ", fishHooked=" + fishHooked +
                  ", hookTick=" + hookTick + ", holdingRod=" + isHoldingRod());
            lastDebugTime = now;
        }

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
                    debug("Auto-throwing hook (no hook detected)");
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

        // Check for fish bite by monitoring bobber (since sound detection isn't working)
        if (!fishHooked && isHookThrown() && hookTick > 20) {
            System.out.println("[AutoFish TICK] Calling checkBobberForBite - hookTick=" + hookTick);
            checkBobberForBite();
        } else {
            if (hookTick > 0 && hookTick % 20 == 0) {
                System.out.println("[AutoFish TICK] NOT checking bobber: fishHooked=" + fishHooked +
                                 ", isHookThrown=" + isHookThrown() + ", hookTick=" + hookTick);
            }
        }

        // Handle fish hooked state
        if (fishHooked) {
            System.out.println("[AutoFish TICK] Fish hooked detected! tickAfterHook=" + tickAfterHook);
            if (tickAfterHook == 0) {
                // Immediately reel in on first tick
                System.out.println("[AutoFish TICK] *** REELING IN FISH NOW! ***");
                debug("Reeling in fish!");
                useRod();
                hookTick = -1;
                autoFishActive = false;
                fishHooked = false; // Reset fish hooked state
                System.out.println("[AutoFish TICK] Reel complete, state reset");
                if (cfgMessage()) say("Auto Fish: Fish hooked! Reeling in...");

                // Schedule re-throw if enabled
                if (cfgRethrow()) {
                    nextTickThrow = true;
                    System.out.println("[AutoFish TICK] Re-throw scheduled");
                }
                return;
            } else {
                System.out.println("[AutoFish TICK] tickAfterHook != 0, incrementing...");
                tickAfterHook++;
            }
        }

        // Rethrow on timeout
        if (cfgRethrow() && isHookThrown() && hookTick > cfgRethrowCooldownTicks() && !fishHooked) {
            // Reel now, then throw next tick
            debug("Timeout rethrow triggered (hookTick=" + hookTick + ")");
            useRod(); // reel
            nextTickThrow = true;
            autoFishActive = false;
            hookTick = -1;
            if (cfgMessage()) say("Auto Fish: Rethrow (timeout)");
        }

        // If requested, do the throw this tick after a reel
        if (nextTickThrow && isHoldingRod() && !fishHooked) {
            debug("Executing scheduled re-throw");
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

    // Check bobber for fish bite by monitoring position changes
    private Vec3d lastBobberPos = null;
    private int bobberStableTicks = 0;

    private void checkBobberForBite() {
        try {
            List<FishingBobberEntity> bobbers = mc.world.getEntitiesByClass(
                    FishingBobberEntity.class,
                    new Box(
                            mc.player.getPos().subtract(100, 100, 100),
                            mc.player.getPos().add(100, 100, 100)
                    ),
                    hook -> hook.getOwner() == mc.player
            );

            if (bobbers.isEmpty()) {
                System.out.println("[AutoFish checkBobber] No bobbers found");
                lastBobberPos = null;
                bobberStableTicks = 0;
                return;
            }

            FishingBobberEntity bobber = bobbers.get(0);
            Vec3d currentPos = bobber.getPos();
            Vec3d velocity = bobber.getVelocity();

            System.out.println("[AutoFish checkBobber] Bobber found at " +
                             String.format("%.2f,%.2f,%.2f", currentPos.x, currentPos.y, currentPos.z) +
                             " velocity=" + String.format("%.3f,%.3f,%.3f", velocity.x, velocity.y, velocity.z));

            // A fish bite causes the bobber to suddenly dip down (negative Y velocity)
            // The bobber should be stable (in water) and then suddenly move down

            if (lastBobberPos != null) {
                double deltaY = currentPos.y - lastBobberPos.y;
                double velocityY = velocity.y;

                System.out.println("[AutoFish checkBobber] deltaY=" + String.format("%.3f", deltaY) +
                                 ", velocityY=" + String.format("%.3f", velocityY) +
                                 ", stableTicks=" + bobberStableTicks);

                // Check if bobber was stable and is now moving significantly downward
                // Fish bite causes Y velocity of around -0.1 to -0.3
                boolean wasStable = bobberStableTicks >= 3;
                boolean suddenDrop = velocityY < -0.08 && deltaY < -0.02;

                System.out.println("[AutoFish checkBobber] wasStable=" + wasStable + ", suddenDrop=" + suddenDrop);

                if (wasStable && suddenDrop) {
                    System.out.println("[AutoFish] *** FISH BITE DETECTED via bobber motion! velocityY=" +
                                     String.format("%.3f", velocityY) + ", deltaY=" + String.format("%.3f", deltaY) + " ***");
                    fishHooked = true;
                    tickAfterHook = 0;
                    lastFishDetectionTime = System.currentTimeMillis();
                    if (cfgMessage()) say("Auto Fish: Fish detected!");
                    lastBobberPos = null;
                    bobberStableTicks = 0;
                    return;
                }

                // Track stability - bobber is stable if it's not moving much
                if (Math.abs(deltaY) < 0.01 && Math.abs(velocityY) < 0.05) {
                    bobberStableTicks++;
                    System.out.println("[AutoFish checkBobber] Bobber stable, incrementing to " + bobberStableTicks);
                } else {
                    if (bobberStableTicks > 0) {
                        System.out.println("[AutoFish checkBobber] Bobber not stable, resetting from " + bobberStableTicks);
                    }
                    bobberStableTicks = 0;
                }
            } else {
                System.out.println("[AutoFish checkBobber] First position recorded");
            }

            lastBobberPos = currentPos;

        } catch (Throwable e) {
            System.out.println("[AutoFish] Error checking bobber: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Splash sound detection to reel in
    private void onSound(SoundInstance sound) {
        if (mc.world == null || mc.player == null) return;
        if (!isModuleEnabled()) return;

        try {
            if (sound == null) return;

            String soundId = sound.getId().toString();

            // Debug only fishing-related sounds
            if (soundId.contains("fish") || soundId.contains("splash") || soundId.contains("bobber")) {
                System.out.println("[AutoFish] FISHING-RELATED Sound: " + soundId + " at " + sound.getX() + "," + sound.getY() + "," + sound.getZ());
            }

            if (cfgSlug()) {
                System.out.println("[AutoFish] Slug mode enabled, ignoring splash");
                return; // slug mode ignores splash
            }

            long now = System.currentTimeMillis();
            // basic debounce vs multiple sound system invocations in same tick
            if (now - lastFishDetectionTime < 250L) {
                System.out.println("[AutoFish] Debounce active, skipping");
                return;
            }

            // Check for splash sound - Fabric 1.21+ uses different sound IDs than Forge 1.8.9
            // In Forge 1.8.9 it was "game.player.swim.splash"
            // In Fabric 1.21+ it's "minecraft:entity.generic.splash" or "minecraft:entity.player.splash"
            boolean isSplash = soundId.contains("splash") ||
                              soundId.contains("entity.fishing_bobber.splash") ||
                              soundId.equals("minecraft:entity.generic.splash") ||
                              soundId.equals("minecraft:entity.player.splash");

            if (!isSplash) return;

            Vec3d soundPos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
            System.out.println("[AutoFish] *** SPLASH SOUND CONFIRMED at: " + soundPos);

            // Look for our fish hook near the splash (using slightly larger box than Forge version)
            Box around = new Box(
                    soundPos.subtract(0.5, 0.5, 0.5),
                    soundPos.add(0.5, 0.5, 0.5)
            );

            List<FishingBobberEntity> entities = mc.world.getEntitiesByClass(
                    FishingBobberEntity.class,
                    around,
                    hook -> hook.getOwner() == mc.player
            );

            System.out.println("[AutoFish] Found " + entities.size() + " bobber entities near splash");

            if (!entities.isEmpty()) {
                System.out.println("[AutoFish] *** FISH BITE DETECTED! Setting fishHooked=true ***");
                // Mark as hooked so the tick handler will reel it in properly
                if (!fishHooked) {
                    fishHooked = true;
                    tickAfterHook = 0;
                    lastFishDetectionTime = now;
                    System.out.println("[AutoFish] State updated: fishHooked=" + fishHooked + ", tickAfterHook=" + tickAfterHook);
                    if (cfgMessage()) say("Auto Fish: Fish detected (splash)!");
                } else {
                    System.out.println("[AutoFish] Already hooked, skipping duplicate");
                }
            } else {
                System.out.println("[AutoFish] No player-owned bobber found near splash");
            }
        } catch (Throwable e) {
            System.out.println("[AutoFish] ERROR in sound detection: " + e.getMessage());
            e.printStackTrace();
        }

        // Increment and test the sound callback counter
        soundCallbackCount++;
        if (soundCallbackCount >= 100) {
            long elapsed = System.currentTimeMillis() - lastSoundCallbackTime;
            debug("Sound callback test: received 100 callbacks in " + elapsed + "ms");
            lastSoundCallbackTime = System.currentTimeMillis();
            soundCallbackCount = 0;
        }
    }


    // Manual interaction detection
    private void onInteract() {
        try {
            if (isHoldingRod() && autoFishActive) {
                autoFishActive = false;
                debug("Manual interaction detected");
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

        // Add status indicator
        String status = fishHooked ? " [HOOKED]" : (isHookThrown() ? " [WAITING]" : " [NO HOOK]");
        label += status;

        drawContext.drawText(mc.textRenderer, Text.literal(label), x, y, fishHooked ? 0xFF00FF00 : 0xFFFFFFFF, true);
    }

    // Helpers
    private boolean isHoldingRod() {
        ItemStack main = mc.player.getMainHandStack();
        ItemStack off = mc.player.getOffHandStack();
        return (main != null && main.getItem() == Items.FISHING_ROD) || (off != null && off.getItem() == Items.FISHING_ROD);
    }

    private boolean isHookThrown() {
        try {
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
        } catch (Throwable e) {
            debug("Error checking hook: " + e.getMessage());
            return false;
        }
    }

    private void useRod() {
        try {
            if (mc.interactionManager != null && mc.player != null) {
                // Find which hand has the fishing rod
                net.minecraft.util.Hand hand = net.minecraft.util.Hand.MAIN_HAND;
                if (mc.player.getOffHandStack().getItem() == Items.FISHING_ROD) {
                    hand = net.minecraft.util.Hand.OFF_HAND;
                }
                debug("Using rod in hand: " + hand);
                mc.interactionManager.interactItem(mc.player, hand);
            }
        } catch (Throwable e) {
            debug("Error using rod: " + e.getMessage());
        }
    }

    private void say(String msg) {
        try {
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§6[Auto Fish] §7" + msg), false);
            }
        } catch (Throwable ignored) {}
    }

    private void debug(String msg) {
        try {
            if (mc.player != null) {
                // Send to action bar for less spam
                mc.player.sendMessage(Text.literal("§e[DEBUG] §7" + msg), true);
            }
            System.out.println("[AutoFish DEBUG] " + msg);
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
