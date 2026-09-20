package com.drag0nge0de.lightsabers.force;

import com.drag0nge0de.lightsabers.advancement.ALAdvancements;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.damage.ALDamageTypes;
import com.drag0nge0de.lightsabers.entity.ThrownLightsaberEntity;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALSounds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class ForcePowers {
   public static final int USE_POWER_COOLDOWN = 20;
   public static final net.minecraft.util.Identifier STUN_MODIFIER_ID = com.drag0nge0de.lightsabers.AL.id("stun_speed_boost");
   public static final net.minecraft.util.Identifier SPEED_MODIFIER_ID = com.drag0nge0de.lightsabers.AL.id("force_speed_boost");
   private static final Map<UUID, Long> LAST_CAST = new HashMap<>();
   private static final Map<UUID, Channel> CHANNELS = new HashMap<>();
   private static final Map<UUID, Float> DRAIN_LIFE_TIMER = new HashMap<>();
   private static final Map<UUID, Boolean> FORCE_PUSHED = new HashMap<>();
   private static final Map<UUID, Boolean> STUN_APPLIED = new HashMap<>();
   private static final Random RAND = new Random();

   public static class Channel {
      public String power;
      public long started;

      public Channel(String power, long started) {
         this.power = power;
         this.started = started;
      }
   }

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(
         ALNetwork.CastPowerPayload.ID, (payload, context) -> context.player().getServer().execute(() -> cast(context.player(), payload.power()))
      );
      ServerPlayNetworking.registerGlobalReceiver(ALNetwork.UnlockPowerPayload.ID, (payload, context) -> context.player().getServer().execute(() -> {
            Power power = Power.byName(payload.power());
            if (power != null) {
               ForceManager.unlockPower(context.player(), power);
            }
         }));
      ServerPlayNetworking.registerGlobalReceiver(ALNetwork.ChannelPayload.ID, (payload, context) -> context.player().getServer().execute(() -> {
            if (payload.active()) {
               startChannel(context.player(), payload.power());
            } else {
               stopChannel(context.player(), true);
            }
         }));
      ServerPlayNetworking.registerGlobalReceiver(ALNetwork.SelectSlotPayload.ID, (payload, context) -> context.player().getServer().execute(() -> {
            ForceManager.setSelectedSlot(context.player(), payload.slot());
            stopChannel(context.player(), true);
         }));
      ServerPlayNetworking.registerGlobalReceiver(ALNetwork.AssignSlotsPayload.ID, (payload, context) -> context.player().getServer()
         .execute(() -> ForceManager.setSelectedPowers(context.player(), payload.powers())));
      ServerPlayNetworking.registerGlobalReceiver(ALNetwork.DrainXpPayload.ID, (payload, context) -> context.player().getServer()
         .execute(() -> {
            com.drag0nge0de.lightsabers.force.ForceState.touch();
            context.player().setAttached(ForceState.DRAINING, payload.power());
            context.player().setAttached(ForceState.DRAINING_TICK, context.player().age);
         }));
      ServerPlayNetworking.registerGlobalReceiver(ALNetwork.CloseHolocronPayload.ID, (payload, context) -> context.player().getServer()
         .execute(() -> {
            if (context.player().getServerWorld().getBlockEntity(payload.pos())
                  instanceof com.drag0nge0de.lightsabers.block.blockentity.HolocronBlockEntity holocron) {
               holocron.removeUser(context.player().getUuid());
               ALNetwork.sendToTracking(context.player().getServerWorld(), payload.pos(),
                     new ALNetwork.HolocronStatePayload(payload.pos(), false));
            }
         }));
   }

   public static boolean isStunned(LivingEntity entity) {
      return ForceEffects.has(entity, ForceEffects.STUN);
   }

   public static float getModifierAmount(int amplifier) {
      float f = 0.25F;

      for (int i = 0; i < amplifier; ++i) {
         f *= 2.0F;
      }

      return 1.0F + f;
   }

   public static float getForceDamageFactor(ServerPlayerEntity player) {
      ForceEffects.Active active = ForceEffects.get(player, ForceEffects.FORTIFY);
      return active != null ? 1.0F / getModifierAmount(active.amplifier) : 1.0F;
   }

   public static float getLightsaberResistFactor(ServerPlayerEntity player) {
      ForceEffects.Active active = ForceEffects.get(player, ForceEffects.RESIST);
      return active != null ? 1.0F / getModifierAmount(active.amplifier) : 1.0F;
   }

   public static float getMeditationMultiplier(ServerPlayerEntity attacker) {
      ForceEffects.Active active = ForceEffects.get(attacker, ForceEffects.MEDITATION);
      return active != null ? getModifierAmount(active.amplifier) : 1.0F;
   }

   public static void resetCastCooldown(ServerPlayerEntity player) {
      LAST_CAST.remove(player.getUuid());
   }

   public static boolean isChanneling(ServerPlayerEntity player) {
      return CHANNELS.containsKey(player.getUuid());
   }

   public static Channel getChannel(ServerPlayerEntity player) {
      return CHANNELS.get(player.getUuid());
   }

   public static boolean cast(ServerPlayerEntity player, String powerName) {

      if (player.isSpectator()) {
         return false;
      }

      Power power = Power.byName(powerName);
      if (power == null || !power.isCastable() || !ForceManager.hasPower(player, power)
         || power.stats.powerType != PowerType.PER_USE) {
         return false;
      }

      long now = player.getWorld().getTime();
      Long last = LAST_CAST.get(player.getUuid());
      if (last != null && now - last < (long)USE_POWER_COOLDOWN) {
         return false;
      }

      if (!(player.getWorld() instanceof ServerWorld world)) {
         return false;
      }

      if (ForceManager.getEnergy(player) < power.getUseCost(player)) {
         player.playSoundToPlayer(ALSounds.FORCE_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
         return false;
      }

      if (executeEffect(player, world, power)) {
         ForceManager.addEnergy(player, -power.getUseCost(player));
         LAST_CAST.put(player.getUuid(), now);
         playCastSound(player, world, power);
         return true;
      }

      player.playSoundToPlayer(ALSounds.FORCE_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
      return false;
   }

   private static void playCastSound(ServerPlayerEntity player, ServerWorld world, Power power) {
      ForceSide side = power.getSide();
      net.minecraft.sound.SoundEvent sound;
      float pitch;
      float volume = 1.0F;
      String base = power.getBaseName();
      if (base.equals("heal")) {
         sound = ALSounds.FORCE_HEAL;
         pitch = 1.0F;
      } else if (base.equals("throw")) {
         sound = ALSounds.LIGHTSABER_SWING;
         pitch = 1.0F;
      } else {
         sound = side == ForceSide.DARK ? ALSounds.FORCE_DARK : ALSounds.FORCE_CAST;
         pitch = (RAND.nextFloat() - RAND.nextFloat()) * 0.2F + 1.0F;
      }

      world.playSound(null, player.getBlockPos(), sound, SoundCategory.PLAYERS, volume, pitch);
   }

   public static void startChannel(ServerPlayerEntity player, String powerName) {

      if (player.isSpectator()) {
         return;
      }

      Power power = Power.byName(powerName);
      if (power == null || !power.isCastable() || !ForceManager.hasPower(player, power)
         || power.stats.powerType != PowerType.PER_SECOND) {
         return;
      }

      long now = player.getWorld().getTime();
      Long last = LAST_CAST.get(player.getUuid());
      if (last != null && now - last < (long)USE_POWER_COOLDOWN && !isChanneling(player)) {
         return;
      }

      Channel channel = CHANNELS.get(player.getUuid());
      if (channel == null) {
         channel = new Channel(powerName, now);
         CHANNELS.put(player.getUuid(), channel);
         if (power.getBaseName().equals("stealth")) {
            player.getWorld().playSound(null, player.getBlockPos(), ALSounds.FORCE_STEALTH_ON, SoundCategory.PLAYERS, 1.0F, 1.0F);
         }
      } else {

         channel.started = now;
         channel.power = powerName;
      }
   }

   public static void keepAliveChannel(ServerPlayerEntity player, String powerName) {
      Channel channel = CHANNELS.get(player.getUuid());
      if (channel != null) {
         channel.started = player.getWorld().getTime();
         channel.power = powerName;
      }
   }

   public static void stopChannel(ServerPlayerEntity player, boolean setCooldown) {
      Channel channel = CHANNELS.remove(player.getUuid());
      if (channel != null) {
         Power power = Power.byName(channel.power);
         if (power != null) {
            stopEffect(player, power);
         }

         if (setCooldown) {
            LAST_CAST.put(player.getUuid(), player.getWorld().getTime());
         }
      }
   }

   public static void tickChannels(ServerPlayerEntity player) {
      Channel channel = CHANNELS.get(player.getUuid());
      if (channel == null) {
         return;
      }

      if (player.getWorld().getTime() - channel.started > 40 || player.isDead()) {
         stopChannel(player, true);
         return;
      }

      Power power = Power.byName(channel.power);
      if (power == null || !power.isCastable() || power.stats.powerType != PowerType.PER_SECOND
         || !ForceManager.hasPower(player, power) || !(player.getWorld() instanceof ServerWorld world)) {
         stopChannel(player, true);
         return;
      }

      float cost = power.getUseCost(player);
      if (ForceManager.getEnergy(player) < cost / 20.0F) {
         stopChannel(player, true);
         return;
      }

      if (executeEffect(player, world, power)) {
         ForceManager.addEnergy(player, -cost / 20.0F);
      } else {
         stopChannel(player, true);
      }
   }

   public static void tickPlayerForce(ServerPlayerEntity player) {
      ForceEffects.tick(player);
      tickStun(player);
      tickSpeed(player);
      tickStealthVisibility(player);

      if (DRAIN_LIFE_TIMER.containsKey(player.getUuid())) {
         float timer = DRAIN_LIFE_TIMER.get(player.getUuid());
         timer -= 1.0F / 30.0F;
         if (timer <= 0.0F) {
            DRAIN_LIFE_TIMER.remove(player.getUuid());
         } else {
            DRAIN_LIFE_TIMER.put(player.getUuid(), timer);
         }

         for (LivingEntity target : ForceEffects.getTargets(player, ForceEffects.DRAIN)) {
            ForceEffects.Active active = ForceEffects.get(target, ForceEffects.DRAIN);
            if (active != null && active.duration % 5 == 0) {
               float damage = 1.0F / 30.0F * (4.0F + active.amplifier * 2.0F) * 5.0F;
               float prevHealth = target.getHealth();
               target.hurtTime = 0;
               target.timeUntilRegen = 0;
               target.damage(ALDamageTypes.of(player.getServerWorld(), ALDamageTypes.FORCE_LIGHTNING), damage);
               player.heal(Math.max(prevHealth - target.getHealth(), 0.0F));
            }
         }
      }

      for (LivingEntity target : ForceEffects.getTargets(player, ForceEffects.CHOKE)) {
         ForceEffects.Active active = ForceEffects.get(target, ForceEffects.CHOKE);
         if (active != null) {
            if (active.duration % 5 == 0) {
               float damage = 1.0F / 60.0F * (2.0F + active.amplifier * 2.0F) * 5.0F;
               target.hurtTime = 0;
               target.timeUntilRegen = 0;
               target.damage(ALDamageTypes.of(player.getServerWorld(), ALDamageTypes.FORCE), damage);
            }

            target.setVelocity(0.0, 0.001F * active.duration, 0.0);
            target.velocityModified = true;
         }
      }
   }

   public static void tickEntityEffects(LivingEntity entity) {
      Boolean pushed = FORCE_PUSHED.get(entity.getUuid());
      if (pushed != null && pushed && entity.horizontalCollision && !entity.isOnGround()) {
         FORCE_PUSHED.put(entity.getUuid(), false);
         entity.hurtTime = 0;
         double motX = entity.prevX - entity.getX();
         double motZ = entity.prevZ - entity.getZ();
         double vel = Math.sqrt(motX * motX + motZ * motZ);
         float damage = (float)Math.max(vel * 5.0 - 3.0, 0.0);
         if (damage > 0.0F && entity.getWorld() instanceof ServerWorld world) {
            entity.damage(ALDamageTypes.of(world, ALDamageTypes.FORCE), damage);
         }
      }

      ForceEffects.Active stun = ForceEffects.get(entity, ForceEffects.STUN);
      if (stun != null) {
         entity.setVelocity(entity.getVelocity().x, -0.2, entity.getVelocity().z);
         entity.velocityModified = true;
      }
   }

   public static void setForcePushed(LivingEntity entity) {
      FORCE_PUSHED.put(entity.getUuid(), true);
   }

   private static void tickStun(LivingEntity entity) {
      EntityAttributeInstance attr = entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (attr == null) {
         return;
      }

      boolean stunned = ForceEffects.has(entity, ForceEffects.STUN);
      boolean applied = STUN_APPLIED.getOrDefault(entity.getUuid(), false);
      EntityAttributeModifier modifier = attr.getModifier(STUN_MODIFIER_ID);
      if (stunned && modifier == null) {
         attr.addPersistentModifier(new EntityAttributeModifier(STUN_MODIFIER_ID, -1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
         STUN_APPLIED.put(entity.getUuid(), true);
      } else if (!stunned && (modifier != null || applied)) {
         if (modifier != null) {
            attr.removeModifier(STUN_MODIFIER_ID);
         }

         STUN_APPLIED.remove(entity.getUuid());
      }
   }

   private static void tickSpeed(LivingEntity entity) {
      EntityAttributeInstance attr = entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (attr == null) {
         return;
      }

      boolean boosted = ForceEffects.has(entity, ForceEffects.SPEED);
      EntityAttributeModifier modifier = attr.getModifier(SPEED_MODIFIER_ID);
      if (boosted && modifier == null) {
         attr.addPersistentModifier(new EntityAttributeModifier(SPEED_MODIFIER_ID, 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
      } else if (!boosted && modifier != null) {
         attr.removeModifier(SPEED_MODIFIER_ID);
      }
   }

   private static void tickStealthVisibility(ServerPlayerEntity player) {
      boolean hidden = ForceEffects.has(player, ForceEffects.STEALTH);
      if (hidden != player.isInvisible()) {
         player.setInvisible(hidden);
      }
   }

   public static void onPlayerDeath(ServerPlayerEntity player) {
      stopChannel(player, false);
      DRAIN_LIFE_TIMER.remove(player.getUuid());
      ForceEffects.clearAll(player);
   }

   private static void refreshStatus(ServerPlayerEntity player, Power power, String effect) {
      float force = ForceManager.getEnergy(player);
      int duration = (int)(force / (power.getUseCost(player) / 20.0F));
      ForceEffects.add(player, effect, duration, power.getTier() - 1, player);
   }

   private static LivingEntity raycastTarget(ServerPlayerEntity player, double range, double radius) {
      Vec3d start = player.getEyePos();
      Vec3d look = player.getRotationVec(1.0F);
      Vec3d end = start.add(look.multiply(range));
      BlockHitResult blockHit = player.getWorld().raycast(new RaycastContext(start, end, ShapeType.COLLIDER, FluidHandling.NONE, player));
      double maxDist = blockHit.getType() == Type.MISS ? range : blockHit.getPos().distanceTo(start);
      LivingEntity target = null;
      double best = Double.MAX_VALUE;

      for (Entity entity : player.getWorld().getOtherEntities(player, player.getBoundingBox().stretch(look.multiply(range)).expand(radius))) {
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (living.isAlive() && player.getVehicle() != living) {
               Vec3d toEntity = entity.getPos().add(0.0, (double)entity.getHeight() * 0.5, 0.0).subtract(start);
               double proj = toEntity.dotProduct(look);
               if (proj > 0.0 && proj < maxDist) {
                  Vec3d perp = toEntity.subtract(look.multiply(proj));
                  if (perp.lengthSquared() < radius * radius && proj < best) {
                     target = living;
                     best = proj;
                  }
               }
            }
         }
      }

      return target;
   }

   private static boolean executeEffect(ServerPlayerEntity player, ServerWorld world, Power power) {
      int tier = power.getTier();

      return switch (power.getBaseName()) {
         case "lightning" -> lightningTick(player, world, power);
         case "stealth" -> stealthTick(player, world, power);
         case "fortify" -> fortifyTick(player, world, power);
         case "push" -> push(player, world, tier);
         case "heal" -> heal(player, world, tier);
         case "wound" -> choke(player, world, tier);
         case "drain" -> drain(player, world, tier);
         case "stun" -> stun(player, world, tier);
         case "sight" -> sight(player, world, tier);
         case "speed" -> speed(player, world, tier);
         case "meditation" -> meditation(player, world, tier);
         case "resist" -> resist(player, world, tier);
         case "throw" -> throwSaber(player, world, tier);
         default -> false;
      };
   }

   private static void stopEffect(ServerPlayerEntity player, Power power) {
      switch (power.getBaseName()) {
         case "stealth" -> {
            ForceEffects.clear(player, ForceEffects.STEALTH);
            player.setInvisible(false);
            player.getWorld().playSound(null, player.getBlockPos(), ALSounds.FORCE_STEALTH_OFF, SoundCategory.PLAYERS, 1.0F, 1.0F);
         }
         case "fortify" -> ForceEffects.clear(player, ForceEffects.FORTIFY);
         case "lightning" -> ForceEffects.clear(player, ForceEffects.LIGHTNING);
         default -> {
         }
      }
   }

   private static boolean lightningTick(ServerPlayerEntity player, ServerWorld world, Power power) {
      int tier = power.getTier();
      refreshStatus(player, power, ForceEffects.LIGHTNING);
      LivingEntity struck = raycastTarget(player, 7.0, 1.2);
      if (struck != null) {
         struck.damage(ALDamageTypes.of(world, ALDamageTypes.FORCE_LIGHTNING), 4.0F + (tier - 1) * 2.0F);
         struck.setVelocity(0.0, Math.min(struck.getVelocity().y, 0.0), 0.0);
         struck.velocityModified = true;
      }

      return true;
   }

   private static boolean stealthTick(ServerPlayerEntity player, ServerWorld world, Power power) {
      refreshStatus(player, power, ForceEffects.STEALTH);
      player.setInvisible(true);
      return true;
   }

   private static boolean fortifyTick(ServerPlayerEntity player, ServerWorld world, Power power) {
      refreshStatus(player, power, ForceEffects.FORTIFY);
      return true;
   }

   private static boolean push(ServerPlayerEntity player, ServerWorld world, int tier) {
      LivingEntity target = raycastTarget(player, 16.0, 1.5);
      if (target != null) {
         Vec3d look = player.getRotationVec(1.0F);
         float knockback = 3.0F + (float)Math.pow(2.0, tier - 1);
         float damage = getPushDamage(tier);
         target.damage(ALDamageTypes.of(world, ALDamageTypes.FORCE), damage);
         setForcePushed(target);
         Vec3d offset = look.multiply(0.5 * knockback);
         target.addVelocity(offset.x, offset.y, offset.z);
         target.velocityModified = true;
      }

      world.spawnParticles(
         ParticleTypes.POOF, player.getX(), player.getY() + 1.0, player.getZ(), 12 + tier * 4, 1.0, 0.5, 1.0, 0.1
      );
      return target != null;
   }

   public static float getPushDamage(int tier) {
      float f = 1.0F;

      for (int i = 1; i < tier; ++i) {
         f *= f + 0.5F;
      }

      return f;
   }

   private static boolean speed(ServerPlayerEntity player, ServerWorld world, int tier) {
      ForceEffects.add(player, ForceEffects.SPEED, 100, 0, player);
      world.spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 10, 0.4, 0.2, 0.4, 0.05);
      return true;
   }

   private static boolean heal(ServerPlayerEntity player, ServerWorld world, int tier) {
      float amount = tier == 1 ? 4.0F : (tier == 2 ? 7.0F : 13.0F);
      player.heal(amount);
      if (tier >= 3) {
         for (Entity entity : world.getOtherEntities(player, player.getBoundingBox().expand(6.0))) {
            if (entity instanceof LivingEntity living && player.isTeammate(entity) && living.getHealth() < living.getMaxHealth()) {
               living.heal(7.0F);
               world.spawnParticles(ParticleTypes.END_ROD, living.getX(), living.getBodyY(0.5), living.getZ(), 2, 0.5, 0.5, 0.5, 0.0);
            }
         }
      }

      world.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getBodyY(0.5), player.getZ(), 2, 0.5, 0.5, 0.5, 0.0);
      return true;
   }

   private static boolean choke(ServerPlayerEntity player, ServerWorld world, int tier) {
      LivingEntity target = raycastTarget(player, 16.0, 1.2);
      if (target == null) {
         return false;
      }

      ForceEffects.add(target, ForceEffects.CHOKE, 60, tier - 1, player);
      return true;
   }

   private static boolean drain(ServerPlayerEntity player, ServerWorld world, int tier) {
      List<LivingEntity> targets = new java.util.ArrayList<>();
      if (tier < 3) {
         LivingEntity target = raycastTarget(player, 5.0, 1.2);
         if (target != null) {
            targets.add(target);
         }
      } else {
         for (Entity entity : world.getOtherEntities(player, player.getBoundingBox().expand(7.0))) {
            if (entity instanceof LivingEntity living && living.isAlive() && !player.isTeammate(entity)) {
               targets.add(living);
            }
         }
      }

      if (targets.isEmpty()) {
         return false;
      }

      for (LivingEntity target : targets) {
         ForceEffects.add(target, ForceEffects.DRAIN, 30, tier - 1, player);
      }

      DRAIN_LIFE_TIMER.put(player.getUuid(), 1.0F);
      return true;
   }

   private static boolean stun(ServerPlayerEntity player, ServerWorld world, int tier) {
      int duration = tier == 1 ? 40 : (tier == 2 ? 70 : 80);
      int count = 0;
      if (tier >= 3) {
         for (Entity entity : world.getOtherEntities(player, player.getBoundingBox().expand(10.0))) {
            if (entity instanceof LivingEntity living && living.isAlive() && !isAlly(player, living) && living != player) {
               applyStun(world, living, duration, 0);
               count++;
            }
         }
      } else {
         LivingEntity target = raycastTarget(player, 16.0, 1.2);
         if (target == null) {
            return false;
         }

         applyStun(world, target, duration, tier - 1);
         count = 1;
      }

      return count > 0;
   }

   private static void applyStun(ServerWorld world, LivingEntity target, int duration, int amplifier) {
      ForceEffects.add(target, ForceEffects.STUN, duration, amplifier, null);
      world.spawnParticles(
         ParticleTypes.CRIT,
         target.getX(),
         target.getY() + target.getHeight() * 0.5,
         target.getZ(),
         12,
         0.4,
         0.5,
         0.4,
         0.1
      );
   }

   private static boolean isAlly(ServerPlayerEntity player, LivingEntity entity) {
      if (entity instanceof MobEntity) {
         return false;
      }

      return player.isTeammate(entity);
   }

   private static boolean sight(ServerPlayerEntity player, ServerWorld world, int tier) {
      int duration = tier == 1 ? 200 : (tier == 2 ? 300 : 500);
      ForceEffects.add(player, ForceEffects.GAZE, duration, tier - 1, player);
      return true;
   }

   private static boolean meditation(ServerPlayerEntity player, ServerWorld world, int tier) {
      player.setAbsorptionAmount(4.0F + (tier - 1) * 2.0F);
      ForceEffects.add(player, ForceEffects.MEDITATION, 1800, tier - 1, player);
      world.spawnParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2, player.getZ(), 24, 0.5, 0.6, 0.5, 0.5);
      return true;
   }

   private static boolean resist(ServerPlayerEntity player, ServerWorld world, int tier) {
      int duration = tier == 1 ? 140 : (tier == 2 ? 180 : 280);
      ForceEffects.add(player, ForceEffects.RESIST, duration, tier - 1, player);
      world.spawnParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2, player.getZ(), 16, 0.4, 0.5, 0.4, 0.4);
      return true;
   }

   private static boolean throwSaber(ServerPlayerEntity player, ServerWorld world, int tier) {
      ItemStack held = player.getMainHandStack();
      if (!(held.getItem() instanceof LightsaberItem)) {
         player.sendMessage(Text.translatable("message.lightsabers.need_saber").formatted(Formatting.YELLOW), true);
         return false;
      }

      LightsaberComponent component = held.get(ALComponents.LIGHTSABER);
      if (component != null && component.active()) {
         ThrownLightsaberEntity entity = new ThrownLightsaberEntity(world, player, held.copy(), tier);
         entity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 2.0F, 0.0F);
         world.spawnEntity(entity);
         player.setStackInHand(player.getActiveHand(), ItemStack.EMPTY);
         world.playSound(null, player.getBlockPos(), ALSounds.LIGHTSABER_SWING, SoundCategory.PLAYERS, 1.0F, 1.0F);
         ALAdvancements.trigger(player, ALAdvancements.SABER_THROW);
         return true;
      }

      player.sendMessage(Text.translatable("message.lightsabers.saber_inactive").formatted(Formatting.YELLOW), true);
      return false;
   }

   public static boolean hasReboundCharge(ServerPlayerEntity player) {
      return ForceManager.hasPower(player, Power.REBOUND) && ForceManager.getEnergy(player) > 0.0F;
   }

   public static float applyReboundFall(ServerPlayerEntity player, float fallDamage) {
      float energy = ForceManager.getEnergy(player);
      int reduce = (int)Math.min(Math.floor(energy / 3.0F), Math.max(0.0F, fallDamage));
      if (reduce <= 0) {
         return fallDamage;
      }

      ForceManager.addEnergy(player, -3.0F * reduce);
      player.playSoundToPlayer(ALSounds.FORCE_CAST, SoundCategory.PLAYERS,
         Math.min(reduce / 10.0F + 0.2F, 1.0F), 1.0F);
      player.getServerWorld()
         .spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 10, 0.3, 0.1, 0.3, 0.02);
      return Math.max(0.0F, fallDamage - reduce);
   }
}
