package com.drag0nge0de.lightsabers.force;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.advancement.ALAdvancements;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import com.drag0nge0de.lightsabers.registry.ALSounds;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class ForceManager {
   public static final float SENSITIVITY_MAX_ENERGY = 50.0F;
   private static final Map<UUID, Float> LAST_SENT_ENERGY = new HashMap<>();

   public static final int MAX_SLOTS = 3;

   public static int getXP(PlayerEntity player) {
      return (Integer)player.getAttachedOrCreate(ForceState.XP, () -> 0);
   }

   public static void addXP(ServerPlayerEntity player, int amount) {
      if (amount > 0) {
         setXP(player, getXP(player) + amount);
      }
   }

   public static void setXP(ServerPlayerEntity player, int amount) {
      player.setAttached(ForceState.XP, Math.max(0, amount));
      sendSync(player);
   }

   public static boolean trySpendXP(ServerPlayerEntity player, int amount) {
      int xp = getXP(player);
      if (xp < amount) {
         return false;
      } else {
         player.setAttached(ForceState.XP, xp - amount);
         return true;
      }
   }

   public static float getEnergy(PlayerEntity player) {
      return (Float)player.getAttachedOrCreate(ForceState.ENERGY, () -> 0.0F);
   }

   public static void setEnergy(ServerPlayerEntity player, float amount) {
      player.setAttached(ForceState.ENERGY, Math.clamp(amount, 0.0F, getMaxEnergy(player)));
      sendSync(player);
   }

   public static void addEnergy(ServerPlayerEntity player, float amount) {
      setEnergy(player, getEnergy(player) + amount);
   }

   public static float getMaxEnergy(PlayerEntity player) {
      if (!hasPower(player, Power.FORCE_SENSITIVITY)) {
         return 0.0F;
      } else {
         float max = SENSITIVITY_MAX_ENERGY;

         for (Power power : Power.POWERS) {
            if (hasPower(player, power)) {
               max += (float)power.stats.forceBonus;
            }
         }

         return max;
      }
   }

   public static float getRegenPerSecond(PlayerEntity player) {
      if (!hasPower(player, Power.FORCE_SENSITIVITY)) {
         return 0.0F;
      } else {
         float regen = 0.0F;

         for (Power power : Power.POWERS) {
            if (hasPower(player, power)) {
               regen += (float)power.stats.regen;
            }
         }

         return regen;
      }
   }

   public static int getBasePower(PlayerEntity player) {
      int base = 0;
      List<String> unlocked = getUnlocked(player);

      for (Power power : Power.POWERS) {
         if (unlocked.contains(power.getName())) {
            base += power.stats.baseBonus - power.stats.baseRequirement;
         }
      }

      return base;
   }

   public static boolean trySpendEnergy(ServerPlayerEntity player, float cost) {
      if (getEnergy(player) < cost) {
         player.playSoundToPlayer(ALSounds.FORCE_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
         player.sendMessage(Text.translatable("message.lightsabers.no_energy").formatted(Formatting.RED), true);
         return false;
      } else {
         setEnergy(player, getEnergy(player) - cost);
         return true;
      }
   }

   public static List<String> getUnlocked(PlayerEntity player) {
      return (List<String>)player.getAttachedOrCreate(ForceState.UNLOCKED, ArrayList::new);
   }

   public static boolean hasPower(PlayerEntity player, Power power) {
      return getUnlocked(player).contains(power.getName());
   }

   public static boolean canUnlock(PlayerEntity player, Power power) {
      return !hasPower(player, power) && (power.parent == null || hasPower(player, power.parent));
   }

   public static int getUnlockCost(PlayerEntity player, Power power) {
      return power.side.isPolar() ? power.getActualXpCost(getCompletion(player, power.side.getOpposite())) : power.stats.xpCost;
   }

   public static float getCompletion(PlayerEntity player, ForceSide side) {
      List<Power> sidePowers = new ArrayList<>();

      for (Power p : Power.sidePowers(side)) {
         if (p != side.getRoot()) {
            sidePowers.add(p);
         }
      }

      if (sidePowers.isEmpty()) {
         return 0.0F;
      } else {
         int unlocked = 0;

         for (Power p : sidePowers) {
            if (hasPower(player, p)) {
               unlocked++;
            }
         }

         return (float)unlocked / (float)sidePowers.size();
      }
   }

   public static boolean unlockPowerUngated(ServerPlayerEntity player, Power power) {
      if (power == null || hasPower(player, power)) {
         return false;
      }

      if (power.parent != null) {
         unlockPowerUngated(player, power.parent);
      }

      List<String> unlocked = new ArrayList<>(getUnlocked(player));
      if (unlocked.contains(power.getName())) {
         return false;
      }

      unlocked.add(power.getName());
      player.setAttached(ForceState.UNLOCKED, unlocked);
      setEnergy(player, Math.min(getEnergy(player), getMaxEnergy(player)));
      sendSync(player);
      return true;
   }

   public static boolean unlockPower(ServerPlayerEntity player, Power power) {
      if (!canUnlock(player, power)) {
         return false;
      } else {
         int basePower = getBasePower(player);
         if (power.stats.baseRequirement > basePower) {
            player.sendMessage(
               Text.translatable("message.lightsabers.need_base_power", new Object[]{power.stats.baseRequirement}).formatted(Formatting.RED), true
            );
            return false;
         } else {
            int cost = getUnlockCost(player, power);
            if (!trySpendXP(player, cost)) {
               player.sendMessage(Text.translatable("message.lightsabers.need_xp", new Object[]{cost}).formatted(Formatting.RED), true);
               return false;
            } else {
               List<String> unlocked = new ArrayList<>(getUnlocked(player));
               unlocked.add(power.getName());
               if (power == Power.FORCE_SENSITIVITY) {
                  for (ForceSide side : new ForceSide[]{ForceSide.LIGHT, ForceSide.DARK, ForceSide.NEUTRAL}) {
                     Power root = side.getRoot();
                     if (root != null && !unlocked.contains(root.getName())) {
                        unlocked.add(root.getName());
                     }
                  }
               }

               player.setAttached(ForceState.UNLOCKED, unlocked);
               setEnergy(player, Math.min(getEnergy(player), getMaxEnergy(player)));
               player.playSoundToPlayer(ALSounds.HOLOCRON_UNLOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
               player.sendMessage(
                  Text.translatable("message.lightsabers.power_unlocked", new Object[]{power.getDisplayName()}).formatted(Formatting.GOLD), false
               );
               if (power == Power.FORCE_SENSITIVITY) {
                  ALAdvancements.trigger(player, ALAdvancements.FORCE_SENSITIVITY);
               } else {
                  checkMastery(player, power.side);
               }

               sendSync(player);
               return true;
            }
         }
      }
   }

   private static void checkMastery(ServerPlayerEntity player, ForceSide side) {
      if (side == ForceSide.LIGHT || side == ForceSide.DARK) {
         float completion = getCompletion(player, side);
         if (completion >= 1.0F) {
            ALAdvancements.triggerMastery(player, side);
         }
      }
   }

   public static boolean removePower(ServerPlayerEntity player, Power power) {
      if (!hasPower(player, power)) {
         return false;
      } else {
         List<String> unlocked = new ArrayList<>(getUnlocked(player));
         removeWithDescendants(unlocked, power);
         player.setAttached(ForceState.UNLOCKED, unlocked);
         setEnergy(player, getEnergy(player));
         sendSync(player);
         return true;
      }
   }

   private static void removeWithDescendants(List<String> unlocked, Power power) {
      unlocked.remove(power.getName());

      for (Power child : power.children) {
         removeWithDescendants(unlocked, child);
      }
   }

   public static void reset(ServerPlayerEntity player) {
      player.removeAttached(ForceState.UNLOCKED);
      player.removeAttached(ForceState.ENERGY);
      player.removeAttached(ForceState.XP);
      player.removeAttached(ForceState.SELECTED_SLOT);
      player.removeAttached(ForceState.SELECTED_POWERS);
      sendSync(player);
   }

   public static int getSelectedSlot(PlayerEntity player) {
      Integer slot = (Integer)player.getAttached(ForceState.SELECTED_SLOT);
      return slot != null ? Math.min(slot, MAX_SLOTS - 1) : 0;
   }

   public static void setSelectedSlot(ServerPlayerEntity player, int slot) {
      player.setAttached(ForceState.SELECTED_SLOT, Math.floorMod(slot, MAX_SLOTS));
      sendSelection(player);
   }

   public static List<String> getSelectedPowers(PlayerEntity player) {
      List<String> list = (List<String>)player.getAttachedOrCreate(ForceState.SELECTED_POWERS, () -> {
         List<String> empty = new ArrayList<>();
         for (int i = 0; i < MAX_SLOTS; i++) {
            empty.add("");
         }
         return empty;
      });

      while (list.size() < MAX_SLOTS) {
         list.add("");
      }

      return list;
   }

   public static void setSelectedPowers(ServerPlayerEntity player, List<String> powers) {
      List<String> copy = new ArrayList<>();
      for (int i = 0; i < MAX_SLOTS; i++) {
         copy.add(i < powers.size() ? powers.get(i) : "");
      }

      player.setAttached(ForceState.SELECTED_POWERS, copy);
      sendSelection(player);
   }

   public static Power getSelectedPower(PlayerEntity player) {
      int slot = getSelectedSlot(player);
      List<String> powers = getSelectedPowers(player);
      if (slot >= powers.size()) {
         return null;
      }

      return Power.byName(powers.get(slot));
   }

   public static int getXPInvested(PlayerEntity player, Power power) {
      Map<String, Integer> map = (Map<String, Integer>) player.getAttachedOrCreate(ForceState.XP_INVESTED, HashMap::new);
      return map.getOrDefault(power.getName(), 0);
   }

   private static void setXPInvested(ServerPlayerEntity player, Power power, int invested) {
      Map<String, Integer> map = new HashMap<>((Map<String, Integer>) player.getAttachedOrCreate(ForceState.XP_INVESTED, HashMap::new));
      map.put(power.getName(), invested);
      player.setAttached(ForceState.XP_INVESTED, map);
   }

   private static void tickDraining(ServerPlayerEntity player) {
      String drainingTo = (String) player.getAttached(ForceState.DRAINING);
      if (drainingTo == null || drainingTo.isEmpty()) {
         return;
      }

      Integer tick = (Integer) player.getAttached(ForceState.DRAINING_TICK);
      if (tick == null || player.age - tick > 2) {

         player.setAttached(ForceState.DRAINING, "");
         return;
      }

      Power power = Power.byName(drainingTo);
      if (power == null || hasPower(player, power) || !canUnlock(player, power)) {
         player.setAttached(ForceState.DRAINING, "");
         return;
      }

      int cost = getUnlockCost(player, power);
      int invested = getXPInvested(player, power);

      if (power.stats.baseRequirement > getBasePower(player)) {
         player.setAttached(ForceState.DRAINING, "");
         return;
      }

      if (invested >= cost) {
         completeDrainUnlock(player, power);
         return;
      }

      int spend = Math.max(1, cost / 40);
      int xp = getXP(player);
      if (spend > xp) {
         spend = xp;
      }
      if (spend > cost - invested) {
         spend = cost - invested;
      }

      if (spend > 0) {
         invested += spend;
         setXPInvested(player, power, invested);
         player.setAttached(ForceState.XP, Math.max(0, xp - spend));
         player.playSoundToPlayer(ALSounds.HOLOCRON_INVEST, SoundCategory.PLAYERS, 1.0F,
               1.1F + (new Random().nextFloat() - new Random().nextFloat()) * 0.2F);
         sendSync(player);
      }

      if (invested >= cost) {
         completeDrainUnlock(player, power);
      }
   }

   private static void completeDrainUnlock(ServerPlayerEntity player, Power power) {
      player.setAttached(ForceState.DRAINING, "");
      List<String> unlocked = new ArrayList<>(getUnlocked(player));
      unlocked.add(power.getName());
      if (power == Power.FORCE_SENSITIVITY) {
         for (ForceSide side : new ForceSide[]{ForceSide.LIGHT, ForceSide.DARK, ForceSide.NEUTRAL}) {
            Power root = side.getRoot();
            if (root != null && !unlocked.contains(root.getName())) {
               unlocked.add(root.getName());
            }
         }
      }

      player.setAttached(ForceState.UNLOCKED, unlocked);
      setEnergy(player, Math.min(getEnergy(player), getMaxEnergy(player)));
      player.playSoundToPlayer(ALSounds.HOLOCRON_UNLOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
      player.sendMessage(
         Text.translatable("message.lightsabers.power_unlocked", new Object[]{power.getDisplayName()}).formatted(Formatting.GOLD), false
      );
      if (power == Power.FORCE_SENSITIVITY) {
         ALAdvancements.trigger(player, ALAdvancements.FORCE_SENSITIVITY);
      } else {
         checkMastery(player, power.side);
      }

      sendSync(player);
   }

   public static void tick(ServerPlayerEntity player) {
      if (!player.isSpectator()) {
         if (player.age % 20 == 0) {
            float max = getMaxEnergy(player);
            float energy = getEnergy(player);
            if (energy < max && !ForcePowers.isChanneling(player)) {
               float regen = getRegenPerSecond(player);
               player.setAttached(ForceState.ENERGY, Math.min(energy + regen, max));
            }
         }

         if (player.age % 10 == 0) {
            float energy = getEnergy(player);
            Float last = LAST_SENT_ENERGY.get(player.getUuid());
            if (last == null || Math.abs(last - energy) >= 0.5F) {
               sendSync(player);
            }
         }

         tickDraining(player);
      }
   }

   public static void sendSync(ServerPlayerEntity player) {
      List<String> unlocked = getUnlocked(player);
      ServerPlayNetworking.send(
         player, new ALNetwork.SyncForcePayload(getXP(player), getEnergy(player), getMaxEnergy(player), getRegenPerSecond(player), List.copyOf(unlocked),
            Map.copyOf((Map<String, Integer>) player.getAttachedOrCreate(ForceState.XP_INVESTED, HashMap::new)))
      );
      LAST_SENT_ENERGY.put(player.getUuid(), getEnergy(player));
      sendSelection(player);
   }

   public static void sendSelection(ServerPlayerEntity player) {
      ServerPlayNetworking.send(player, new ALNetwork.SyncSelectionPayload(getSelectedSlot(player), List.copyOf(getSelectedPowers(player))));
   }
}
