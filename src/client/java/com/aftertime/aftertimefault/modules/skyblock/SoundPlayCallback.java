package com.aftertime.aftertimefault.modules.skyblock;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.sound.SoundInstance;

@FunctionalInterface
public interface SoundPlayCallback {
    Event<SoundPlayCallback> EVENT = EventFactory.createArrayBacked(SoundPlayCallback.class,
            callbacks -> (sound) -> {
                for (SoundPlayCallback callback : callbacks) {
                    callback.onPlaySound(sound);
                }
            });

    void onPlaySound(SoundInstance sound);
}

