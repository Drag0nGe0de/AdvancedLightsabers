package com.drag0nge0de.lightsabers.block.blockentity;

import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HolocronBlockEntity extends BlockEntity {
   public float openTimer;
   public float prevOpenTimer;
   public int openTicks;
   public int prevOpenTicks;
   public boolean sith;
   private final Set<UUID> users = new HashSet<>();
   private int clientUsers;

   public HolocronBlockEntity(BlockPos pos, BlockState state) {
      super(ALBlockEntities.HOLOCRON, pos, state);
   }

   public boolean isSith() {
      return this.sith;
   }

   public void addUser(UUID uuid) {
      this.users.add(uuid);
   }

   public void removeUser(UUID uuid) {
      this.users.remove(uuid);
   }

   public void serverTick(World world, BlockPos pos, BlockState state) {
      this.prevOpenTimer = this.openTimer;
      this.prevOpenTicks = this.openTicks;

      this.users.removeIf(uuid -> world.getServer() == null
            || world.getServer().getPlayerManager().getPlayer(uuid) == null);
      tickTimer(this.users.size() > 0);
   }

   public void clientAddUser(int delta) {
      this.clientUsers = Math.max(0, this.clientUsers + delta);
   }

   public void clientTick() {
      this.prevOpenTimer = this.openTimer;
      this.prevOpenTicks = this.openTicks;
      tickTimer(this.clientUsers > 0);
   }

   private void tickTimer(boolean open) {
      if (!open) {
         this.openTimer *= 0.85F;
      } else if (this.openTimer < 1.0F) {
         this.openTimer += 0.05F;
         this.openTimer *= 1.05F;
      }

      if (this.openTimer < 1.0E-6F) {
         this.openTimer = 0.0F;
      }

      if (this.openTimer == 0.0F) {
         this.openTicks = 0;
      } else if (this.openTimer >= 1.0F) {
         ++this.openTicks;
      }

      this.openTimer = MathHelper_clamp(this.openTimer, 0.0F, 1.0F);
   }

   private static float MathHelper_clamp(float v, float min, float max) {
      return v < min ? min : Math.min(v, max);
   }

   public void writeNbt(NbtCompound nbt, WrapperLookup registries) {
      super.writeNbt(nbt, registries);
      nbt.putBoolean("Sith", this.sith);
   }

   public void readNbt(NbtCompound nbt, WrapperLookup registries) {
      super.readNbt(nbt, registries);
      this.sith = nbt.getBoolean("Sith");
   }
}
