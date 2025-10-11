package com.aftertime.aftertimefault.config;

public class ModConfig {
    // Render category
    public boolean enableDarkMode = false;
    public int darkModeOpacity = 128;

    // Add other config fields as needed

    public void save() {
        ConfigManager.saveConfig(this);
    }

    public static ModConfig load() {
        return ConfigManager.loadConfig();
    }
}
