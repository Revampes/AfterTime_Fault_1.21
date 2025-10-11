package com.aftertime.aftertimefault.ui.categories;

import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.ui.components.UIComponent;
import net.minecraft.text.Text;

import java.util.List;

public abstract class ModCategory {
    protected final String name;
    protected final Text displayName;
    protected final Text description;

    public ModCategory(String name, Text description) {
        this.name = name;
        this.displayName = Text.literal(name);
        this.description = description;
    }

    /**
     * Initialize the category with components
     * @param config The mod configuration
     */
    public abstract void initialize(ModConfig config);

    /**
     * Get all UI components for this category
     * @return List of UI components
     */
    public abstract List<UIComponent> getComponents();

    /**
     * Called when the category is opened
     */
    public void onOpen() {
        // Optional: Refresh components when category is opened
    }

    /**
     * Called when the category is closed
     */
    public void onClose() {
        // Optional: Save settings or clean up
    }

    // Getters
    public String getName() {
        return name;
    }

    public Text getDisplayName() {
        return displayName;
    }

    public Text getDescription() {
        return description;
    }
}
