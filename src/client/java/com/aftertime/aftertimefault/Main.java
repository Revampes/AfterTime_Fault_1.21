package com.aftertime.aftertimefault;

import com.aftertime.aftertimefault.config.KeyBindManager;
import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.modules.render.DarkMode;
import com.aftertime.aftertimefault.ui.screens.ModMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Main implements ClientModInitializer {
    public static final String MOD_NAME = "AfterTimeFault";
    public static final String VERSION = "v1.0";
    public static final String AUTHOR = "Revampes(AfterTime)";

    private static ModConfig config;

    @Override
    public void onInitializeClient() {
        config = ModConfig.load();

        // Register keybinds
        KeyBindManager.registerKeyBinds();

        // Register modules
        DarkMode.register(config);

        // Register tick event for keybind detection
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeyBindManager.OPEN_GUI_KEYBIND.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new ModMenuScreen());
                }
            }
        });
    }

    public static ModConfig getConfig() {
        return config;
    }
}