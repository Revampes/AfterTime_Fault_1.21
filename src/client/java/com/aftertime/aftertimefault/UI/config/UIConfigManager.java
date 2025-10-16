package com.aftertime.aftertimefault.UI.config;

import com.aftertime.aftertimefault.UI.annotations.*;
import com.aftertime.aftertimefault.UI.categories.CategoryPanel;
import com.aftertime.aftertimefault.UI.categories.ModulePanel;
import com.aftertime.aftertimefault.UI.config.ModConfigIO;
import com.aftertime.aftertimefault.config.ModConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class UIConfigManager {

    public static Map<String, CategoryPanel> createUICategories() {
        // Define your desired category order here
        List<String> categoryOrder = Arrays.asList(
                "Render", "Dungeon", "SkyBlock", "Kuudra", "Combat", "Layout", "General", "Visual", "Controls"
        );
        Map<String, CategoryPanel> categories = new LinkedHashMap<>();

        // Get all fields from ModConfig class
        Field[] fields = ModConfig.class.getDeclaredFields();

        // Group fields by module key
        Map<String, List<Field>> moduleFields = new HashMap<>();

        for (Field field : fields) {
            // Process ToggleButton annotations (main modules)
            ToggleButton toggleAnnotation = field.getAnnotation(ToggleButton.class);
            if (toggleAnnotation != null) {
                String key = toggleAnnotation.key();
                moduleFields.putIfAbsent(key, new ArrayList<>());
                moduleFields.get(key).add(field);
            }

            // Process other annotations (sub-settings)
            processAnnotation(field, CheckBox.class, moduleFields);
            processAnnotation(field, Slider.class, moduleFields);
            processAnnotation(field, KeyBindInput.class, moduleFields);
            processAnnotation(field, ColorPicker.class, moduleFields);
            processAnnotation(field, DropdownBox.class, moduleFields);
            processAnnotation(field, TextInputField.class, moduleFields);
            processAnnotation(field, NormalButton.class, moduleFields);
            // New: process UILayout annotation
            processAnnotation(field, UILayout.class, moduleFields);
        }

        // Create UI panels from annotated fields
        for (Map.Entry<String, List<Field>> entry : moduleFields.entrySet()) {
            List<Field> fieldsForModule = entry.getValue();

            // Find the main toggle button for this module
            Field toggleField = fieldsForModule.stream()
                    .filter(f -> f.getAnnotation(ToggleButton.class) != null)
                    .findFirst()
                    .orElse(null);

            if (toggleField != null) {
                ToggleButton toggleAnnotation = toggleField.getAnnotation(ToggleButton.class);
                String categoryName = toggleAnnotation.category();

                // Get or create category panel
                CategoryPanel category = categories.get(categoryName);
                if (category == null) {
                    category = new CategoryPanel(categoryName, 0, 0, 200, 400);
                    categories.put(categoryName, category);
                }

                // Create module panel
                try {
                    toggleField.setAccessible(true);
                    boolean initialValue = toggleField.getBoolean(null);
                    ModulePanel modulePanel = new ModulePanel(
                            toggleAnnotation.name(),
                            toggleAnnotation.description(),
                            0, 0, 180,
                            initialValue
                    );

                    // Bind toggle to field
                    modulePanel.getToggleButton().setOnToggle(() -> {
                        try {
                            toggleField.setBoolean(null, modulePanel.isEnabled());
                            ModConfigIO.save(); // persist + notify modules
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });

                    // Add sub-settings
                    for (Field subField : fieldsForModule) {
                        if (subField == toggleField) continue;
                        addSubSetting(modulePanel, subField);
                    }

                    category.addModule(modulePanel);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // --- Custom ordering logic ---
        Map<String, CategoryPanel> orderedCategories = new LinkedHashMap<>();
        for (String cat : categoryOrder) {
            if (categories.containsKey(cat)) {
                orderedCategories.put(cat, categories.get(cat));
            }
        }
        // Add any categories not in the list at the end
        for (Map.Entry<String, CategoryPanel> entry2 : categories.entrySet()) {
            if (!orderedCategories.containsKey(entry2.getKey())) {
                orderedCategories.put(entry2.getKey(), entry2.getValue());
            }
        }
        return orderedCategories;
    }

    private static <T extends java.lang.annotation.Annotation>
    void processAnnotation(Field field, Class<T> annotationClass, Map<String, List<Field>> moduleFields) {
        T annotation = field.getAnnotation(annotationClass);
        if (annotation != null) {
            try {
                // Get the key method from the annotation
                String key = (String) annotation.getClass().getMethod("key").invoke(annotation);
                moduleFields.putIfAbsent(key, new ArrayList<>());
                moduleFields.get(key).add(field);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void addSubSetting(ModulePanel modulePanel, Field field) {
        try {
            field.setAccessible(true);

            // Handle different annotation types
            CheckBox checkBox = field.getAnnotation(CheckBox.class);
            if (checkBox != null) {
                boolean current = field.getBoolean(null);
                modulePanel.addCheckBox(checkBox.title(), current, () -> {
                    try {
                        // Toggle boolean value in config
                        boolean now = field.getBoolean(null);
                        field.setBoolean(null, !now);
                        ModConfigIO.save();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            Slider slider = field.getAnnotation(Slider.class);
            if (slider != null) {
                Object obj = field.get(null);
                float current;
                if (obj instanceof Number) {
                    current = ((Number) obj).floatValue();
                } else {
                    try { current = Float.parseFloat(String.valueOf(obj)); }
                    catch (Exception e) { current = slider.min(); }
                }
                String annTitle;
                try {
                    annTitle = slider.title();
                } catch (Throwable t) {
                    annTitle = ""; // in case of mismatch
                }
                String label = (annTitle != null && !annTitle.trim().isEmpty())
                        ? annTitle
                        : prettifyName(field.getName());
                modulePanel.addSlider(label, slider.min(), slider.max(), current, (val) -> {
                    try {
                        Class<?> type = field.getType();
                        if (type == int.class || type == Integer.class) {
                            field.setInt(null, Math.round(val));
                        } else if (type == float.class || type == Float.class) {
                            field.setFloat(null, val);
                        } else if (type == double.class || type == Double.class) {
                            field.setDouble(null, val);
                        } else if (type == long.class || type == Long.class) {
                            field.setLong(null, (long) val.floatValue());
                        } else {
                            // Fallback: store as int
                            field.set(null, Math.round(val));
                        }
                        ModConfigIO.save();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            KeyBindInput keyBind = field.getAnnotation(KeyBindInput.class);
            if (keyBind != null) {
                String current = String.valueOf(field.get(null));
                com.aftertime.aftertimefault.UI.elements.KeyBindInput element = modulePanel.addKeyBindInputReturn(keyBind.title(), current, null);
                // Attach callback after creation to avoid forward reference issues
                element.setOnChange(() -> {
                    try {
                        if (field.getType() == String.class) {
                            field.set(null, element.getKeyName());
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            ColorPicker colorPicker = field.getAnnotation(ColorPicker.class);
            if (colorPicker != null) {
                int current = field.getInt(null);
                java.awt.Color initialColor = new java.awt.Color(current, true);
                com.aftertime.aftertimefault.UI.elements.ColorPicker element = modulePanel.addColorPickerReturn(colorPicker.title(), initialColor, null);
                element.setOnChange(() -> {
                    try {
                        field.setInt(null, element.getColor().getRGB());
                        ModConfigIO.save();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            DropdownBox dropdown = field.getAnnotation(DropdownBox.class);
            if (dropdown != null) {
                String[] optionsArr = dropdown.options();
                String[] options = (optionsArr == null || optionsArr.length == 0) ? new String[]{""} : optionsArr;
                int current = field.getInt(null);
                com.aftertime.aftertimefault.UI.elements.DropdownBox element = modulePanel.addDropdownReturn(dropdown.title(), options, current, null);
                element.setOnChange(() -> {
                    try {
                        field.setInt(null, element.getSelectedIndex());
                        ModConfigIO.save();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            TextInputField textInput = field.getAnnotation(TextInputField.class);
            if (textInput != null) {
                String current = String.valueOf(field.get(null));
                final com.aftertime.aftertimefault.UI.elements.TextInputField[] ref = new com.aftertime.aftertimefault.UI.elements.TextInputField[1];
                Runnable onChange = () -> {
                    try {
                        if (field.getType() == String.class && ref[0] != null) {
                            field.set(null, ref[0].getText());
                            ModConfigIO.save();
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                };
                com.aftertime.aftertimefault.UI.elements.TextInputField element = modulePanel.addTextInputReturn(textInput.title(), textInput.maxLength(), onChange);
                ref[0] = element;
                element.setText(current == null ? "" : current);
                return;
            }

            NormalButton normalButton = field.getAnnotation(NormalButton.class);
            if (normalButton != null) {
                String action = normalButton.action();
                modulePanel.addNormalButton(normalButton.title(), () -> invokeAction(action));
                return;
            }

            // New: UILayout grouped controls backed by a single String field value "x,y,scale"
            UILayout uiLayout = field.getAnnotation(UILayout.class);
            if (uiLayout != null) {
                String stored = String.valueOf(field.get(null));
                int defX = uiLayout.posx();
                int defY = uiLayout.posy();
                float defScale = uiLayout.scale();
                int[] parsedXY = new int[]{defX, defY};
                float[] parsedScale = new float[]{defScale};
                parseLayout(stored, parsedXY, parsedScale);

                // X control
                modulePanel.addSlider(uiLayout.title() + " X", -500, 500, parsedXY[0], (val) -> {
                    try {
                        parsedXY[0] = Math.round(val);
                        field.set(null, layoutString(parsedXY[0], parsedXY[1], parsedScale[0]));
                        ModConfigIO.save();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                // Y control
                modulePanel.addSlider(uiLayout.title() + " Y", -500, 500, parsedXY[1], (val) -> {
                    try {
                        parsedXY[1] = Math.round(val);
                        field.set(null, layoutString(parsedXY[0], parsedXY[1], parsedScale[0]));
                        ModConfigIO.save();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                // Scale control
                modulePanel.addSlider(uiLayout.title() + " Scale", 0.5f, 3.0f, parsedScale[0], (val) -> {
                    try {
                        parsedScale[0] = Math.max(0.1f, Math.min(5.0f, val));
                        field.set(null, layoutString(parsedXY[0], parsedXY[1], parsedScale[0]));
                        ModConfigIO.save();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                // Optional: Reset button
                modulePanel.addNormalButton("Reset " + uiLayout.title(), () -> {
                    try {
                        parsedXY[0] = defX;
                        parsedXY[1] = defY;
                        parsedScale[0] = defScale;
                        field.set(null, layoutString(parsedXY[0], parsedXY[1], parsedScale[0]));
                        ModConfigIO.save();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            // Add handling for other annotation types...

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void invokeAction(String action) {
        if (action == null || action.isEmpty()) return;
        try {
            if (action.contains("#")) {
                String[] parts = action.split("#", 2);
                Class<?> cls = Class.forName(parts[0]);
                Method m = cls.getDeclaredMethod(parts[1]);
                m.setAccessible(true);
                m.invoke(null);
                return;
            }
            // Simple built-ins
            switch (action.toLowerCase()) {
                case "save":
                case "saveconfig":
                    ModConfigIO.save();
                    break;
                case "reload":
                case "reloadconfig":
                    ModConfigIO.load();
                    break;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static String prettifyName(String raw) {
        // Convert camelCase or snake_case to Title Case
        String spaced = raw.replace('_', ' ')
                .replaceAll("(?<!^)([A-Z])", " $1");
        String[] parts = spaced.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1).toLowerCase());
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    // Helpers for UILayout string parsing
    private static void parseLayout(String stored, int[] xyOut, float[] scaleOut) {
        if (stored == null) return;
        try {
            String[] parts = stored.split(",");
            if (parts.length >= 2) {
                xyOut[0] = Integer.parseInt(parts[0].trim());
                xyOut[1] = Integer.parseInt(parts[1].trim());
            }
            if (parts.length >= 3) {
                scaleOut[0] = Float.parseFloat(parts[2].trim());
            }
        } catch (Exception ignored) {}
    }

    private static String layoutString(int x, int y, float s) {
        return x + "," + y + "," + (Math.round(s * 100f) / 100f);
    }
}
