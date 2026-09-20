package com.drag0nge0de.lightsabers.mixin;

import com.drag0nge0de.lightsabers.handler.ForceEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
   @Inject(method = "addExperience(I)V", at = @At("TAIL"))
   private void al$onVanillaXp(int experience, CallbackInfo ci) {
      if (experience > 0) {
         ForceEvents.onVanillaXp((ServerPlayerEntity)(Object)this, experience);
      }
   }
}
