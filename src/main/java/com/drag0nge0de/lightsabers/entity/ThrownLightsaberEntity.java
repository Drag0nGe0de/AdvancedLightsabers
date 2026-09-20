package com.drag0nge0de.lightsabers.entity;

import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALEntities;
import com.drag0nge0de.lightsabers.registry.ALSounds;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThrownLightsaberEntity extends ThrownEntity {
   private static final TrackedData<ItemStack> STACK = DataTracker.registerData(ThrownLightsaberEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
   private static final TrackedData<Integer> TIER = DataTracker.registerData(ThrownLightsaberEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Byte> RETURNING = DataTracker.registerData(ThrownLightsaberEntity.class, TrackedDataHandlerRegistry.BYTE);
   private static final int RETURN_DELAY_TICKS = 20;
   private static final int LIFETIME_TICKS = 200;
   private static final double RETURN_SPEED = 2.2;
   private final Set<UUID> hitEntities = new HashSet<>();

   public ThrownLightsaberEntity(EntityType<? extends ThrownLightsaberEntity> type, World world) {
      super(type, world);
   }

   public ThrownLightsaberEntity(World world, LivingEntity thrower, ItemStack saber, int tier) {
      super(ALEntities.THROWN_LIGHTSABER, world);
      this.setOwner(thrower);
      this.setPosition(thrower.getX(), thrower.getEyeY() - 0.2, thrower.getZ());
      this.dataTracker.set(STACK, saber);
      this.dataTracker.set(TIER, tier);
   }

   public ItemStack getSaberStack() {
      return (ItemStack)this.dataTracker.get(STACK);
   }

   public int getTier() {
      return (Integer)this.dataTracker.get(TIER);
   }

   public boolean isReturning() {
      return (Byte)this.dataTracker.get(RETURNING) > 0;
   }

   public void setReturning(boolean returning) {
      this.dataTracker.set(RETURNING, (byte)(returning ? 1 : 0));
   }

   protected void initDataTracker(Builder builder) {
      builder.add(STACK, ItemStack.EMPTY);
      builder.add(TIER, 1);
      builder.add(RETURNING, (byte)0);
   }

   protected double getGravity() {
      return 0.01;
   }

   public void tick() {
      LivingEntity thrower = this.getOwner() instanceof LivingEntity living ? living : null;
      if (!this.getWorld().isClient) {
         if (thrower == null || !thrower.isAlive() || this.age > 200) {

            if (!(thrower instanceof SithGhostEntity)) {
               this.dropStack(this.getSaberStack());
            }
            this.discard();
            return;
         }

         if (this.age % 3 == 0) {

            boolean isPlayer = thrower instanceof PlayerEntity;
            this.getWorld().playSound(null, this.getBlockPos(),
               isPlayer ? ALSounds.LIGHTSABER_SWING : ALSounds.MOB_LIGHTSABER_SWING,
               isPlayer ? SoundCategory.PLAYERS : SoundCategory.HOSTILE, 0.7F, 1.0F);
         }

         if (this.age > 20 && !this.isReturning()) {
            this.setReturning(true);
            this.hitEntities.clear();
         }

         if (this.isReturning()) {
            Vec3d target = thrower.getPos().add(0.0, (double)thrower.getHeight() * 0.6, 0.0);
            Vec3d toThrower = target.subtract(this.getPos());
            double dist = toThrower.length();
            if (dist <= 2.0) {
               this.recoverBy(thrower);
               this.discard();
               return;
            }

            Vec3d velocity = toThrower.normalize().multiply(2.2);
            this.setVelocity(velocity);
            this.velocityDirty = true;
            this.setPosition(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
            this.getWorld().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.4, this.getZ(), 0.0, 0.0, 0.0);
            return;
         }
      }

      super.tick();
   }

   private void recoverBy(LivingEntity thrower) {
      ItemStack stack = this.getSaberStack();
      if (thrower instanceof PlayerEntity player && !player.getInventory().insertStack(stack)) {
         player.dropItem(stack, false);
      } else if (!(thrower instanceof PlayerEntity)) {

         thrower.equipStack(EquipmentSlot.MAINHAND, stack);
      }
   }

   protected void onEntityHit(EntityHitResult hit) {
      super.onEntityHit(hit);
      if (!this.getWorld().isClient && hit.getEntity() instanceof LivingEntity target) {
         if (target != this.getOwner() && this.hitEntities.add(target.getUuid())) {
            if (this.getWorld() instanceof ServerWorld world) {
               DamageSource source = this.getOwner() instanceof ServerPlayerEntity player
                  ? com.drag0nge0de.lightsabers.damage.ALDamageTypes.of(world, com.drag0nge0de.lightsabers.damage.ALDamageTypes.LIGHTSABER, player)
                  : world.getDamageSources().thrown(this, this.getOwner());
               target.damage(source, 8.0F);
               if (this.getTier() >= 2) {
                  for (Entity near : world.getOtherEntities(this, this.getBoundingBox().expand(3.0))) {
                     if (near instanceof LivingEntity living && living != this.getOwner() && this.hitEntities.add(living.getUuid())) {
                        living.damage(source, 4.0F);
                     }
                  }
               }
            }

            this.getWorld().playSound(null, this.getBlockPos(), ALSounds.LIGHTSABER_HIT, SoundCategory.PLAYERS, 0.8F, 1.0F);
            target.takeKnockback(0.5, this.getX() - target.getX(), this.getZ() - target.getZ());
         }
      }
   }

   protected void onBlockHit(BlockHitResult hit) {
      super.onBlockHit(hit);
      if (!this.getWorld().isClient && !this.isReturning()) {
         this.setReturning(true);
         this.hitEntities.clear();
         Vec3d velocity = this.getVelocity().multiply(-0.3).add(0.0, 0.2, 0.0);
         this.setVelocity(velocity);
         this.velocityDirty = true;
      }
   }

   public boolean canUsePortals(boolean allowVehicles) {
      return true;
   }

   public boolean shouldRender(double distance) {
      return true;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.put("SaberItem", this.getSaberStack().encodeAllowEmpty(this.getRegistryManager()));
      nbt.putInt("Tier", this.getTier());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      ItemStack stack = ItemStack.fromNbtOrEmpty(this.getRegistryManager(), nbt.getCompound("SaberItem"));
      this.dataTracker.set(STACK, stack);
      this.dataTracker.set(TIER, nbt.getInt("Tier"));
   }
}
