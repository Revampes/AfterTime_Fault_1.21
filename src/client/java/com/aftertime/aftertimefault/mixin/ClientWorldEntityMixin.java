package com.aftertime.aftertimefault.mixin;

import com.aftertime.aftertimefault.events.EntityLoadEventBus;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldEntityMixin {

    @Inject(method = "addEntity", at = @At("HEAD"))
    private void onEntityLoad(Entity entity, CallbackInfo ci) {
        EntityLoadEventBus.fire(entity, (ClientWorld)(Object)this);
    }
}
