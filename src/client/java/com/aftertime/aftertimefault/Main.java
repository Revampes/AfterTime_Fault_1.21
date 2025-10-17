package com.aftertime.aftertimefault;

import com.aftertime.aftertimefault.KeyBind.KeybindHandler;
import com.aftertime.aftertimefault.UI.config.ModConfigIO;
import com.aftertime.aftertimefault.modules.dungeon.AutoSell;
import com.aftertime.aftertimefault.modules.dungeon.InvincibleTimer;
import com.aftertime.aftertimefault.modules.dungeon.KeyHighlight;
import com.aftertime.aftertimefault.modules.dungeon.StarMobHighlight;
import com.aftertime.aftertimefault.modules.render.DarkMode;
import com.aftertime.aftertimefault.modules.skyblock.AutoFish;
import net.fabricmc.api.ClientModInitializer;

public class Main implements ClientModInitializer {
    public static final String MOD_NAME = "AfterTimeFault";
    public static final String VERSION = "v1.0";
    public static final String AUTHOR = "Revampes(AfterTime)";

    @Override
    public void onInitializeClient() {
        ModConfigIO.load();
        // Register DarkMode to use the loaded ModConfig
        DarkMode.register();
        InvincibleTimer.register();
        StarMobHighlight.register();
        KeyHighlight.register();
        AutoSell.register();
        AutoFish.getInstance();
        // Register keybinds
        KeybindHandler.registerKeybinds();
    }
}