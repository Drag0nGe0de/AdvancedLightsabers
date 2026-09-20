package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public class ALSounds {
   public static final SoundEvent LIGHTSABER_ON = register("lightsaber.on");
   public static final SoundEvent LIGHTSABER_OFF = register("lightsaber.off");
   public static final SoundEvent LIGHTSABER_HIT = register("lightsaber.hit");
   public static final SoundEvent LIGHTSABER_SWING = register("lightsaber.swing");
   public static final SoundEvent FORCE_CAST = register("force.cast");
   public static final SoundEvent FORCE_DARK = register("force.dark");
   public static final SoundEvent FORCE_FAIL = register("force.fail");
   public static final SoundEvent FORCE_LIGHTNING = register("force.lightning");
   public static final SoundEvent FORCE_HEAL = register("force.heal");
   public static final SoundEvent HOLOCRON_INVEST = register("block.holocron.invest");
   public static final SoundEvent HOLOCRON_UNLOCK = register("block.holocron.unlock");
   public static final SoundEvent SARCOPHAGUS_OPEN = register("block.sarcophagus.open");

   public static final SoundEvent SARCOPHAGUS_CLOSE = register("block.sarcophagus.close");
   public static final SoundEvent SITH_GHOST_IDLE = register("mob.sith_ghost.idle");
   public static final SoundEvent SITH_GHOST_DEATH = register("mob.sith_ghost.death");

   public static final SoundEvent MOB_LIGHTSABER_ON = register("mob.lightsaber.on");
   public static final SoundEvent MOB_LIGHTSABER_OFF = register("mob.lightsaber.off");
   public static final SoundEvent MOB_LIGHTSABER_SWING = register("mob.lightsaber.swing");
   public static final SoundEvent AMBIENT_FORTIFY = register("ambient.fortify");
   public static final SoundEvent AMBIENT_STEALTH = register("ambient.stealth");
   public static final SoundEvent FORCE_STEALTH_ON = register("force.stealth.on");
   public static final SoundEvent FORCE_STEALTH_OFF = register("force.stealth.off");

   private static SoundEvent register(String name) {
      return (SoundEvent)Registry.register(Registries.SOUND_EVENT, AL.id(name), SoundEvent.of(AL.id(name)));
   }

   public static void register() {
   }
}
