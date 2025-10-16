package com.aftertime.aftertimefault.KeyBind;

import com.aftertime.aftertimefault.UI.ModGUI;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
    private static KeyBinding configGuiKey = new KeyBinding("Open Config GUI", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "ExampleGUI");

    public static void registerKeybinds() {
        KeyBindingHelper.registerKeyBinding(configGuiKey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configGuiKey.wasPressed()) {
                client.setScreen(new ModGUI());
            }
        });
    }
}
