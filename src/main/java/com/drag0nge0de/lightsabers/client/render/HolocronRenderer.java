package com.drag0nge0de.lightsabers.client.render;

import com.drag0nge0de.lightsabers.block.blockentity.HolocronBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class HolocronRenderer implements BlockEntityRenderer<HolocronBlockEntity> {
   public HolocronRenderer(Context context) {
   }

   public void render(HolocronBlockEntity holocron, float tickDelta, MatrixStack matrices,
         VertexConsumerProvider vertexConsumers, int light, int overlay) {
      float open = MathHelper.lerp(tickDelta, holocron.prevOpenTimer, holocron.openTimer);
      int openTicks = (int) MathHelper.lerp(tickDelta, (float) holocron.prevOpenTicks, (float) holocron.openTicks);
      matrices.push();
      matrices.translate(0.5, 0.0, 0.5);
      HolocronGeometry.draw(matrices, vertexConsumers, holocron.isSith(), open, openTicks);
      matrices.pop();
   }
}
