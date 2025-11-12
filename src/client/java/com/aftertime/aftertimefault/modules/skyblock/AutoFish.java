package com.aftertime.aftertimefault.modules.skyblock;

import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.util.PlayerUtil;
import me.shimmer.api.client.module.Module;
import me.shimmer.api.event.bus.annotation.ShimmerSubscribe;
import me.shimmer.api.event.impl.RenderEvent;
import me.shimmer.api.event.impl.TickEvent;
import me.shimmer.client.ui.hud.Hud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class AutoFish extends Module {
    private boolean fishHooked;
    private int tickAfterHook;
    private long lastThrow;
    private boolean lastRotate = false;
    private boolean lastMove = false;
    private long lastFishUP = 0L;
    private float originYaw = 0.0F;
    private float originPitch = 0.0F;
    private boolean isThrowed = false;
    private boolean prevThrowed = false;
    private final Timer timer = new Timer();
    private Random random = new Random();

    public AutoFish() {
        super("AutoFish", "Fishing Automatically", -1, Category.MACRO);
        this.setNeedDisable(true);
    }

    @ShimmerSubscribe
    public void onRender(RenderEvent.Post event) {
        if (mc.world == null || mc.player == null)
            return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity.hasCustomName() && entity.getDisplayName().getString().contains("!!!") && System.currentTimeMillis() - lastFishUP >= 500L) {
                fishHooked = true;
                tickAfterHook = 0;
                lastFishUP = System.currentTimeMillis();
                break;
            }
        }
    }

    @Override
    public void onEnable() {
        if (MinecraftClient.getInstance().player != null) {
            super.onEnable();
            fishHooked = false;
            random = new Random();
            originYaw = MinecraftClient.getInstance().player.getYaw();
            originPitch = MinecraftClient.getInstance().player.getPitch();
            if (ModConfig.autoFishAlwaysSneak)
                mc.options.sneakKey.setPressed(true);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (ModConfig.autoFishAlwaysSneak)
            mc.options.sneakKey.setPressed(false);
    }

    @ShimmerSubscribe
    public void onTick(TickEvent event) {
        float randomYaw = random.nextFloat() * 4.5F - 2.25F;
        float randomPitch = random.nextFloat() * 4.5F - 2.25F;
        if (MinecraftClient.getInstance().player == null)
            return;
        isThrowed = false;
        for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
            if (entity instanceof FishingBobberEntity) {
                FishingBobberEntity hook = (FishingBobberEntity) entity;
                if (hook.getPlayerOwner() != null && hook.getPlayerOwner().getUuid().equals(MinecraftClient.getInstance().player.getUuid())) {
                    isThrowed = true;
                    break;
                }
            }
        }
        if (ModConfig.autoFishEnableAutoRethrow && System.currentTimeMillis() - lastThrow >= (isThrowed ? 20000L : 3000L) && !MinecraftClient.getInstance().player.getMainHandStack().isEmpty() && MinecraftClient.getInstance().player.getMainHandStack().getItem() == Items.FISHING_ROD) {
            MinecraftClient.getInstance().player.setYaw(originYaw);
            MinecraftClient.getInstance().player.setPitch(originPitch);
            PlayerUtil.useItem();
            lastThrow = System.currentTimeMillis();
            Hud.onMessage(Text.literal(Formatting.AQUA + "AutoFish"), Text.literal("After " + (isThrowed ? 20 : 3) + "s"), Text.literal("Auto reset triggered"));
            if (isThrowed)
                timer.schedule(new TimerTask() {
                    public void run() {
                        PlayerUtil.useItem();
                        AutoFish.this.lastThrow = System.currentTimeMillis();
                    }
                }, 1000L);
        }
        if (isThrowed && !prevThrowed) {
            lastThrow = System.currentTimeMillis();
            Hud.onMessage(Text.literal(Formatting.AQUA + "AutoFish"), Text.literal("Throw hook detected"), Text.literal("Already reset the hook timer"));
        }
        if (fishHooked) {
            if (tickAfterHook == 0) {
                PlayerUtil.useItem();
                lastThrow = System.currentTimeMillis();
                if (ModConfig.autoFishEnableRotate) {
                    MinecraftClient.getInstance().player.setYaw(MinecraftClient.getInstance().player.getYaw() + randomYaw);
                    MinecraftClient.getInstance().player.setPitch(MinecraftClient.getInstance().player.getPitch() + randomPitch);
                }
            }
            if (tickAfterHook == 3 &&
                    ModConfig.autoFishEnableRotate) {
                MinecraftClient.getInstance().player.setYaw(MinecraftClient.getInstance().player.getYaw() - randomYaw);
                MinecraftClient.getInstance().player.setPitch(MinecraftClient.getInstance().player.getPitch() - randomPitch);
            }
            if (tickAfterHook == ModConfig.autoFishThrowDelay) {
                if (Math.abs(MinecraftClient.getInstance().player.getYaw() - originYaw) > 2.5F || Math.abs(MinecraftClient.getInstance().player.getPitch() - originPitch) > 2.5F) {
                    MinecraftClient.getInstance().player.setYaw(originYaw);
                    MinecraftClient.getInstance().player.setPitch(originPitch);
                }
                PlayerUtil.useItem();
                MinecraftClient.getInstance().player.setYaw(MinecraftClient.getInstance().player.getYaw() + (lastRotate ? 3 : -3));
                lastRotate = !lastRotate;
                lastThrow = System.currentTimeMillis();
            }
            if (tickAfterHook == 2 && ModConfig.autoFishEnableSneak)
                MinecraftClient.getInstance().options.sneakKey.setPressed(true);
            if (tickAfterHook == 3 && ModConfig.autoFishEnableMove) {
                if (ModConfig.autoFishMoveMethod == 0) // WS
                    if (lastMove) {
                        MinecraftClient.getInstance().options.backKey.setPressed(true);
                    } else {
                        MinecraftClient.getInstance().options.forwardKey.setPressed(true);
                    }
                if (ModConfig.autoFishMoveMethod == 1) // AD
                    if (lastMove) {
                        MinecraftClient.getInstance().options.rightKey.setPressed(true);
                    } else {
                        MinecraftClient.getInstance().options.leftKey.setPressed(true);
                    }
            }
            if (tickAfterHook == 4 && ModConfig.autoFishEnableMove) {
                if (ModConfig.autoFishMoveMethod == 0) // WS
                    if (lastMove) {
                        MinecraftClient.getInstance().options.backKey.setPressed(false);
                        MinecraftClient.getInstance().options.forwardKey.setPressed(true);
                    } else {
                        MinecraftClient.getInstance().options.forwardKey.setPressed(false);
                        MinecraftClient.getInstance().options.backKey.setPressed(true);
                    }
                if (ModConfig.autoFishMoveMethod == 1) // AD
                    if (lastMove) {
                        MinecraftClient.getInstance().options.rightKey.setPressed(false);
                        MinecraftClient.getInstance().options.leftKey.setPressed(true);
                    } else {
                        MinecraftClient.getInstance().options.leftKey.setPressed(false);
                        MinecraftClient.getInstance().options.rightKey.setPressed(true);
                    }
            }
            if (tickAfterHook == 4 && ModConfig.autoFishEnableMove) {
                if (ModConfig.autoFishMoveMethod == 0) // WS
                    if (lastMove) {
                        MinecraftClient.getInstance().options.forwardKey.setPressed(false);
                    } else {
                        MinecraftClient.getInstance().options.backKey.setPressed(false);
                    }
                if (ModConfig.autoFishMoveMethod == 1) // AD
                    if (lastMove) {
                        MinecraftClient.getInstance().options.leftKey.setPressed(false);
                    } else {
                        MinecraftClient.getInstance().options.rightKey.setPressed(false);
                    }
                lastMove = !lastMove;
            }
            if (tickAfterHook == 5 && ModConfig.autoFishEnableSneak)
                MinecraftClient.getInstance().options.sneakKey.setPressed(false);
            if (tickAfterHook > 31)
                fishHooked = false;
            tickAfterHook++;
        }
        if (isThrowed != prevThrowed)
            prevThrowed = isThrowed;
    }
}
