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

    @ToggleButton(
            key = "invincible-timer",
            name = "Invincible Timer",
            description = "Shows cooldown for Bonzo Mask, Spirit Mask and Phoenix Pet.",
            category = "Dungeon"
    )
    public static boolean enableInvincibleTimer = false;

    @UILayout(
            key = "invincible-timer",
            title = "Pos of timer",
            posx = 10,
            posy = 50,
            scale = 1.0f
    )
    public static String invincibleTimerLayout = "10,50,1.0";

    @Slider(key = "invincible-timer", title = "Bonzo X", min = -500, max = 500)
    public static int bonzoX = 10;

    @Slider(key = "invincible-timer", title = "Bonzo Y", min = -500, max = 500)
    public static int bonzoY = 50;

    @Slider(key = "invincible-timer", title = "Spirit X", min = -500, max = 500)
    public static int spiritX = 10;

    @Slider(key = "invincible-timer", title = "Spirit Y", min = -500, max = 500)
    public static int spiritY = 65;

    @Slider(key = "invincible-timer", title = "Phoenix X", min = -500, max = 500)
    public static int phoenixX = 10;

    @Slider(key = "invincible-timer", title = "Phoenix Y", min = -500, max = 500)
    public static int phoenixY = 80;

    @Slider(key = "invincible-timer", title = "Proc Text X", min = -500, max = 500)
    public static int procX = 10;

    @Slider(key = "invincible-timer", title = "Proc Text Y", min = -500, max = 500)
    public static int procY = 95;

    @Slider(key = "invincible-timer", title = "Scale", min = 0.5f, max = 3.0f)
    public static float invincibleScale = 1.0f;
}