package com.aftertime.aftertimefault.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // Fix the path - use the standard Minecraft config directory
    private static final Path CONFIG_PATH = Path.of("config/aftertimefault.json");

    public static void saveConfig(ModConfig config) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(config, writer);
                System.out.println("Config saved to: " + CONFIG_PATH.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Failed to save config:");
            e.printStackTrace();
        }
    }

    public static ModConfig loadConfig() {
        try {
            File file = CONFIG_PATH.toFile();
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    ModConfig config = GSON.fromJson(reader, ModConfig.class);
                    System.out.println("Config loaded from: " + CONFIG_PATH.toAbsolutePath());
                    return config;
                }
            } else {
                System.out.println("Config file not found, creating default config");
            }
        } catch (Exception e) {
            System.err.println("Failed to load config:");
            e.printStackTrace();
        }
        return new ModConfig();
    }
}