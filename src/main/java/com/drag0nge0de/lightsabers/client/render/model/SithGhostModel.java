package com.drag0nge0de.lightsabers.client.render.model;

import com.drag0nge0de.lightsabers.entity.SithGhostEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public class SithGhostModel extends BipedEntityModel<SithGhostEntity> {

   public SithGhostModel(ModelPart root) {
      super(root);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData modelData = new ModelData();
      ModelPartData root = modelData.getRoot();

      ModelPartData head = root.addChild(EntityModelPartNames.HEAD,
         ModelPartBuilder.create().uv(0, 0).mirrored()
            .cuboid(-4.5F, -8.0F, -4.5F, 9.0F, 9.0F, 1.0F),
         ModelTransform.pivot(0.0F, 0.0F, 0.0F));

      head.addChild("hood",
         ModelPartBuilder.create().uv(0, 27).mirrored()
            .cuboid(-5.5F, -9.4F, -3.6F, 11.0F, 11.0F, 9.0F),
         ModelTransform.of(0.0F, 0.0F, 0.0F, 0.08429940287132612F, 0.0F, 0.0F));

      root.addChild(EntityModelPartNames.BODY,
         ModelPartBuilder.create().uv(22, 10).mirrored()
            .cuboid(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F),
         ModelTransform.pivot(0.0F, 0.0F, 0.0F));

      root.addChild(EntityModelPartNames.RIGHT_ARM,
         ModelPartBuilder.create().uv(48, 10)
            .cuboid(-4.0F, -2.0F, -1.9F, 4.0F, 12.0F, 4.0F),
         ModelTransform.pivot(-4.0F, 2.0F, 0.0F));

      root.addChild(EntityModelPartNames.LEFT_ARM,
         ModelPartBuilder.create().uv(48, 10).mirrored()
            .cuboid(0.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
         ModelTransform.pivot(4.0F, 2.0F, 0.0F));

      root.addChild(EntityModelPartNames.RIGHT_LEG,
         ModelPartBuilder.create().uv(0, 10)
            .cuboid(-2.3F, 0.0F, -2.5F, 6.0F, 12.0F, 5.0F),
         ModelTransform.pivot(-2.0F, 12.0F, 0.0F));

      root.addChild(EntityModelPartNames.LEFT_LEG,
         ModelPartBuilder.create().uv(0, 10).mirrored()
            .cuboid(-3.7F, 0.0F, -2.5F, 6.0F, 12.0F, 5.0F),
         ModelTransform.pivot(2.0F, 12.0F, 0.0F));

      root.addChild(EntityModelPartNames.HAT,
         ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

      return TexturedModelData.of(modelData, 64, 64);
   }

   @Override
   public void setAngles(SithGhostEntity entity, float limbFrequency, float limbDistance,
         float animationProgress, float headYaw, float headPitch) {
      super.setAngles(entity, limbFrequency, limbDistance, animationProgress, headYaw, headPitch);

      this.rightArm.pivotX = -4.0F;
      this.rightArm.pivotY = 2.0F;
      this.leftArm.pivotX = 4.0F;
      this.leftArm.pivotY = 2.0F;

      this.rightLeg.roll = 0.05235987755982988F;
      this.leftLeg.roll = -0.05235987755982988F;
   }
}
