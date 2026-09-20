package com.drag0nge0de.lightsabers.client.render;

import com.drag0nge0de.lightsabers.client.render.model.HiltModels;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class HippedSaberFeature extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

   public HippedSaberFeature(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
      super(context);
   }

   @Override
   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
         AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta,
         float animationProgress, float headYaw, float headPitch) {
      ItemStack held = player.getMainHandStack();
      if (held.getItem() instanceof LightsaberItem) {
         return;
      }

      ItemStack saber = null;
      for (ItemStack stack : player.getInventory().main) {
         if (!stack.isEmpty() && stack.getItem() instanceof LightsaberItem
               && !LightsaberItem.getComponent(stack).doubleSaber()) {
            saber = stack;
            break;
         }
      }

      if (saber == null) {
         return;
      }

      String hilt = LightsaberItem.getComponent(saber).hilt();
      matrices.push();

      this.getContextModel().body.rotate(matrices);

      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
      matrices.translate(0.2F, -0.55F, 0.15F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(15));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10));
      matrices.scale(0.15F, 0.15F, 0.15F);
      matrices.translate(0, -(HiltModels.stats(hilt).bodyH() + HiltModels.stats(hilt).pommelH() / 2) * 0.0625F, 0);

      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
      HiltRenderer.renderHilt(matrices, vertexConsumers, hilt, light,
            net.minecraft.client.render.OverlayTexture.DEFAULT_UV);
      matrices.pop();
   }
}
