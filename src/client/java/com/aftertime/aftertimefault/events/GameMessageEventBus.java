package com.aftertime.aftertimefault.events;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class GameMessageEventBus {
    private GameMessageEventBus() {}

    private static final List<BiConsumer<Text, Boolean>> listeners = new ArrayList<>();

    public static void register(BiConsumer<Text, Boolean> l) {
        listeners.add(l);
    }

    public static void fire(Text message, boolean overlay) {
        for (BiConsumer<Text, Boolean> l : listeners) l.accept(message, overlay);
    }
}
