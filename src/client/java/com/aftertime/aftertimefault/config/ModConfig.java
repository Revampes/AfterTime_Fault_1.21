package com.aftertime.aftertimefault.config;


import com.aftertime.aftertimefault.UI.annotations.*;

public class ModConfig {
    @ToggleButton(
            key = "dark-mode",
            name = "Dark Mode",
            description = "Protect your lovely eye balls",
            category = "Render"
    )
    public static boolean enableDarkMode = false;

    @Slider(
            key = "dark-mode",
            title = "Opacity",
            min = 0,
            max = 255
    )
    public static int darkModeOpacity = 128;
}