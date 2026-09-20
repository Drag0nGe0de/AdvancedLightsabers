package com.drag0nge0de.lightsabers.mixin.client;

import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

   @Shadow
   public HitResult crosshairTarget;

   @Shadow
   public ClientPlayerEntity player;

   @Inject(method = "doAttack", at = @At("HEAD"))
   private void lightsabers$swingSound(CallbackInfoReturnable<Boolean> cir) {
      MinecraftClient self = (MinecraftClient) (Object) this;

      if (self.crosshairTarget != null && self.crosshairTarget.getType() != HitResult.Type.BLOCK
            && self.player != null
            && self.player.getMainHandStack().getItem() instanceof LightsaberItem
            && LightsaberItem.getComponent(self.player.getMainHandStack()).active()) {
         net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new ALNetwork.SwingSaberPayload());
      }
   }
}
