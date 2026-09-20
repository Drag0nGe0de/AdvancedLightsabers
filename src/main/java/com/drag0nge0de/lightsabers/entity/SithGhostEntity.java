package com.drag0nge0de.lightsabers.entity;

import java.util.EnumSet;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.block.blockentity.SithSarcophagusBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.SithStoneCoffinBlockEntity;
import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.FocusingCrystalType;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALEntities;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.registry.ALSounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.LongDoorInteractGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class SithGhostEntity extends HostileEntity {

   public boolean hasRestingPlace;
   public int restX;
   public int restY;
   public int restZ;

   public int throwLightsaberCooldown;
   public int swingItemCooldown;
   public int strafeTimer;
   public int strafe = 1;
   public int taskFinished;

   public void setRestingPlace(BlockPos pos) {
      this.hasRestingPlace = true;
      this.restX = pos.getX();
      this.restY = pos.getY();
      this.restZ = pos.getZ();
   }

   public SithGhostEntity(EntityType<? extends SithGhostEntity> type, World world) {
      super(type, world);
      this.experiencePoints = 5;
   }

   public static DefaultAttributeContainer.Builder createAttributes() {
      return HostileEntity.createHostileAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.6)
         .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
   }

   @Override
   protected void initGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(0, new BreakBlockGoal(this));
      this.goalSelector.add(1, new MeleeAttackGoal(this, 0.5, true));
      this.goalSelector.add(2, new RestGoal(this, 0.4));

      this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 4.0F, 10.0F));
      this.goalSelector.add(4, new LongDoorInteractGoal(this, true));
      this.goalSelector.add(5, new GoToWalkTargetGoal(this, 0.6));
      this.targetSelector.add(1, new RevengeGoal(this));

      this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 1, false, false, (java.util.function.Predicate<LivingEntity>)null));
   }

   @Override
   protected EntityNavigation createNavigation(World world) {
      MobNavigation navigation = new MobNavigation(this, world);

      navigation.setCanPathThroughDoors(true);
      navigation.setCanEnterOpenDoors(true);
      return navigation;
   }

   @Override
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
         EntityData entityData) {
      EntityData data = super.initialize(world, difficulty, spawnReason, entityData);

      if (this.getMainHandStack().isEmpty()) {
         this.equipStack(EquipmentSlot.MAINHAND, createRandomRedSaber(this.random));
      }

      for (EquipmentSlot slot : EquipmentSlot.values()) {
         this.setEquipmentDropChance(slot, 0.0F);
      }

      this.age = -this.random.nextInt(20);
      return data;
   }

   public static ItemStack createRandomRedSaber(Random random) {
      ItemStack saber = new ItemStack(ALItems.LIGHTSABER);
      Hilt hilt = Hilt.values()[random.nextInt(Hilt.values().length)];
      int focusing = 0;

      if (random.nextInt(10) == 0) {
         int first = randomFocusingBit(random);
         focusing |= first;

         if (random.nextInt(20) == 0) {
            int second;
            do {
               second = randomFocusingBit(random);
            } while (second == first);
            focusing |= second;
         }
      }

      saber.set(ALComponents.LIGHTSABER, new LightsaberComponent(false, hilt.getId(), CrystalColor.RED.rgb, false, focusing));
      saber.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));
      return saber;
   }

   private static int randomFocusingBit(Random random) {
      return 1 << random.nextInt(FocusingCrystalType.values().length);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.getWorld().isClient) {
         return;
      }

      LivingEntity target = this.getTarget();
      ItemStack heldItem = this.getMainHandStack();

      if (target != null && !target.isAlive()) {
         this.setTarget(null);
         target = null;
      }

      if (this.throwLightsaberCooldown > 0) {
         --this.throwLightsaberCooldown;
      }

      if (this.swingItemCooldown > 0) {
         --this.swingItemCooldown;
      }

      if (--this.strafeTimer <= 0) {
         this.strafe *= -1;
         this.strafeTimer = 100 + this.random.nextInt(1000);
      }

      if (!heldItem.isEmpty()) {

         if (this.age > 5 && !isSaberActive(heldItem)) {
            igniteHeldSaber();
         }

         if (target != null && target.isAlive()) {
            this.getLookControl().lookAt(target, 100.0F, 100.0F);

            double distanceSq = this.squaredDistanceTo(target);
            boolean canSee = this.canSee(target);

            if (isSaberActive(heldItem) && distanceSq > 25.0 && canSee && this.throwLightsaberCooldown == 0) {
               this.throwLightsaberCooldown = 40 + this.random.nextInt(60);
               this.swingHand(Hand.MAIN_HAND);
               throwSaber(target, heldItem);
            }

            if (distanceSq < 25.0 && canSee) {
               double yawRad = Math.toRadians(this.getBodyYaw());
               float input = 0.3F * this.strafe;
               float accel = this.isOnGround()
                  ? input * (float)this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 0.7535F
                  : input * 0.02F;
               this.addVelocity(Math.cos(yawRad) * accel, 0, Math.sin(yawRad) * accel);
            }
         }
      }

      if (target instanceof SithGhostEntity) {
         PlayerEntity player = this.getWorld().getClosestPlayer(this.getX(), this.getY(), this.getZ(), 32.0,
            e -> e instanceof PlayerEntity p && p.isAlive() && !p.isSpectator() && !p.isCreative());
         if (player != null) {
            this.setTarget(player);
         }
      }

      if (target != null) {
         if (this.taskFinished != 1 && Boolean.getBoolean("al.behaviorTest")) {
            AL.LOGGER.info("[AL-GHOST] #{} task->1 at {}", this.getId(), this.getBlockPos().toShortString());
         }

         this.taskFinished = 1;
      } else if (this.taskFinished == 1) {
         if (Boolean.getBoolean("al.behaviorTest")) {
            AL.LOGGER.info("[AL-GHOST] #{} task->2 (target lost) at {}", this.getId(), this.getBlockPos().toShortString());
         }

         this.taskFinished = 2;
      }

      if (this.taskFinished == 2 && this.hasRestingPlace
            && this.squaredDistanceTo(this.restX, this.restY, this.restZ) <= 4.0) {
         if (Boolean.getBoolean("al.behaviorTest")) {
            AL.LOGGER.info("[AL-GHOST] #{} depositing at rest {}", this.getId(), this.getBlockPos().toShortString());
         }

         this.taskFinished = 3;
         BlockEntity tile = this.getWorld().getBlockEntity(new BlockPos(this.restX, this.restY, this.restZ));

         if (tile instanceof SithSarcophagusBlockEntity sarcophagus) {
            depositSaber(sarcophagus);

            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
               SoundCategory.HOSTILE, 1.0F, 1.0F);
         } else if (tile instanceof SithStoneCoffinBlockEntity stoneCoffin) {
            depositIntoStoneCoffin(stoneCoffin);

            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
               SoundCategory.HOSTILE, 1.0F, 1.0F);
         }

         this.discard();
      }
   }

   private void throwSaber(LivingEntity target, ItemStack heldItem) {
      ThrownLightsaberEntity saber = new ThrownLightsaberEntity(this.getWorld(), this, heldItem.copy(), 1);
      Vec3d delta = target.getEyePos().subtract(this.getEyePos());
      double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
      float pitch = -(float)(Math.toDegrees(Math.atan2(delta.y, horizontal)));
      float yaw = (float)(Math.toDegrees(Math.atan2(-delta.x, delta.z)));
      saber.setVelocity(this, pitch, yaw, 0.0F, 2.0F, 0.0F);
      this.getWorld().spawnEntity(saber);
      this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
   }

   private void depositIntoStoneCoffin(SithStoneCoffinBlockEntity coffin) {
      ItemStack heldItem = this.getMainHandStack();

      if (Boolean.getBoolean("al.behaviorTest")) {
         AL.LOGGER.info("[AL-GHOST] #{} deposit hand={} into stone coffin {}", this.getId(),
            heldItem.getItem(), coffin.getPos().toShortString());
      }

      if (!heldItem.isEmpty()) {
         if (heldItem.contains(ALComponents.LIGHTSABER) && heldItem.get(ALComponents.LIGHTSABER).active()) {
            LightsaberItem.setActive(heldItem, false);
         }

         coffin.setEquipment(heldItem);
         this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      }

      if (coffin.getCachedState().contains(com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock.OPEN)) {
         coffin.getWorld().setBlockState(coffin.getPos(),
            coffin.getCachedState().with(com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock.OPEN, false),
            net.minecraft.block.Block.NOTIFY_LISTENERS);
      }
   }

   private void depositSaber(SithSarcophagusBlockEntity sarcophagus) {
      ItemStack heldItem = this.getMainHandStack();

      if (Boolean.getBoolean("al.behaviorTest")) {
         AL.LOGGER.info("[AL-GHOST] #{} deposit hand={} into {}", this.getId(),
            heldItem.getItem(), sarcophagus.getPos().toShortString());
      }

      if (!heldItem.isEmpty()) {
         boolean stored = false;

         for (int i = 0; i < sarcophagus.size(); i++) {
            if (sarcophagus.getStack(i).isEmpty()) {
               sarcophagus.setStack(i, heldItem);
               stored = true;
               break;
            }
         }

         if (!stored) {

            this.dropStack(heldItem);
         }

         sarcophagus.markDirty();
         this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      }
   }

   private static boolean isSaberActive(ItemStack stack) {
      LightsaberComponent component = stack.get(ALComponents.LIGHTSABER);
      return component != null && component.active();
   }

   private void igniteHeldSaber() {
      ItemStack heldItem = this.getMainHandStack();

      if (heldItem.getItem() instanceof LightsaberItem) {
         this.getWorld().playSound(null, this.getBlockPos(), ALSounds.MOB_LIGHTSABER_ON,
            SoundCategory.HOSTILE, 1.0F, 1.0F);
         LightsaberItem.setActive(heldItem, true);
      }
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return ALSounds.SITH_GHOST_IDLE;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return ALSounds.SITH_GHOST_DEATH;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return null;
   }

   @Override
   public boolean tryAttack(Entity target) {
      if (target instanceof SithGhostEntity) {
         return false;
      }

      return super.tryAttack(target);
   }

   @Override
   public void swingHand(Hand hand) {

      if (this.swingItemCooldown == 0) {
         this.swingItemCooldown = 5;
         super.swingHand(hand);
      }
   }

   @Override
   public boolean damage(DamageSource source, float amount) {

      if (source.isOf(DamageTypes.IN_WALL)) {
         return false;
      }

      return super.damage(source, Math.min(amount, 10.0F));
   }

   @Override
   public void remove(RemovalReason reason) {
      if ((reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED)
            && this.getWorld() instanceof ServerWorld serverWorld) {
         serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE,
            this.getX(), this.getBoundingBox().minY + this.getHeight() / 2.0, this.getZ(),
            128, this.getWidth() * 1.2, this.getHeight() * 0.6, this.getWidth() * 1.2, 0.0);
      }

      super.remove(reason);
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("HasRestingPlace", this.hasRestingPlace);
      nbt.putInt("RestX", this.restX);
      nbt.putInt("RestY", this.restY);
      nbt.putInt("RestZ", this.restZ);
      nbt.putInt("ThrowCooldown", this.throwLightsaberCooldown);
      nbt.putInt("SwingCooldown", this.swingItemCooldown);
      nbt.putInt("StrafeTimer", this.strafeTimer);
      nbt.putInt("Strafe", this.strafe);
      nbt.putInt("TaskFinished", this.taskFinished);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.hasRestingPlace = nbt.getBoolean("HasRestingPlace");
      this.restX = nbt.getInt("RestX");
      this.restY = nbt.getInt("RestY");
      this.restZ = nbt.getInt("RestZ");
      this.throwLightsaberCooldown = nbt.getInt("ThrowCooldown");
      this.swingItemCooldown = nbt.getInt("SwingCooldown");
      this.strafeTimer = nbt.getInt("StrafeTimer");
      this.strafe = nbt.getInt("Strafe");
      this.taskFinished = nbt.getInt("TaskFinished");
   }

   class BreakBlockGoal extends Goal {

      private final SithGhostEntity ghost;
      private int breakingTime;
      private int lastProgress = -1;
      private int targetX;
      private int targetY;
      private int targetZ;
      private BlockState targetBlock;

      BreakBlockGoal(SithGhostEntity ghost) {
         this.ghost = ghost;

      }

      @Override
      public boolean canStart() {
         if (!this.ghost.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
         }

         if (!this.ghost.horizontalCollision) {
            return false;
         }

         Path path = this.ghost.getNavigation().getCurrentPath();

         if (path != null && !path.isFinished()) {
            int end = Math.min(path.getCurrentNodeIndex() + 2, path.getLength());

            for (int i = 0; i < end; ++i) {
               PathNode node = path.getNode(i);
               this.targetX = node.x;
               this.targetY = node.y + 1;
               this.targetZ = node.z;

               if (this.ghost.squaredDistanceTo(this.targetX, this.ghost.getY(), this.targetZ) <= 2.25) {
                  this.targetBlock = this.pickBlock();

                  if (this.targetBlock != null) {
                     return true;
                  }
               }
            }
         }

         for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos neighbour = this.ghost.getBlockPos().offset(direction);

            for (int dy = 0; dy <= 1; dy++) {
               BlockPos probe = neighbour.up(dy);
               BlockState state = this.ghost.getWorld().getBlockState(probe);

               if (state.isAir()) {
                  continue;
               }

               if (state.getHardness(this.ghost.getWorld(), probe) < 0) {
                  continue;
               }

               if (!state.getCollisionShape(this.ghost.getWorld(), probe).isEmpty()) {
                  this.targetX = probe.getX();
                  this.targetY = probe.getY();
                  this.targetZ = probe.getZ();
                  this.targetBlock = state;
                  return true;
               }
            }
         }

         this.targetX = MathHelper.floor(this.ghost.getX());
         this.targetY = MathHelper.floor(this.ghost.getY() + 1.0);
         this.targetZ = MathHelper.floor(this.ghost.getZ());
         this.targetBlock = this.pickBlock();
         return this.targetBlock != null;
      }

      private BlockState pickBlock() {
         BlockPos pos = new BlockPos(this.targetX, this.targetY, this.targetZ);
         BlockState state = this.ghost.getWorld().getBlockState(pos);
         return state.getHardness(this.ghost.getWorld(), pos) < 0 ? null : state;
      }

      @Override
      public boolean shouldContinue() {
         return this.breakingTime <= 60;
      }

      @Override
      public void start() {
         this.breakingTime = 0;
         this.lastProgress = -1;
      }

      @Override
      public void stop() {
         this.ghost.getWorld().setBlockBreakingInfo(this.ghost.getId(),
            new BlockPos(this.targetX, this.targetY, this.targetZ), -1);
      }

      @Override
      public void tick() {
         ++this.breakingTime;
         BlockPos pos = new BlockPos(this.targetX, this.targetY, this.targetZ);
         int progress = (int)(this.breakingTime / 60.0F * 10.0F);

         if (progress != this.lastProgress) {
            this.ghost.getWorld().setBlockBreakingInfo(this.ghost.getId(), pos, progress);
            this.lastProgress = progress;
         }

         if (this.breakingTime == 60 && this.ghost.getWorld().getDifficulty() == Difficulty.HARD) {
            this.ghost.getWorld().removeBlock(pos, false);
            this.ghost.getWorld().syncWorldEvent(null, WorldEvents.BLOCK_BROKEN, pos,
               Block.getRawIdFromState(this.targetBlock));
         }
      }
   }

   class RestGoal extends Goal {

      private final SithGhostEntity ghost;
      private final double speed;

      RestGoal(SithGhostEntity ghost, double speed) {
         this.ghost = ghost;
         this.speed = speed;
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      @Override
      public boolean canStart() {
         return this.ghost.taskFinished == 2 && this.ghost.hasRestingPlace;
      }

      @Override
      public boolean shouldContinue() {
         return this.ghost.taskFinished == 2 && this.ghost.hasRestingPlace;
      }

      @Override
      public void start() {
         this.ghost.getNavigation().startMovingTo(this.ghost.restX + 0.5, this.ghost.restY,
            this.ghost.restZ + 0.5, this.speed);
      }
   }
}
