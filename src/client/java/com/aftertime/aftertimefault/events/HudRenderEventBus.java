package com.aftertime.aftertimefault.events;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class HudRenderEventBus {
    private HudRenderEventBus() {}

    private static final List<BiConsumer<DrawContext, RenderTickCounter>> listeners = new ArrayList<>();

    public static void register(BiConsumer<DrawContext, RenderTickCounter> l) {
        listeners.add(l);
    }

    public static void fire(DrawContext drawContext, RenderTickCounter tickCounter) {
        for (BiConsumer<DrawContext, RenderTickCounter> l : listeners) l.accept(drawContext, tickCounter);
    }
}
