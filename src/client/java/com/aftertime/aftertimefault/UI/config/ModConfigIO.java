package com.aftertime.aftertimefault.UI.config;

import com.aftertime.aftertimefault.config.ModConfig;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ModConfigIO {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("ExampleGUI");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("ExampleGUI-config.json");

    public static void load() {
        ensureDir();
        if (!java.nio.file.Files.exists(CONFIG_FILE)) {
            // Nothing saved yet
            return;
        }
        try (Reader reader = new InputStreamReader(java.nio.file.Files.newInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            JsonElement rootEl = JsonParser.parseReader(reader);
            if (!rootEl.isJsonObject()) return;
            JsonObject obj = rootEl.getAsJsonObject();
            // Iterate all fields in ModConfig and populate from json when present
            for (Field f : ModConfig.class.getDeclaredFields()) {
                String name = f.getName();
                if (!obj.has(name)) continue;
                try {
                    f.setAccessible(true);
                    Class<?> t = f.getType();
                    JsonElement v = obj.get(name);
                    if (t == boolean.class || t == Boolean.class) {
                        f.setBoolean(null, v.getAsBoolean());
                    } else if (t == int.class || t == Integer.class) {
                        f.setInt(null, v.getAsInt());
                    } else if (t == float.class || t == Float.class) {
                        f.setFloat(null, v.getAsFloat());
                    } else if (t == double.class || t == Double.class) {
                        f.setDouble(null, v.getAsDouble());
                    } else if (t == long.class || t == Long.class) {
                        f.setLong(null, v.getAsLong());
                    } else if (t == String.class) {
                        f.set(null, v.isJsonNull() ? null : v.getAsString());
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        ensureDir();
        JsonObject obj = new JsonObject();
        for (Field f : ModConfig.class.getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object val = f.get(null);
                if (val == null) {
                    obj.add(f.getName(), JsonNull.INSTANCE);
                } else if (val instanceof Boolean) {
                    obj.addProperty(f.getName(), (Boolean) val);
                } else if (val instanceof Number) {
                    obj.addProperty(f.getName(), (Number) val);
                } else if (val instanceof String) {
                    obj.addProperty(f.getName(), (String) val);
                } else {
                    // Fallback: store toString
                    obj.addProperty(f.getName(), String.valueOf(val));
                }
            } catch (Throwable ignored) {}
        }

        try (Writer writer = new OutputStreamWriter(java.nio.file.Files.newOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(obj, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ensureDir() {
        try {
            java.nio.file.Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
