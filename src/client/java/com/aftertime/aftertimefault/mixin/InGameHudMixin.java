package com.aftertime.aftertimefault.mixin;

import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.modules.skyblock.AutoFish;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ModConfig.enabledAutoFish && ModConfig.autofishShowTimer) {
            AutoFish.getInstance().renderHudOverlay(context);
        }
    }
}

