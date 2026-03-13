package com.aftertime.aftertimefault.mixin;

import com.aftertime.aftertimefault.events.WorldRenderEventBus;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(CallbackInfo ci) {
        WorldRenderEventBus.fireAfterEntities();
    }
}
