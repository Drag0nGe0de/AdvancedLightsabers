package com.drag0nge0de.lightsabers.client;

import com.drag0nge0de.lightsabers.config.ALConfig;
import com.drag0nge0de.lightsabers.item.LightsaberItem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import org.joml.Matrix4f;

public final class SaberAnimations {

   private static final float TP_SCALE = 0.175F;
   private static final float FP_SCALE = 0.2F;

   private SaberAnimations() {
   }

   public static float walkAmount(LivingEntity entity, float tickDelta) {
      return entity.limbAnimator.getSpeed(tickDelta);
   }

   public static float walkPos(LivingEntity entity, float tickDelta) {
      return entity.limbAnimator.getPos(tickDelta);
   }

   public static float swingProgress(LivingEntity entity, float tickDelta) {
      return entity.getHandSwingProgress(tickDelta);
   }

   public static float walkBob(LivingEntity entity, float tickDelta) {
      float walk = walkAmount(entity, tickDelta);
      float pos = walkPos(entity, tickDelta);
      return MathHelper.cos(pos * 0.6662F) * 1.4F * walk;
   }

   public static float swingTriangle(float swing) {
      return (swing > 0.5F ? 1 - swing : swing) * 2.0F;
   }

   public static float tickDelta() {
      return MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
   }

   public static boolean firstPersonEnabled() {
      return ALConfig.get().firstPersonAnimations;
   }

   public static boolean thirdPersonEnabled() {
      return ALConfig.get().thirdPersonAnimations;
   }

   public static boolean isDoubleSaber(ItemStack stack) {
      return stack.getItem() instanceof LightsaberItem && LightsaberItem.getComponent(stack).doubleSaber();
   }

   public static Matrix4f thirdPersonDelta(boolean doubleSaber, LivingEntity entity, float tickDelta) {
      float scale = 1.0F / TP_SCALE;
      Matrix4f delta = new Matrix4f().scale(scale);
      delta.mul(midStaticInverse(doubleSaber));
      delta.mul(midDynamicThirdPerson(doubleSaber, entity, tickDelta));
      delta.scale(TP_SCALE);
      return delta;
   }

   private static Matrix4f midStaticInverse(boolean doubleSaber) {
      Matrix4f inverse = new Matrix4f();

      if (doubleSaber) {
         inverse.rotateX((float) Math.toRadians(5.0F));
         inverse.rotateZ((float) Math.toRadians(-82.0F));
         inverse.translate(-0.15F, 0, 0);
      } else {
         inverse.translate(-0.05F, -0.05F, 0);
         inverse.rotateZ((float) Math.toRadians(10.0F));
         inverse.rotateY((float) Math.toRadians(30.0F));
      }

      return inverse;
   }

   private static Matrix4f midDynamicThirdPerson(boolean doubleSaber, LivingEntity entity, float tickDelta) {
      Matrix4f dynamic = new Matrix4f();
      float walk = walkAmount(entity, tickDelta);
      float swing = swingProgress(entity, tickDelta);
      float still = 1.0F - walk;

      if (doubleSaber) {
         float bob = walkBob(entity, tickDelta);
         dynamic.rotateX((float) Math.toRadians(-10.0F * bob * walk));
         dynamic.translate(0.15F * still, 0, 0);
         dynamic.rotateZ((float) Math.toRadians(82.0F * still));
         dynamic.rotateX((float) Math.toRadians(-5.0F * still));
         dynamic.rotateZ((float) Math.toRadians(-360.0F * swing));
      } else {
         if (entity.isSneaking()) {
            return dynamic;
         }

         dynamic.rotateY((float) Math.toRadians(-30.0F * still));
         dynamic.rotateZ((float) Math.toRadians(-10.0F * still));
         dynamic.translate(0.05F * still, 0.05F * still, 0);
         float triangle = swingTriangle(swing);
         dynamic.rotateZ((float) Math.toRadians(-140.0F * triangle));
         dynamic.rotateX((float) Math.toRadians(-80.0F * triangle));
      }

      return dynamic;
   }

   public static Matrix4f firstPersonDoubleDelta(LivingEntity entity, float tickDelta) {
      Matrix4f dynamic = new Matrix4f();
      float walk = walkAmount(entity, tickDelta);
      float swing = swingProgress(entity, tickDelta);
      float triangle = swingTriangle(swing);

      dynamic.rotateZ((float) Math.toRadians(90.0F * walk));
      dynamic.translate(0.2F * triangle + 0.8F * walk, 0.5F * triangle, 0.4F * walk);
      dynamic.rotateX((float) Math.toRadians(30.0F * triangle));
      dynamic.rotateZ((float) Math.toRadians(360.0F * swing));

      Matrix4f delta = new Matrix4f().scale(1.0F / FP_SCALE);
      delta.mul(dynamic);
      delta.scale(FP_SCALE);
      return delta;
   }

   public static boolean entityHoldsSaber(LivingEntity entity) {
      return !entity.getMainHandStack().isEmpty() && entity.getMainHandStack().getItem() instanceof LightsaberItem;
   }

   public static void dumpAnimState(boolean doubleSaber, LivingEntity entity, float tickDelta) {
      float walk = walkAmount(entity, tickDelta);
      float swing = swingProgress(entity, tickDelta);
      float triangle = swingTriangle(swing);
      System.out.println("[AL-ANIM] double=" + doubleSaber
              + " walk=" + walk
              + " swing=" + swing
              + " triangle=" + triangle
              + " handSwinging=" + entity.handSwinging
              + " handSwingTicks=" + entity.handSwingTicks
              + " still=" + (1.0F - walk));
   }
}
