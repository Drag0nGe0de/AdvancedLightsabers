package com.drag0nge0de.lightsabers.config;

import com.drag0nge0de.lightsabers.AL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ALConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path PATH = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("lightsabers.json");
   private static ALConfig instance;

   public boolean enableShaders = true;
   public boolean dynamicLightsEnabled = true;
   public int dynamicLightsUpdateInterval = 1000;
   public float renderGlobalMultiplier = 1.0F;
   public float renderWidthMultiplier = 1.0F;
   public float renderSmoothingMultiplier = 1.0F;
   public float renderOpacityMultiplier = 1.0F;
   public float renderLightingMultiplier = 1.0F;

   public boolean firstPersonAnimations = true;
   public boolean thirdPersonAnimations = true;
   public float damageMultiplier = 1.0F;
   public float attackSpeedMultiplier = 1.0F;
   public float jediTempleSpawnRate = 1.0F;
   public float sithTombSpawnRate = 1.0F;
   public float crystalCaveRate = 1.0F;

   public static ALConfig get() {
      if (instance == null) {
         load();
      }

      return instance;
   }

   public static void load() {
      if (instance == null) {
         instance = new ALConfig();
      }

      if (Files.exists(PATH)) {
         try {
            ALConfig loaded = GSON.fromJson(Files.readString(PATH), ALConfig.class);
            if (loaded != null) {
               instance.enableShaders = loaded.enableShaders;
               instance.dynamicLightsEnabled = loaded.dynamicLightsEnabled;
               instance.dynamicLightsUpdateInterval = Math.max(100, loaded.dynamicLightsUpdateInterval);
               instance.renderGlobalMultiplier = clamp(loaded.renderGlobalMultiplier);
               instance.renderWidthMultiplier = clamp(loaded.renderWidthMultiplier);
               instance.renderSmoothingMultiplier = clamp(loaded.renderSmoothingMultiplier);
               instance.renderOpacityMultiplier = clamp(loaded.renderOpacityMultiplier);
               instance.renderLightingMultiplier = clamp(loaded.renderLightingMultiplier);
               instance.firstPersonAnimations = loaded.firstPersonAnimations;
               instance.thirdPersonAnimations = loaded.thirdPersonAnimations;
               instance.damageMultiplier = clampRate(loaded.damageMultiplier);
               instance.attackSpeedMultiplier = clampSpeed(loaded.attackSpeedMultiplier);
               instance.jediTempleSpawnRate = clampRate(loaded.jediTempleSpawnRate);
               instance.sithTombSpawnRate = clampRate(loaded.sithTombSpawnRate);
               instance.crystalCaveRate = clampRate(loaded.crystalCaveRate);
            }
         } catch (IOException e) {
            AL.LOGGER.warn("Could not read lightsabers.json, using defaults", e);
         }
      }

      save();
   }

   public static void save() {
      try {
         Files.createDirectories(PATH.getParent());
         Files.writeString(PATH, GSON.toJson(instance));
      } catch (IOException e) {
         AL.LOGGER.warn("Could not write lightsabers.json", e);
      }
   }

   private static float clamp(float value) {
      return Math.max(0.05F, Math.min(value, 5.0F));
   }

   private static float clampRate(float value) {
      if (Float.isNaN(value)) {
         return 1.0F;
      }

      return Math.max(0.0F, Math.min(value, 5.0F));
   }

   private static float clampSpeed(float value) {
      if (Float.isNaN(value)) {
         return 1.0F;
      }

      return Math.max(0.1F, Math.min(value, 3.0F));
   }
}
