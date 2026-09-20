package com.drag0nge0de.lightsabers.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.drag0nge0de.lightsabers.force.ForceEffects;
import com.drag0nge0de.lightsabers.registry.ALSounds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public final class ForceLoopSounds {

   private static final Map<String, Loop> LOOPS = new HashMap<>();
   private static final Random RAND = new Random();

   private ForceLoopSounds() {
   }

   private static class Loop extends MovingSoundInstance {
      private final Follower follower;
      private boolean finished = false;

      private Loop(SoundEvent sound, Follower follower, float volume) {
         super(sound, SoundCategory.PLAYERS, net.minecraft.util.math.random.Random.create());
         this.follower = follower;
         this.volume = volume;
         this.relative = false;
         this.repeat = true;
         this.repeatDelay = 0;
         this.attenuationType = SoundInstance.AttenuationType.LINEAR;
         this.x = follower.x();
         this.y = follower.y();
         this.z = follower.z();
      }

      @Override
      public void tick() {
         if (this.finished || !this.follower.active()) {
            this.setDone();
            return;
         }

         this.x = this.follower.x();
         this.y = this.follower.y();
         this.z = this.follower.z();
      }

      void stopNow() {
         this.finished = true;
      }
   }

   private interface Follower {
      boolean active();

      double x();

      double y();

      double z();
   }

   public static void tick(MinecraftClient client) {
      if (client.player == null || client.world == null) {
         LOOPS.values().forEach(Loop::stopNow);
         return;
      }

      Map<String, Entity> wanted = new HashMap<>();

      ForceLoopSounds.collect(client, wanted);

      Iterator<Map.Entry<String, Loop>> it = LOOPS.entrySet().iterator();

      while (it.hasNext()) {
         Map.Entry<String, Loop> entry = it.next();

         if (!wanted.containsKey(entry.getKey())) {
            entry.getValue().stopNow();
            it.remove();
         }
      }

      for (Map.Entry<String, Entity> entry : wanted.entrySet()) {
         String key = entry.getKey();

         if (!LOOPS.containsKey(key)) {
            Loop loop = createLoop(key, entry.getValue());

            if (loop != null) {
               LOOPS.put(key, loop);
               client.getSoundManager().play(loop);
            }
         }
      }
   }

   private static void collect(MinecraftClient client, Map<String, Entity> wanted) {
      for (Entity entity : client.world.getEntities()) {
         int[] lightning = ForceArcs.getEffect(entity, ForceArcs.LIGHTNING);

         if (lightning != null && entity.isAlive()) {
            wanted.put("lightning:" + entity.getId(), entity);
         }
      }

      int[] fortify = client.player != null ? ForceClientState.getEffect(ForceEffects.FORTIFY) : null;

      if (fortify != null) {
         wanted.put("fortify:self", client.player);
      }

      int[] stealth = client.player != null ? ForceClientState.getEffect(ForceEffects.STEALTH) : null;

      if (stealth != null) {
         wanted.put("stealth:self", client.player);
      }
   }

   private static Loop createLoop(String key, Entity entity) {
      if (key.startsWith("lightning:")) {
         return new Loop(ALSounds.FORCE_LIGHTNING, followerOf(entity), 1.0F) {
            @Override
            public void tick() {
               super.tick();
               this.pitch = 0.75F + RAND.nextFloat() * 0.25F;
            }
         };
      }

      if (key.equals("fortify:self")) {
         return new Loop(ALSounds.AMBIENT_FORTIFY, followerOf(entity), 1.0F);
      }

      if (key.equals("stealth:self")) {
         return new Loop(ALSounds.AMBIENT_STEALTH, followerOf(entity), 1.0F);
      }

      return null;
   }

   private static Follower followerOf(Entity entity) {
      return new Follower() {
         @Override
         public boolean active() {
            return entity.isAlive() && entity.getWorld() == MinecraftClient.getInstance().world;
         }

         @Override
         public double x() {
            return entity.getX();
         }

         @Override
         public double y() {
            return entity.getY();
         }

         @Override
         public double z() {
            return entity.getZ();
         }
      };
   }

   public static void stopAll() {
      LOOPS.values().forEach(Loop::stopNow);
      LOOPS.clear();
   }
}
