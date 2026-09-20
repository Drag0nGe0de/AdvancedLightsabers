package com.drag0nge0de.lightsabers.client.render;

import com.drag0nge0de.lightsabers.block.LightsaberStandBlock;
import com.drag0nge0de.lightsabers.block.blockentity.LightsaberStandBlockEntity;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class LightsaberStandRenderer implements BlockEntityRenderer<LightsaberStandBlockEntity> {
   public LightsaberStandRenderer(Context context) {
   }

   public void render(LightsaberStandBlockEntity stand, float tickDelta, MatrixStack matrices,
         VertexConsumerProvider vertexConsumers, int light, int overlay) {
      ItemStack stack = stand.getDisplayStack();
      matrices.push();

      matrices.translate(0.5, 0.5, 0.5);
      matrices.scale(1, -1, -1);
      adjustRotation(matrices, stand);
      BlockEntityRenderers.renderStandParts(matrices, vertexConsumers, light, stack);
      matrices.pop();
   }

   private void adjustRotation(MatrixStack matrices, LightsaberStandBlockEntity stand) {
      BlockFace face = stand.getCachedState().contains(LightsaberStandBlock.FACE)
            ? stand.getCachedState().get(LightsaberStandBlock.FACE) : BlockFace.FLOOR;
      Direction facing = stand.getCachedState().contains(LightsaberStandBlock.FACING)
            ? stand.getCachedState().get(LightsaberStandBlock.FACING) : Direction.NORTH;

      if (face == BlockFace.WALL) {

         float yaw = switch (facing) {
            case NORTH -> 180.0F;
            case SOUTH -> 0.0F;
            case WEST -> 90.0F;
            default -> 270.0F;
         };
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
      } else if (face == BlockFace.CEILING) {

         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
      }

      matrices.translate(0, -1, 0);
   }
}
