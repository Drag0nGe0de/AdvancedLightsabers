package com.drag0nge0de.lightsabers.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class ALGameEventTrigger implements Criterion<ALGameEventTrigger.Conditions> {
   public static final ALGameEventTrigger INSTANCE = new ALGameEventTrigger();
   private final Map<PlayerAdvancementTracker, Set<ConditionsContainer<Conditions>>> listeners = new HashMap<>();

   @Override
   public Codec<Conditions> getConditionsCodec() {
      return Conditions.CODEC.codec();
   }

   @Override
   public void beginTrackingCondition(PlayerAdvancementTracker manager, ConditionsContainer<Conditions> conditions) {
      this.listeners.computeIfAbsent(manager, m -> new HashSet<>()).add(conditions);
   }

   @Override
   public void endTrackingCondition(PlayerAdvancementTracker manager, ConditionsContainer<Conditions> conditions) {
      Set<ConditionsContainer<Conditions>> set = this.listeners.get(manager);
      if (set != null) {
         set.remove(conditions);
         if (set.isEmpty()) {
            this.listeners.remove(manager);
         }
      }
   }

   @Override
   public void endTracking(PlayerAdvancementTracker manager) {
      this.listeners.remove(manager);
   }

   public void trigger(ServerPlayerEntity player, String event) {
      PlayerAdvancementTracker tracker = player.getAdvancementTracker();
      Set<ConditionsContainer<Conditions>> set = this.listeners.get(tracker);
      if (set != null && !set.isEmpty()) {
         ArrayList<ConditionsContainer<Conditions>> snapshot = new ArrayList<>(set);

         for (ConditionsContainer<Conditions> container : snapshot) {
            if (container.conditions().event().equals(event)) {
               container.grant(tracker);
            }
         }
      }
   }

   public record Conditions(String event) implements AbstractCriterion.Conditions {
      public static final MapCodec<Conditions> CODEC = RecordCodecBuilder.mapCodec(
         instance -> instance.group(Codec.STRING.fieldOf("event").forGetter(Conditions::event)).apply(instance, Conditions::new)
      );

      @Override
      public Optional<LootContextPredicate> player() {
         return Optional.empty();
      }
   }
}
