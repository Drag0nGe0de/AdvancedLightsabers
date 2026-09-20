package com.drag0nge0de.lightsabers.mixin;

import com.drag0nge0de.lightsabers.item.PouchInventory;
import com.drag0nge0de.lightsabers.registry.ALComponents;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

   @Shadow
   public abstract ItemStack getStack();

   @Shadow
   public abstract void setStack(ItemStack stack);

   @Shadow
   private int pickupDelay;

   @Inject(method = "onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("HEAD"), cancellable = true)
   private void al$pouchAutoPickup(PlayerEntity player, CallbackInfo ci) {
      if (player.getWorld().isClient || player.isSpectator() || this.pickupDelay > 0) {
         return;
      }

      ItemStack stack = this.getStack();

      if (!PouchInventory.isCrystal(stack)) {
         return;
      }

      PlayerInventory inventory = player.getInventory();

      for (int i = 0; i < inventory.size(); i++) {
         ItemStack candidate = inventory.getStack(i);

         if (!candidate.isEmpty() && candidate.contains(ALComponents.POUCH_CONTENTS)) {
            PouchInventory pouch = new PouchInventory(candidate);
            ItemStack remainder = pouch.insert(stack);

            if (remainder.isEmpty()) {
               this.setStack(ItemStack.EMPTY);
               ((ItemEntity) (Object) this).discard();
               ci.cancel();
               return;
            }

            if (remainder.getCount() < stack.getCount()) {
               this.setStack(remainder);
               ci.cancel();
               return;
            }
         }
      }
   }
}
