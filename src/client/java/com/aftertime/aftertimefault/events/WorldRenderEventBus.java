package com.aftertime.aftertimefault.events;

import java.util.ArrayList;
import java.util.List;

public final class WorldRenderEventBus {
    private WorldRenderEventBus() {}

    private static final List<Runnable> afterEntities = new ArrayList<>();

    public static void registerAfterEntities(Runnable r) {
        afterEntities.add(r);
    }

    public static void fireAfterEntities() {
        for (Runnable r : afterEntities) r.run();
    }
}
