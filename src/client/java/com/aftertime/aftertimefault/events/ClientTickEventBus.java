package com.aftertime.aftertimefault.events;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ClientTickEventBus {
    private ClientTickEventBus() {}

    private static final List<Consumer<MinecraftClient>> listeners = new ArrayList<>();

    public static void register(Consumer<MinecraftClient> l) {
        listeners.add(l);
    }

    public static void fire(MinecraftClient client) {
        for (Consumer<MinecraftClient> l : listeners) l.accept(client);
    }
}
