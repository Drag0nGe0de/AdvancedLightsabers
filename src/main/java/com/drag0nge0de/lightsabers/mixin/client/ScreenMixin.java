package com.drag0nge0de.lightsabers.mixin.client;

import net.minecraft.client.gui.screen.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
    private void al$skipBlur(float delta, CallbackInfo ci) {
        if (Boolean.getBoolean("al.shots")) {
            ci.cancel();
        }
    }
}
