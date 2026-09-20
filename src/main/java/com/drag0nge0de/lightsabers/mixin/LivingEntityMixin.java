package com.drag0nge0de.lightsabers.mixin;

import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.registry.ALSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("TAIL"))
   private void al$onSwing(Hand hand, boolean bl, CallbackInfo ci) {
      LivingEntity self = (LivingEntity) (Object) this;
      if (self.getWorld().isClient || hand != Hand.MAIN_HAND) {
         return;
      }

      ItemStack held = self.getMainHandStack();
      if (held.getItem() instanceof LightsaberItem && LightsaberItem.getComponent(held).active()) {
         HitResult hit = self.raycast(5.0, 1.0F, true);
         if (!(hit instanceof BlockHitResult)) {
            self.getWorld().playSound(null, self.getBlockPos(), ALSounds.LIGHTSABER_SWING,
                  SoundCategory.PLAYERS, 1.0F, 1.0F);
         }
      }
   }
}
