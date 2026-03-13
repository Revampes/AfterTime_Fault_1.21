package com.aftertime.aftertimefault.events;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class EntityLoadEventBus {
    private EntityLoadEventBus() {}

    private static final List<BiConsumer<Entity, World>> listeners = new ArrayList<>();

    public static void register(BiConsumer<Entity, World> l) {
        listeners.add(l);
    }

    public static void fire(Entity entity, World world) {
        for (BiConsumer<Entity, World> l : listeners) l.accept(entity, world);
    }
}
