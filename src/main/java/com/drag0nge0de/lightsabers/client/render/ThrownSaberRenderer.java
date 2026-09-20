package com.drag0nge0de.lightsabers.client.render;

import com.drag0nge0de.lightsabers.entity.ThrownLightsaberEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ThrownSaberRenderer extends EntityRenderer<ThrownLightsaberEntity> {
   private final ItemRenderer itemRenderer;

   public ThrownSaberRenderer(Context context) {
      super(context);
      this.itemRenderer = context.getItemRenderer();
   }

   public void render(ThrownLightsaberEntity entity, float entityYaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      ItemStack stack = entity.getSaberStack();
      if (!stack.isEmpty()) {
         matrices.push();
         float spin = ((float)entity.age + tickDelta) * 28.0F;
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(((float)entity.age + tickDelta) * 0.25F) * 12.0F));
         matrices.scale(1.35F, 1.35F, 1.35F);
         this.itemRenderer
            .renderItem(
               stack,
               ModelTransformationMode.FIXED,
               false,
               matrices,
               vertexConsumers,
               15728880,
               OverlayTexture.DEFAULT_UV,
               this.itemRenderer.getModels().getModel(stack)
            );
         matrices.pop();
         super.render(entity, entityYaw, tickDelta, matrices, vertexConsumers, light);
      }
   }

   public Identifier getTexture(ThrownLightsaberEntity entity) {
      return Identifier.ofVanilla("textures/misc/shadow.png");
   }
}
