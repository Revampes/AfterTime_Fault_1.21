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
    public static int bloodKeyColor = 0xAA0000; // Red

    @ToggleButton(
            key = "secret-clicks",
            name = "Secret Click Highlighter",
            description = "Highlights secret chests, levers, and skulls you interact with in dungeons",
            category = "Dungeon"
    )
    public static boolean enableSecretClicks = false;

    @ColorPicker(
            key = "secret-clicks",
            title = "Highlight Color"
    )
    public static int secretClicksHighlightColor = 0x8000FF00; // Semi-transparent green

    @ToggleButton(
            key = "auto-sell",
            name = "Auto Sell",
            description = "Automatically sells configured drops when an NPC trade window is open",
            category = "Dungeon"
    )
    public static boolean enableAutoSell = false;

    @Slider(
            key = "auto-sell",
            title = "Sell Loop Delay (ms)",
            min = 250,
            max = 5000
    )
    public static int autoSellDelayMs = 750;

    @DropdownBox(
            key = "auto-sell",
            title = "Click Type",
            options = {"Shift Click", "Middle Click", "Left Click"}
    )
    public static int autoSellClickType = 0;

    @ToggleButton(
            key = "auto-sell",
            name = "Include Default Sell List",
            description = "Sell the built-in list of dungeon trash drops",
            category = "Dungeon"
    )
    public static boolean autoSellUseDefaultItems = true;

    @TextInputField(
            key = "auto-sell",
            title = "Custom Items (comma separated)",
            maxLength = 512
    )
    public static String autoSellCustomItems = "";

    @ToggleButton(
            key = "auto-fish",
            name = "Auto Fish",
            description = "Automatically fishes for you.",
            category = "Misc"
    )
    public static boolean enableAutoFish = false;

    @ToggleButton(
            key = "auto-fish",
            name = "Move",
            description = "Allow move when finish fishing",
            category = "Misc"
    )
    public static boolean autoFishEnableMove = true;

    @ToggleButton(
            key = "auto-fish",
            name = "Sneak Move",
            description = "Allow sneak when move",
            category = "Misc"
    )
    public static boolean autoFishEnableSneak = true;

    @ToggleButton(
            key = "auto-fish",
            name = "Always Sneak",
            description = "Allow sneak when fishing",
            category = "Misc"
    )
    public static boolean autoFishAlwaysSneak = true;

    @ToggleButton(
            key = "auto-fish",
            name = "Auto Reset",
            description = "Auto reset status after 20s if no fish hooked",
            category = "Misc"
    )
    public static boolean autoFishEnableAutoRethrow = true;

    @Slider(
            key = "auto-fish",
            title = "Throw Delay (tick)",
            min = 10,
            max = 30
    )
    public static int autoFishThrowDelay = 10;

    @ToggleButton(
            key = "auto-fish",
            name = "Rotate",
            description = "Allow rotate when finish fishing",
            category = "Misc"
    )
    public static boolean autoFishEnableRotate = true;
}