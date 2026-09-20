package com.drag0nge0de.lightsabers.handler;

import com.drag0nge0de.lightsabers.damage.ALDamageTypes;
import com.drag0nge0de.lightsabers.force.ForceEffects;
import com.drag0nge0de.lightsabers.force.ForceManager;
import com.drag0nge0de.lightsabers.force.ForcePowers;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import java.util.Iterator;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

public class ForceEvents {
   private static final ThreadLocal<Boolean> RESCALING = ThreadLocal.withInitial(() -> false);

   public static void register() {
      AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
         if (!world.isClient && !player.isCreative()
               && world.getBlockState(pos).isOf(ALBlocks.SITH_STONE_COFFIN)) {
            net.minecraft.block.BlockState state = world.getBlockState(pos);
            net.minecraft.util.math.BlockPos basePos = state.contains(com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock.TOP)
                  && state.get(com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock.TOP) ? pos.down() : pos;

            if (world.getBlockState(basePos).isOf(ALBlocks.SITH_STONE_COFFIN)
                  && world.getBlockEntity(basePos) instanceof com.drag0nge0de.lightsabers.block.blockentity.SithStoneCoffinBlockEntity coffin) {
               return coffin.punchRetrieve((ServerWorld) world, basePos, player)
                  ? net.minecraft.util.ActionResult.FAIL
                  : net.minecraft.util.ActionResult.PASS;
            }
         }

         return net.minecraft.util.ActionResult.PASS;
      });

      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ForcePowers.tickChannels(player);
            ForcePowers.tickPlayerForce(player);
            ForceManager.tick(player);
         }

         for (ServerWorld world : server.getWorlds()) {
            Iterator<Entity> it = world.iterateEntities().iterator();

            while (it.hasNext()) {
               Entity entity = it.next();
               if (entity instanceof LivingEntity living && living.isAlive()) {
                  ForcePowers.tickEntityEffects(living);
               }
            }
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, sender) -> ForcePowers.stopChannel(handler.player, false));
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ForceManager.sendSync(handler.player));
      ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> ForceManager.sendSync(newPlayer));
      ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
         if (entity instanceof ServerPlayerEntity player) {
            ForcePowers.onPlayerDeath(player);
         } else {
            ForceEffects.clearAll(entity);
         }
      });
      ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
         int xp = ForceManager.getXP(oldPlayer);
         if (!alive) {
            if (newPlayer.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
               xp = 0;
            } else {
               xp = (int) Math.floor((float) xp * 0.7F);
            }
         }

         newPlayer.setAttached(com.drag0nge0de.lightsabers.force.ForceState.XP, xp);
         newPlayer.setAttached(
            com.drag0nge0de.lightsabers.force.ForceState.UNLOCKED,
            new java.util.ArrayList<>(ForceManager.getUnlocked(oldPlayer))
         );
         newPlayer.setAttached(
            com.drag0nge0de.lightsabers.force.ForceState.SELECTED_POWERS,
            new java.util.ArrayList<>(ForceManager.getSelectedPowers(oldPlayer))
         );
         newPlayer.setAttached(com.drag0nge0de.lightsabers.force.ForceState.SELECTED_SLOT, ForceManager.getSelectedSlot(oldPlayer));
         ForceManager.setEnergy(newPlayer, ForceManager.getEnergy(oldPlayer));
      });
      ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
         if (!(entity instanceof ServerPlayerEntity player) || amount <= 0.0F) {
            return true;
         } else if (RESCALING.get()) {
            return true;
         } else {
            LivingEntity attacker = source.getAttacker() instanceof LivingEntity living ? living : null;
            if (attacker != null && ForcePowers.isStunned(attacker)) {
               return false;
            } else if (isSaberMelee(source) && !isActiveSaber(attacker)) {
               return false;
            } else if (source.isIn(DamageTypeTags.IS_FALL)
               && ForcePowers.hasReboundCharge(player)) {
               float remaining = ForcePowers.applyReboundFall(player, amount);
               if (remaining <= 0.0F) {
                  player.sendMessage(Text.translatable("message.lightsabers.rebound").formatted(Formatting.YELLOW), true);
                  return false;
               } else {
                  return rescale(entity, source, remaining);
               }
            } else {
               ForceEffects.Active fortify = ForceEffects.get(player, ForceEffects.FORTIFY);
               if (fortify != null && source.isOf(ALDamageTypes.FORCE)) {
                  return rescale(entity, source, amount / ForcePowers.getModifierAmount(fortify.amplifier));
               }

               ForceEffects.Active resist = ForceEffects.get(player, ForceEffects.RESIST);
               if (resist != null && isSaberMelee(source)) {
                  return rescale(entity, source, amount / ForcePowers.getModifierAmount(resist.amplifier));
               }

               if (attacker instanceof ServerPlayerEntity melee && source.isOf(DamageTypes.PLAYER_ATTACK)) {
                  ForceEffects.Active meditation = ForceEffects.get(melee, ForceEffects.MEDITATION);
                  if (meditation != null) {
                     float multiplier = ForcePowers.getModifierAmount(meditation.amplifier);
                     if (multiplier > 1.0F) {
                        return rescale(entity, source, amount * multiplier);
                     }
                  }
               }

               return true;
            }
         }
      });
   }

   public static void onVanillaXp(ServerPlayerEntity player, int amount) {
      if (amount > 0) {
         ForceManager.addXP(player, amount / 2);
      }
   }

   private static boolean isSaberMelee(DamageSource source) {
      return source.isOf(DamageTypes.PLAYER_ATTACK)
         && source.getAttacker() instanceof ServerPlayerEntity attacker
         && attacker.getMainHandStack().getItem() instanceof LightsaberItem;
   }

   private static boolean isActiveSaber(LivingEntity attacker) {
      if (attacker == null) {
         return false;
      } else {
         ItemStack held = attacker.getMainHandStack();
         return held.getItem() instanceof LightsaberItem && LightsaberItem.getComponent(held).active();
      }
   }

   private static boolean rescale(LivingEntity entity, DamageSource source, float newAmount) {
      RESCALING.set(true);

      try {
         entity.damage(source, Math.max(0.1F, newAmount));
      } finally {
         RESCALING.set(false);
      }

      return false;
   }
}
