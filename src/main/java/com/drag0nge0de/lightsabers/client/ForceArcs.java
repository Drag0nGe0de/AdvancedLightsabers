package com.drag0nge0de.lightsabers.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class ForceArcs {

   public static final String LIGHTNING = "lightning";
   public static final String DRAIN = "drain";

   private static final Map<Integer, Map<String, int[]>> ENTITY_EFFECTS = new HashMap<>();

   private ForceArcs() {
   }

   public static void updateEntity(int entityId, Map<String, int[]> effects) {
      if (effects.isEmpty()) {
         ENTITY_EFFECTS.remove(entityId);
      } else {
         ENTITY_EFFECTS.put(entityId, new HashMap<>(effects));
      }
   }

   public static void clear() {
      ENTITY_EFFECTS.clear();
   }

   public static int[] getEffect(Entity entity, String id) {
      Map<String, int[]> map = ENTITY_EFFECTS.get(entity.getId());
      return map != null ? map.get(id) : null;
   }

   public static void register() {
      WorldRenderEvents.AFTER_ENTITIES.register(ForceArcs::render);
   }

   private static void render(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientWorld world = client.world;

      if (world == null || ENTITY_EFFECTS.isEmpty()) {
         return;
      }

      Vec3d camera = context.camera().getPos();
      float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
      VertexConsumer buffer = context.consumers() == null
         ? null
         : context.consumers().getBuffer(RenderLayer.getDebugLineStrip(1.0));

      if (buffer == null) {
         return;
      }

      List<LivingEntity> casters = new ArrayList<>();

      for (Integer id : ENTITY_EFFECTS.keySet()) {
         Entity entity = world.getEntityById(id);

         if (entity instanceof LivingEntity living && living.isAlive()) {
            casters.add(living);
         } else {
            ENTITY_EFFECTS.remove(id);
         }
      }

      for (LivingEntity caster : casters) {
         int[] lightning = getEffect(caster, LIGHTNING);

         if (lightning != null) {
            LivingEntity target = findArcTarget(caster, 7.0);
            Vec3d color = new Vec3d(0, 0, 1);
            Random rand = new Random(caster.age * 100000L);

            for (int hand = 0; hand < 2; hand++) {
               for (int j = 0; j < 2 + lightning[0]; j++) {
                  renderBolt(caster, target, hand, color, rand, 1.5F + lightning[0] * 0.5F, tickDelta, camera, buffer, false);
               }
            }
         }

         int[] drain = getEffect(caster, DRAIN);

         if (drain != null) {
            Random rand = new Random(caster.age * 100000L);
            Vec3d color = new Vec3d(1, 0.4, 0);

            for (LivingEntity target : casters) {
               if (target != caster && casterDrains(target, caster)) {
                  renderBolt(caster, target, 0, color, rand, 1.0F, tickDelta, camera, buffer, true);
               }
            }
         }
      }
   }

   private static boolean casterDrains(LivingEntity target, LivingEntity caster) {
      Map<String, int[]> map = ENTITY_EFFECTS.get(target.getId());

      if (map == null) {
         return false;
      }

      int[] drain = map.get(DRAIN);
      return drain != null && drain.length > 2 && drain[2] == caster.getId();
   }

   private static LivingEntity findArcTarget(LivingEntity caster, double range) {
      MinecraftClient client = MinecraftClient.getInstance();
      Vec3d start = caster.getCameraPosVec(1.0F);
      Vec3d look = caster.getRotationVec(1.0F);
      Vec3d end = start.add(look.multiply(range));

      BlockHitResult blockHit = caster.getWorld().raycast(new RaycastContext(start, end,
         RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster));
      double maxDist = blockHit.getType() == HitResult.Type.MISS ? range : blockHit.getPos().distanceTo(start);

      LivingEntity target = null;
      double best = Double.MAX_VALUE;

      for (Entity entity : caster.getWorld().getOtherEntities(caster, caster.getBoundingBox().stretch(look.multiply(range)).expand(1.2))) {
         if (entity instanceof LivingEntity living && living.isAlive() && living != client.player) {
            Vec3d toEntity = entity.getPos().add(0, entity.getHeight() * 0.5, 0).subtract(start);
            double proj = toEntity.dotProduct(look);

            if (proj > 0 && proj < maxDist) {
               Vec3d perp = toEntity.subtract(look.multiply(proj));

               if (perp.lengthSquared() < 1.2 * 1.2 && proj < best) {
                  target = living;
                  best = proj;
               }
            }
         }
      }

      return target;
   }

   private static void renderBolt(LivingEntity caster, LivingEntity target, int hand, Vec3d color, Random rand,
         float spreadFactor, float tickDelta, Vec3d camera, VertexConsumer buffer, boolean single) {
      Vec3d casterPos = caster.getLerpedPos(tickDelta);
      double handSide = hand == 0 ? -0.275 : 0.275;

      float pitch = caster.getPitch(tickDelta);
      float yaw = caster.getYaw(tickDelta);

      Vec3d srcLocal = new Vec3d(handSide, -0.25, 0.8)
         .rotateX((float) (-pitch * Math.PI / 180.0))
         .rotateY((float) (-yaw * Math.PI / 180.0));
      Vec3d src = casterPos.add(0, caster.getHeight() * 0.8, 0).add(srcLocal);

      Vec3d dst;
      Vec3d center;

      if (target != null) {
         center = target.getLerpedPos(tickDelta).add(0, target.getHeight() / 2, 0);
      } else {
         Vec3d start = caster.getCameraPosVec(tickDelta);
         Vec3d look = caster.getRotationVec(tickDelta);
         BlockHitResult hit = caster.getWorld().raycast(new RaycastContext(start, start.add(look.multiply(7.0)),
            RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster));
         center = hit.getType() == HitResult.Type.MISS ? start.add(look.multiply(7.0)) : hit.getPos();
      }

      dst = center.subtract(src);

      boolean firstPerson = caster == MinecraftClient.getInstance().player
         && MinecraftClient.getInstance().options.getPerspective().isFirstPerson();
      double amount = Math.min(src.distanceTo(center) * 0.05, 1.0) * (firstPerson ? 0.75 : 1.0);
      double srcSpread = Math.min(firstPerson ? 0.05 : 0.15, amount);
      double dstSpread = Math.min(0.2, amount) * spreadFactor;
      double d0 = 0.33;
      double d1 = 0.66;

      Vec3d dst1 = dst.multiply(d0).add(jitter(rand, amount));
      Vec3d dst2 = dst.multiply(d1).add(jitter(rand, amount));
      Vec3d dstEnd = dst.add(jitter(rand, dstSpread * 0.125));

      dst1 = dst1.add(jitter(rand, dstSpread * d0));
      dst2 = dst2.add(jitter(rand, dstSpread * d1));

      Vec3d mid1 = src.add(dst1);
      Vec3d mid2 = src.add(dst2);
      Vec3d end = src.add(dstEnd);

      drawLine(buffer, camera, src, mid1, color);
      drawLine(buffer, camera, mid1, mid2, color);
      drawLine(buffer, camera, mid2, end, color);
      drawLine(buffer, camera, src, mid1, new Vec3d(1, 1, 1));
      drawLine(buffer, camera, mid1, mid2, new Vec3d(1, 1, 1));
      drawLine(buffer, camera, mid2, end, new Vec3d(1, 1, 1));
   }

   private static Vec3d jitter(Random rand, double amount) {
      return new Vec3d(
         (rand.nextDouble() * 2 - 1) * amount,
         (rand.nextDouble() * 2 - 1) * amount,
         (rand.nextDouble() * 2 - 1) * amount
      );
   }

   private static void drawLine(VertexConsumer buffer, Vec3d camera, Vec3d a, Vec3d b, Vec3d color) {
      buffer.vertex((float) (a.x - camera.x), (float) (a.y - camera.y), (float) (a.z - camera.z))
         .color((float) color.x, (float) color.y, (float) color.z, 0.9F);
      buffer.vertex((float) (b.x - camera.x), (float) (b.y - camera.y), (float) (b.z - camera.z))
         .color((float) color.x, (float) color.y, (float) color.z, 0.9F);
   }

   public static boolean hasLocalEffect(String id) {
      PlayerEntity player = MinecraftClient.getInstance().player;
      return player != null && getEffect(player, id) != null;
   }
}
