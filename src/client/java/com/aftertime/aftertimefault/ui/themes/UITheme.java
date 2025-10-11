package com.aftertime.aftertimefault.ui.themes;

import net.minecraft.util.Identifier;

public class UITheme {
    public static final int BACKGROUND_COLOR = 0xFF000000;
    public static final int HEADER_COLOR = 0xFF1A1A1A;
    public static final int FOOTER_COLOR = 0xFF1A1A1A;
    public static final int ACCENT_COLOR = 0xFF00FF00;
    public static final int TEXT_COLOR = 0xFFFFFFFF;
    public static final int BUTTON_COLOR = 0xFF2D2D2D;
    public static final int BUTTON_HOVER_COLOR = 0xFF3D3D3D;

    // Use Identifier.of() instead of new Identifier()
    public static final Identifier BACKGROUND_TEXTURE = Identifier.of("aftertimefault", "textures/ui/background.png");
}