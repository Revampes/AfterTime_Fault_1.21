package com.aftertime.aftertimefault.KeyBind;

import com.aftertime.aftertimefault.UI.ModGUI;
import com.aftertime.aftertimefault.events.ClientTickEventBus;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
    // Public so GameOptionsMixin can add it to GameOptions.allKeys at startup
    public static final KeyBinding CONFIG_GUI_KEY = new KeyBinding(
            "Open Config GUI",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "ExampleGUI");

    public static void registerKeybinds() {
        // Key is already added to allKeys by GameOptionsMixin.
        // Just register the tick handler that opens the GUI.
        ClientTickEventBus.register(client -> {
            while (CONFIG_GUI_KEY.wasPressed()) {
                client.setScreen(new ModGUI());
            }
        });
    }
}
