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

    @ToggleButton(
            key = "star-mob-highlighter",
            name = "Star Mob Highlighter",
            description = "Highlights starred mobs and Shadow Assassins in dungeons",
            category = "Dungeon"
    )
    public static boolean enableStarMobHighlighter = false;

    @ColorPicker(
            key = "star-mob-highlighter",
            title = "Star Mob Color"
    )
    public static int starMobColor = 0xFFFF00; // Yellow

    @ColorPicker(
            key = "star-mob-highlighter",
            title = "Shadow Assassin Color"
    )
    public static int shadowAssassinColor = 0xAA00FF; // Purple

    @ToggleButton(
            key = "key-highlighter",
            name = "Key Highlighter",
            description = "Highlights Wither Keys and Blood Keys in dungeons",
            category = "Dungeon"
    )
    public static boolean enableKeyHighlighter = false;

    @ColorPicker(
            key = "key-highlighter",
            title = "Wither Key Color"
    )
    public static int witherKeyColor = 0xFFFF00; // Yellow

    @ColorPicker(
            key = "key-highlighter",
            title = "Blood Key Color"
    )
    public static int bloodKeyColor = 0xFF0000; // Red

    @ToggleButton(
            key = "auto-sell",
            name = "Auto Sell",
            description = "Automatically sell items in dungeon chests",
            category = "Dungeon"
    )
    public static boolean enableAutoSell = false;

    @Slider(
            key = "auto-sell",
            title = "Delay (ms)",
            min = 100,
            max = 2000
    )
    public static int autoSellDelayMs = 500;

    @Slider(
            key = "auto-sell",
            title = "Click Type",
            min = 0,
            max = 2
    )
    public static int autoSellClickType = 0; // 0=Shift, 1=Middle, 2=Left

    @CheckBox(
            key = "auto-sell",
            title = "Use Default Items"
    )
    public static boolean autoSellUseDefaultItems = true;

    @TextInputField(
            key = "auto-sell",
            title = "Custom Items (comma separated)",
            maxLength = 256
    )
    public static String autoSellCustomItems = "";

    @ToggleButton(
            key = "auto-fish",
            name = "Auto Fish",
            description = "Automatically fish for you",
            category = "Fishing"
    )
    public static boolean enabledAutoFish = false;

    @CheckBox(
            key = "auto-fish",
            title = "Sneak Hold"
    )
    public static boolean autofishSneakHold = false;

    @CheckBox(
            key = "auto-fish",
            title = "Throw If No Hook"
    )
    public static boolean autofishThrowIfNoHook = true;

    @Slider(
            key = "auto-fish",
            title = "Throw Cooldown (s)",
            min = 0,
            max = 10
    )
    public static int autofishThrowCooldownS = 1;

    @CheckBox(
            key = "auto-fish",
            title = "Rethrow"
    )
    public static boolean autofishRethrow = true;

    @Slider(
            key = "auto-fish",
            title = "Rethrow Timeout (s)",
            min = 1,
            max = 60
    )
    public static int autofishRethrowTimeoutS = 30;

    @CheckBox(
            key = "auto-fish",
            title = "Slug Mode"
    )
    public static boolean autofishSlugMode = false;

    @CheckBox(
            key = "auto-fish",
            title = "Show Messages"
    )
    public static boolean autofishMessages = true;

    @CheckBox(
            key = "auto-fish",
            title = "Show Timer"
    )
    public static boolean autofishShowTimer = true;

    @Slider(
            key = "auto-fish",
            title = "Timer X",
            min = 0,
            max = 1000
    )
    public static int autofishTimerX = 10;

    @Slider(
            key = "auto-fish",
            title = "Timer Y",
            min = 0,
            max = 1000
    )
    public static int autofishTimerY = 10;

    @CheckBox(
            key = "auto-fish",
            title = "Auto Shift"
    )
    public static boolean enabledAutoShift = false;

    @Slider(
            key = "auto-fish",
            title = "Auto Shift Interval (s)",
            min = 1,
            max = 60
    )
    public static int autofishAutoShiftIntervalS = 30;

    @TextInputField(
            key = "auto-fish",
            title = "Toggle Hotkey",
            maxLength = 16
    )
    public static String autofishHotkeyName = "F";
}