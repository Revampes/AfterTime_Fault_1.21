package com.aftertime.aftertimefault.ui.categories;

import java.util.HashMap;
import java.util.Map;

public class CategoryManager {
    private static final Map<String, ModCategory> CATEGORIES = new HashMap<>();

    static {
        // Register categories
        CATEGORIES.put("Render", new RenderCategory());
        CATEGORIES.put("SkyBlock", new SkyBlockCategory());
        // Add other categories here
    }

    public static ModCategory getCategory(String name) {
        return CATEGORIES.get(name);
    }

    public static String[] getCategoryNames() {
        return CATEGORIES.keySet().toArray(new String[0]);
    }
}