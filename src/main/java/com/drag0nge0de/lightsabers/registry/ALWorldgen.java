package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.world.CrystalCaveFeature;
import com.drag0nge0de.lightsabers.world.StructureGuard;
import com.drag0nge0de.lightsabers.world.WaterSafeJigsawStructure;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.structure.StructureType;

public class ALWorldgen {
   public static final CrystalCaveFeature CRYSTAL_CAVE = new CrystalCaveFeature(DefaultFeatureConfig.CODEC);
   public static final StructureType<WaterSafeJigsawStructure> WATER_SAFE_JIGSAW =
      Registry.register(Registries.STRUCTURE_TYPE, AL.id("water_safe_jigsaw"), () -> WaterSafeJigsawStructure.CODEC);

   public static void register() {
      Registry.register(Registries.FEATURE, AL.id("crystal_cave"), CRYSTAL_CAVE);

      BiomeModifications.addFeature(
         BiomeSelectors.foundInOverworld(), Feature.UNDERGROUND_STRUCTURES, RegistryKey.of(RegistryKeys.PLACED_FEATURE, AL.id("crystal_cave"))
      );

      ServerChunkEvents.CHUNK_LOAD.register(StructureGuard::onChunkLoad);
      ServerTickEvents.END_SERVER_TICK.register(StructureGuard::onServerTick);
      ServerLifecycleEvents.SERVER_STOPPING.register(server -> StructureGuard.clearSessionState());
   }
}
