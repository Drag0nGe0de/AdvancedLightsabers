package com.drag0nge0de.lightsabers.advancement;

import com.drag0nge0de.lightsabers.AL;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

public class ALAdvancements {
   public static final String IGNITE_SABER = "ignite_saber";
   public static final String FORCE_SENSITIVITY = "force_sensitivity";
   public static final String SABER_THROW = "saber_throw";
   public static final String LIGHT_MASTERY = "light_mastery";
   public static final String DARK_MASTERY = "dark_mastery";

   public static void register() {
      Registry.register(Registries.CRITERION, AL.id("game_event"), ALGameEventTrigger.INSTANCE);
   }

   public static void trigger(ServerPlayerEntity player, String event) {
      ALGameEventTrigger.INSTANCE.trigger(player, event);
   }

   public static void triggerMastery(ServerPlayerEntity player, com.drag0nge0de.lightsabers.force.ForceSide side) {
      if (side == com.drag0nge0de.lightsabers.force.ForceSide.LIGHT) {
         trigger(player, LIGHT_MASTERY);
      } else if (side == com.drag0nge0de.lightsabers.force.ForceSide.DARK) {
         trigger(player, DARK_MASTERY);
      }
   }
}
