package com.aftertime.aftertimefault.modules.dungeon;

import com.aftertime.aftertimefault.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.ClickEvent;
import net.minecraft.util.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoSell {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private ScheduledExecutorService executor;

    private final String[] defaultItems = {
            "enchanted ice", "superboom tnt", "rotten", "skeleton master", "skeleton grunt", "cutlass",
            "skeleton lord", "skeleton soldier", "zombie soldier", "zombie knight", "zombie commander", "zombie lord",
            "skeletor", "super heavy", "heavy", "sniper helmet", "dreadlord", "earth shard", "zombie commander whip",
            "machine gun", "sniper bow", "soulstealer bow", "silent death", "training weight",
            "beating heart", "premium flesh", "mimic fragment", "enchanted rotten flesh", "sign",
            "enchanted bone", "defuse kit", "optical lens", "tripwire hook", "button", "carpet", "lever", "diamond atom",
            "healing viii splash potion", "healing 8 splash potion", "candycomb"
    };

    private static AutoSell instance;

    public static AutoSell getInstance() {
        if (instance == null) {
            instance = new AutoSell();
        }
        return instance;
    }

    public AutoSell() {
        instance = this;
        executor = Executors.newSingleThreadScheduledExecutor();
        startAutoSellLoop();
    }

    private void startAutoSellLoop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        if (!ModConfig.enableAutoSell) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor();

        int delay = getDelayFromConfig();
        executor.scheduleWithFixedDelay(this::executeAutoSell, delay, delay, TimeUnit.MILLISECONDS);
    }

    private void executeAutoSell() {
        if (!ModConfig.enableAutoSell) {
            return;
        }

        List<String> sellList = getSellListFromConfig();
        if (sellList.isEmpty()) {
            return;
        }

        if (mc.player == null || mc.player.currentScreenHandler == null) {
            return;
        }

        if (!(mc.currentScreen instanceof GenericContainerScreen)) {
            return;
        }

        GenericContainerScreen screen = (GenericContainerScreen) mc.currentScreen;

        if (!isValidContainer(screen)) {
            return;
        }

        Integer slotIndex = findItemToSell(screen, sellList);
        if (slotIndex == null) {
            return;
        }

        int clickType = getClickTypeFromConfig();
        switch (clickType) {
            case 0:
                windowClick(slotIndex, ClickType.SHIFT);
                break;
            case 1:
                windowClick(slotIndex, ClickType.MIDDLE);
                break;
            case 2:
                windowClick(slotIndex, ClickType.LEFT);
                break;
        }
    }

    private int getDelayFromConfig() {
        return ModConfig.autoSellDelayMs;
    }

    private int getClickTypeFromConfig() {
        return ModConfig.autoSellClickType;
    }

    private List<String> getSellListFromConfig() {
        List<String> result = new ArrayList<>();
        if (ModConfig.autoSellUseDefaultItems) {
            result.addAll(Arrays.asList(defaultItems));
        }
        String customItems = ModConfig.autoSellCustomItems;
        if (customItems != null && !customItems.trim().isEmpty()) {
            String[] items = customItems.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed.toLowerCase());
                }
            }
        }
        return result;
    }

    private boolean isValidContainer(GenericContainerScreen screen) {
        try {
            if (screen == null || screen.getTitle() == null) return false;

            String title = screen.getTitle().getString().toLowerCase(Locale.ENGLISH);
            return title.contains("trades") || title.contains("booster cookie") ||
                    title.contains("farm merchant") || title.contains("ophelia");
        } catch (Exception e) {
            return false;
        }
    }

    private Integer findItemToSell(GenericContainerScreen screen, List<String> sellList) {
        if (screen.getScreenHandler() == null || screen.getScreenHandler().slots.size() < 90) {
            return null;
        }

        for (int i = 54; i < 90 && i < screen.getScreenHandler().slots.size(); i++) {
            Slot slot = screen.getScreenHandler().getSlot(i);
            if (slot == null || !slot.hasStack()) continue;

            ItemStack stack = slot.getStack();
            if (stack == null || stack.getName() == null) continue;

            String displayName = stack.getName().getString().toLowerCase(Locale.ENGLISH);

            for (String sellItem : sellList) {
                if (displayName.contains(sellItem.toLowerCase())) {
                    return i;
                }
            }
        }

        return null;
    }

    private void windowClick(int slotIndex, ClickType clickType) {
        try {
            if (mc.interactionManager == null || mc.player == null) return;

            switch (clickType) {
                case SHIFT:
                    mc.interactionManager.clickSlot(
                            mc.player.currentScreenHandler.syncId,
                            slotIndex,
                            0,
                            SlotActionType.QUICK_MOVE,
                            mc.player
                    );
                    break;
                case MIDDLE:
                    if (mc.player.getAbilities().creativeMode) {
                        mc.interactionManager.clickSlot(
                                mc.player.currentScreenHandler.syncId,
                                slotIndex,
                                2,
                                SlotActionType.CLONE,
                                mc.player
                        );
                    }
                    break;
                case LEFT:
                    mc.interactionManager.clickSlot(
                            mc.player.currentScreenHandler.syncId,
                            slotIndex,
                            0,
                            SlotActionType.PICKUP,
                            mc.player
                    );
                    break;
            }
        } catch (Exception e) {
            //
        }
    }

    public enum ClickType {
        SHIFT, MIDDLE, LEFT
    }

    public void onConfigChanged() {
        startAutoSellLoop();
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    public static void register() {
        getInstance();
    }
}
