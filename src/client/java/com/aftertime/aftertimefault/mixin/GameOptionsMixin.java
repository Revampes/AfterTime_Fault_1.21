package com.aftertime.aftertimefault.mixin;

import com.aftertime.aftertimefault.KeyBind.KeybindHandler;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

    @Mutable
    @Shadow
    public KeyBinding[] allKeys;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void registerCustomKeybindings(CallbackInfo ci) {
        int len = allKeys.length;
        allKeys = Arrays.copyOf(allKeys, len + 1);
        allKeys[len] = KeybindHandler.CONFIG_GUI_KEY;
    }
}
