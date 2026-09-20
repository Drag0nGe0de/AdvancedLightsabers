package com.drag0nge0de.lightsabers.mixin.client;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.drag0nge0de.lightsabers.client.render.ItemRenderers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

   @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V", at = @At("HEAD"))
   private void al$pushRenderEntity(LivingEntity entity, ItemStack stack, ModelTransformationMode mode, boolean leftHanded,
         MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int seed, int overlay, int light, CallbackInfo ci) {
      ItemRenderers.beginEntityRender(entity);
   }

   @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V", at = @At("RETURN"))
   private void al$popRenderEntity(LivingEntity entity, ItemStack stack, ModelTransformationMode mode, boolean leftHanded,
         MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int seed, int overlay, int light, CallbackInfo ci) {
      ItemRenderers.endEntityRender();
   }
}
