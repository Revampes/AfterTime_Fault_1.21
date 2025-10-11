package com.aftertime.aftertimefault.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindManager {
    public static KeyBinding OPEN_GUI_KEYBIND;

    public static void registerKeyBinds() {
        OPEN_GUI_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aftertimefault.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, // Default key - you can change this
                "category.aftertimefault.general"
        ));
    }
}