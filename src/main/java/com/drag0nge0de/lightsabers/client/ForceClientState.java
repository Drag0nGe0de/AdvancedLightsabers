package com.drag0nge0de.lightsabers.client;

import com.drag0nge0de.lightsabers.force.ForceSide;
import com.drag0nge0de.lightsabers.force.Power;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ForceClientState {
   public static int xp;
   public static float energy;
   public static float maxEnergy = 160.0F;
   public static float regen = 4.0F;
   public static int selectedSlot;
   public static Map<String, Integer> xpInvested = Map.of();
   private static final Set<String> UNLOCKED = new HashSet<>();
   private static final List<String> SELECTED_POWERS = new ArrayList<>();
   private static final Map<String, int[]> EFFECTS = new HashMap<>();

   private ForceClientState() {
   }

   public static void update(int newXp, float newEnergy, float newMax, float newRegen, Set<String> newUnlocked,
         Map<String, Integer> newInvested) {
      xp = newXp;
      energy = newEnergy;
      maxEnergy = newMax;
      regen = newRegen;
      UNLOCKED.clear();
      UNLOCKED.addAll(newUnlocked);
      xpInvested = newInvested != null ? newInvested : Map.of();
   }

   public static void updateSelection(int slot, List<String> powers) {
      selectedSlot = slot;

      while (SELECTED_POWERS.size() < 3) {
         SELECTED_POWERS.add("");
      }

      for (int i = 0; i < 3; i++) {
         SELECTED_POWERS.set(i, i < powers.size() ? powers.get(i) : "");
      }
   }

   public static void updateEffects(Map<String, int[]> effects) {
      EFFECTS.clear();
      EFFECTS.putAll(effects);
   }

   public static void tickEffects() {
      if (EFFECTS.isEmpty()) {
         return;
      }

      for (Map.Entry<String, int[]> entry : EFFECTS.entrySet()) {
         int[] data = entry.getValue();
         data[1] = Math.max(0, data[1] - 1);
      }

      EFFECTS.entrySet().removeIf(e -> e.getValue()[1] <= 0);
   }

   public static int[] getEffect(String id) {
      return EFFECTS.get(id);
   }

   public static boolean hasEffect(String id) {
      return EFFECTS.containsKey(id);
   }

   public static java.util.Map<String, int[]> allEffects() {
      return EFFECTS;
   }

   public static Power getSelectedPower() {
      if (selectedSlot >= SELECTED_POWERS.size()) {
         return null;
      }

      return Power.byName(SELECTED_POWERS.get(selectedSlot));
   }

   public static Power getSlotPower(int slot) {
      return slot >= 0 && slot < SELECTED_POWERS.size() ? Power.byName(SELECTED_POWERS.get(slot)) : null;
   }

   public static void setSlotPower(int slot, String power) {
      while (SELECTED_POWERS.size() < 3) {
         SELECTED_POWERS.add("");
      }

      SELECTED_POWERS.set(slot, power);
   }

   public static boolean hasPower(String name) {
      return UNLOCKED.contains(name);
   }

   public static boolean hasSensitivity() {
      return hasPower("forceSensitivity");
   }

   public static int basePower() {
      int bp = 0;

      for (Power p : Power.POWERS) {
         if (hasPower(p.getName())) {
            bp += p.stats.baseBonus - p.stats.baseRequirement;
         }
      }

      return bp;
   }

   public static float completionOf(ForceSide side) {
      List<Power> sidePowers = Power.sidePowers(side);
      if (sidePowers.isEmpty()) {
         return 0.0F;
      } else {
         int unlocked = 0;

         for (Power p : sidePowers) {
            if (hasPower(p.getName())) {
               unlocked++;
            }
         }

         return (float)unlocked / (float)sidePowers.size();
      }
   }
}
