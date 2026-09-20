package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.entity.SithGhostEntity;
import com.drag0nge0de.lightsabers.entity.ThrownLightsaberEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ALEntities {
   public static final EntityType<ThrownLightsaberEntity> THROWN_LIGHTSABER = register(
      "thrown_lightsaber", EntityType.Builder.<ThrownLightsaberEntity>create(ThrownLightsaberEntity::new, SpawnGroup.MISC).dimensions(0.5F, 0.5F).maxTrackingRange(6).trackingTickInterval(1)
   );
   public static final EntityType<SithGhostEntity> SITH_GHOST = register(
      "sith_ghost", EntityType.Builder.<SithGhostEntity>create(SithGhostEntity::new, SpawnGroup.MONSTER).dimensions(0.6F, 1.8F).maxTrackingRange(10)
   );

   private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
      return (EntityType<T>)Registry.register(Registries.ENTITY_TYPE, AL.id(name), builder.build(name));
   }

   public static void register() {
      FabricDefaultAttributeRegistry.register(SITH_GHOST, SithGhostEntity.createAttributes());
   }
}
