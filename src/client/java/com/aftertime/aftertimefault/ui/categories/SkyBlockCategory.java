package com.aftertime.aftertimefault.ui.categories;

import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.ui.components.ModuleContainer;
import com.aftertime.aftertimefault.ui.components.KeyBindInput;
import com.aftertime.aftertimefault.ui.components.UIComponent;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SkyBlockCategory extends ModCategory {
    private final List<UIComponent> components = new ArrayList<>();
    private ModConfig config;

    public SkyBlockCategory() {
        super("SkyBlock", Text.literal("SkyBlock-related modules"));
    }

    @Override
    public void initialize(ModConfig config) {
        this.config = config;
        components.clear();

        //add setting here

    }

    @Override
    public List<UIComponent> getComponents() {
        return components;
    }
}
