package com.drag0nge0de.lightsabers.client.model;

import com.drag0nge0de.lightsabers.AL;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public final class PartModels {
   public static final Identifier WHITE = AL.id("textures/misc/white.png");

   private static final Map<String, ModelPart> CACHE = new ConcurrentHashMap<>();
   private static final Set<Direction> ALL_FACES = Set.of(
      Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
   );

   public static ModelPart saberPart(String partType, String hiltId) {
      return CACHE.computeIfAbsent(partType + ":" + hiltId, PartModels::build);
   }

   public static ModelPart crystal() {
      return CACHE.computeIfAbsent("crystal", PartModels::build);
   }

   public static ModelPart blade(float inflate) {
      return CACHE.computeIfAbsent("blade:" + inflate, k -> build(new SaberModelData.PartDef(
         "", 64.0F, 32.0F,
         List.of(new SaberModelData.Box(-0.5F, 0.0F, -0.5F, -38.0F, -0.5F, 1.0F, 38.0F, 1.0F, inflate, false, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F))
      )));
   }

   private static ModelPart build(String key) {
      return build(SaberModelData.PARTS.get(key));
   }

   private static ModelPart build(SaberModelData.PartDef def) {
      Map<String, ModelPart> children = new java.util.LinkedHashMap<>();
      int index = 0;
      for (SaberModelData.Box b : def.boxes()) {
         ModelPart.Cuboid cuboid = new ModelPart.Cuboid(
            (int)b.u(), (int)b.v(),
            b.x(), b.y(), b.z(),
            b.w(), b.h(), b.d(),
            b.inflate(), b.inflate(), b.inflate(),
            b.mirror(),
            def.texW(), def.texH(),
            ALL_FACES
         );
         ModelPart part = new ModelPart(List.of(cuboid), Map.of());
         part.setPivot(b.px(), b.py(), b.pz());
         part.setAngles(b.rx(), b.ry(), b.rz());
         children.put("b" + index++, part);
      }

      return new ModelPart(List.of(), children);
   }

   public static void render(
      ModelPart root,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      Identifier texture,
      boolean translucent,
      int light,
      int overlay,
      float r,
      float g,
      float b,
      float a
   ) {
      VertexConsumer vc = vertexConsumers.getBuffer(
         translucent ? RenderLayer.getEntityCutoutNoCull(texture) : RenderLayer.getEntityCutoutNoCull(texture)
      );
      int color = ((int)(a * 255.0F) & 0xFF) << 24 | ((int)(r * 255.0F) & 0xFF) << 16 | ((int)(g * 255.0F) & 0xFF) << 8 | (int)(b * 255.0F) & 0xFF;
      root.render(matrices, vc, light, overlay, color);
   }

   private PartModels() {
   }
}
