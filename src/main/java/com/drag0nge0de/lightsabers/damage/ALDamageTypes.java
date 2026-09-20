package com.drag0nge0de.lightsabers.damage;

import com.drag0nge0de.lightsabers.AL;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class ALDamageTypes {
   public static final RegistryKey<DamageType> FORCE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, AL.id("force"));
   public static final RegistryKey<DamageType> FORCE_LIGHTNING = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, AL.id("force_lightning"));
   public static final RegistryKey<DamageType> LIGHTSABER = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, AL.id("lightsaber"));

   public static DamageSource of(World world, RegistryKey<DamageType> key) {
      return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
   }

   public static DamageSource of(World world, RegistryKey<DamageType> key, Entity attacker) {
      return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker, attacker);
   }
}
