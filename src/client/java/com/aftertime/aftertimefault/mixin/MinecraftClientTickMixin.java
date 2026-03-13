package com.aftertime.aftertimefault.mixin;

import com.aftertime.aftertimefault.events.ClientTickEventBus;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onEndTick(CallbackInfo ci) {
        ClientTickEventBus.fire((MinecraftClient)(Object)this);
    }
}
