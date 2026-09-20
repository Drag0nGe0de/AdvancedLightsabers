package com.drag0nge0de.lightsabers.mixin.client;

import com.drag0nge0de.lightsabers.client.SaberAnimations;
import com.drag0nge0de.lightsabers.config.ALConfig;
import com.drag0nge0de.lightsabers.item.LightsaberItem;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {

   @Shadow public ModelPart rightArm;

   @Shadow public ModelPart leftArm;

   @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
   private void al$saberGuardPose(LivingEntity entity, float limbAngle, float limbDistance, float ageInTicks,
         float headYaw, float headPitch, CallbackInfo ci) {
      if (!ALConfig.get().thirdPersonAnimations) {
         return;
      }

      ItemStack held = entity.getMainHandStack();
      if (held.isEmpty() || !(held.getItem() instanceof LightsaberItem) || entity.isSneaking()) {
         return;
      }

      boolean doubleSaber = LightsaberItem.getComponent(held).doubleSaber();
      float swing = entity.getHandSwingProgress(ageInTicks - entity.age);
      float triangle = SaberAnimations.swingTriangle(swing);

      if (doubleSaber) {
         float still = 1.0F - limbDistance;
         this.rightArm.pitch = this.rightArm.pitch * limbDistance - ((float) Math.PI / 2.5F) * still;
         this.rightArm.yaw = this.rightArm.yaw * limbDistance + (this.rightArm.yaw * 0.5F - (float) Math.PI / 15.0F) * still;
         this.rightArm.pitch -= triangle;
         this.rightArm.yaw += triangle;
      } else {
         float still = 1.0F - limbDistance;
         this.rightArm.pitch = this.rightArm.pitch * limbDistance - still;
         this.rightArm.yaw = this.rightArm.yaw * limbDistance + (this.rightArm.yaw * 0.5F - 0.5F) * still;
         this.rightArm.roll = this.rightArm.roll * limbDistance + (this.rightArm.roll * 0.5F - 0.2F) * still;
         this.rightArm.pivotX += 0.5F * still;

         float partial = limbDistance > 0.5F ? 1.0F : limbDistance * 2.0F;
         float rest = 1.0F - partial;
         this.leftArm.pitch = this.leftArm.pitch * partial - rest;
         this.leftArm.yaw = this.leftArm.yaw * partial + (this.leftArm.yaw * 0.5F + 0.75F) * rest;
         this.leftArm.roll = this.leftArm.roll * partial + this.leftArm.roll * 0.5F * rest;
         this.leftArm.pivotX -= 0.5F * still;

         this.rightArm.pitch -= triangle;
         this.rightArm.yaw -= triangle * 0.4F;
         this.leftArm.pitch -= triangle * 1.6F;
         this.leftArm.yaw -= triangle * 0.9F;
      }
   }
}
