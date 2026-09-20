package com.drag0nge0de.lightsabers.force;

import com.drag0nge0de.lightsabers.network.ALNetwork;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ForceEffects {
   public static final String FORTIFY = "fortify";
   public static final String STUN = "stun";
   public static final String DRAIN = "drain";
   public static final String LIGHTNING = "lightning";
   public static final String CHOKE = "choke";
   public static final String STEALTH = "stealth";
   public static final String SPEED = "speed";
   public static final String GAZE = "gaze";
   public static final String MEDITATION = "meditation";
   public static final String RESIST = "resist";

   private static final Map<UUID, List<Active>> EFFECTS = new HashMap<>();

   public static class Active {
      public final String id;
      public int amplifier;
      public int duration;
      public UUID caster;

      public Active(String id, int duration, int amplifier, UUID caster) {
         this.id = id;
         this.duration = duration;
         this.amplifier = amplifier;
         this.caster = caster;
      }
   }

   private ForceEffects() {
   }

   public static void add(LivingEntity entity, String effect, int duration, int amplifier, LivingEntity caster) {
      List<Active> list = EFFECTS.computeIfAbsent(entity.getUuid(), k -> new ArrayList<>());
      int clamped = Math.max(1, Math.min(duration, 32767));

      for (Active active : list) {
         if (active.id.equals(effect)) {
            int prevDuration = active.duration;
            int prevAmplifier = active.amplifier;
            active.duration = Math.max(active.duration, clamped);
            active.amplifier = Math.max(active.amplifier, amplifier);
            if (caster != null) {
               active.caster = caster.getUuid();
            }

            if (prevDuration != active.duration || prevAmplifier != active.amplifier) {
               list.sort(Comparator.comparingInt(a -> a.id.hashCode()));
               sync(entity);
            }

            return;
         }
      }

      list.add(new Active(effect, clamped, amplifier, caster != null ? caster.getUuid() : null));
      list.sort(Comparator.comparingInt(a -> a.id.hashCode()));
      sync(entity);
   }

   public static Active get(LivingEntity entity, String effect) {
      List<Active> list = EFFECTS.get(entity.getUuid());
      if (list == null) {
         return null;
      }

      for (Active active : list) {
         if (active.id.equals(effect)) {
            return active;
         }
      }

      return null;
   }

   public static boolean has(LivingEntity entity, String effect) {
      return get(entity, effect) != null;
   }

   public static List<Active> all(LivingEntity entity) {
      List<Active> list = EFFECTS.get(entity.getUuid());
      return list != null ? list : List.of();
   }

   public static void clear(LivingEntity entity, String effect) {
      List<Active> list = EFFECTS.get(entity.getUuid());
      if (list == null) {
         return;
      }

      boolean changed = list.removeIf(active -> active.id.equals(effect));
      if (changed) {
         sync(entity);
      }
   }

   public static void clearAll(LivingEntity entity) {
      List<Active> list = EFFECTS.remove(entity.getUuid());
      if (list != null && !list.isEmpty()) {
         sync(entity);
      }
   }

   public static List<LivingEntity> getTargets(LivingEntity caster, String effect) {
      List<LivingEntity> targets = new ArrayList<>();
      if (caster.getWorld() instanceof ServerWorld world) {
         for (Entity entity : world.iterateEntities()) {
            if (entity instanceof LivingEntity living && living != caster) {
               Active active = get(living, effect);
               if (active != null && active.caster != null && active.caster.equals(caster.getUuid())) {
                  targets.add(living);
               }
            }
         }
      }

      return targets;
   }

   public static void tick(LivingEntity entity) {
      List<Active> list = EFFECTS.get(entity.getUuid());
      if (list == null || list.isEmpty()) {
         return;
      }

      Iterator<Active> it = list.iterator();
      int expiredChokeAmplifier = -1;

      while (it.hasNext()) {
         Active active = it.next();
         active.duration--;
         if (active.duration <= 0) {
            it.remove();
            if (active.id.equals(CHOKE)) {
               expiredChokeAmplifier = active.amplifier;
            }
         }
      }

      if (expiredChokeAmplifier >= 0) {
         add(entity, STUN, Power.getStunDurationTicks(expiredChokeAmplifier), 0, null);
      }

      sync(entity);
   }

   public static void sync(LivingEntity entity) {
      List<Active> list = EFFECTS.get(entity.getUuid());
      List<ALNetwork.EffectData> data = new ArrayList<>();
      if (list != null) {
         for (Active active : list) {
            data.add(new ALNetwork.EffectData(active.id, active.amplifier, active.duration, casterEntityId(entity, active.caster)));
         }
      }

      ALNetwork.SyncEffectsPayload payload = new ALNetwork.SyncEffectsPayload(entity.getId(), data);
      if (entity instanceof ServerPlayerEntity p) {
         ServerPlayNetworking.send(p, payload);
      }

      for (ServerPlayerEntity watcher : PlayerLookup.tracking(entity)) {
         ServerPlayNetworking.send(watcher, payload);
      }
   }

   private static int casterEntityId(LivingEntity entity, java.util.UUID caster) {
      if (caster == null || !(entity.getWorld() instanceof net.minecraft.server.world.ServerWorld world)) {
         return 0;
      }

      net.minecraft.entity.Entity casterEntity = world.getEntity(caster);
      return casterEntity != null ? casterEntity.getId() : 0;
   }

   public static void reset() {
      EFFECTS.clear();
   }
}
