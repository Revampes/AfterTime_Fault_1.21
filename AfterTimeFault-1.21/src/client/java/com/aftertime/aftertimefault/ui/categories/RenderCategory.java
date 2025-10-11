package com.aftertime.aftertimefault.ui.categories;

import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.ui.components.ModuleContainer;
import com.aftertime.aftertimefault.ui.components.SliderComponent;
import com.aftertime.aftertimefault.ui.components.UIComponent;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class RenderCategory extends ModCategory {
    private final List<UIComponent> components = new ArrayList<>();
    private ModConfig config;

    public RenderCategory() {
        super("Render", Text.literal("Visual and rendering modules"));
    }

    @Override
    public void initialize(ModConfig config) {
        this.config = config;
        components.clear();

        System.out.println("=== Initializing RenderCategory ===");

        // Create Dark Mode module with sub-settings
        ModuleContainer darkModeModule = new ModuleContainer(
                0, 0, 380, // Reduced width to fit better
                Text.literal("Dark Mode"),
                Text.literal("Applies a dark overlay to the screen"),
                config.enableDarkMode,
                () -> {
                    config.enableDarkMode = !config.enableDarkMode;
                    config.save();
                    System.out.println("Dark Mode toggled to: " + config.enableDarkMode);
                }
        );

        // Add opacity sub-setting
        darkModeModule.addSubSetting(new SliderComponent(0, 0, 200, 20,
                Text.literal("Opacity"),
                0, 255,
                config.darkModeOpacity,
                value -> {
                    config.darkModeOpacity = (int) value;
                    config.save();
                    System.out.println("Dark Mode Opacity changed to: " + config.darkModeOpacity);
                }));

        components.add(darkModeModule);
        System.out.println("Added Dark Mode module");

        // Add a second test module
        ModuleContainer testModule = new ModuleContainer(
                0, 0, 380,
                Text.literal("Test Module"),
                Text.literal("This is a test module"),
                false,
                () -> System.out.println("Test Module toggled")
        );

        components.add(testModule);
        System.out.println("Added Test Module");

        System.out.println("Total components: " + components.size());
    }

    @Override
    public List<UIComponent> getComponents() {
        return components;
    }
}