package com.drag0nge0de.lightsabers.block.blockentity;

import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;

public class LightsaberStandBlockEntity extends BlockEntity {
   private ItemStack displayStack = ItemStack.EMPTY;
   private UUID owner;

   public LightsaberStandBlockEntity(BlockPos pos, BlockState state) {
      super(ALBlockEntities.LIGHTSABER_STAND, pos, state);
   }

   public ItemStack getDisplayStack() {
      return this.displayStack;
   }

   public void setDisplayStack(ItemStack stack) {
      this.displayStack = stack.copy();
      this.markDirty();
      this.syncToClients();
   }

   public boolean isOwner(PlayerEntity player) {
      return this.owner == null || player.getUuid().equals(this.owner);
   }

   public void setOwner(UUID owner) {
      this.owner = owner;
   }

   public void writeNbt(NbtCompound nbt, WrapperLookup registries) {
      super.writeNbt(nbt, registries);
      if (!this.displayStack.isEmpty()) {
         nbt.put("DisplayStack", this.displayStack.encodeAllowEmpty(registries));
      }

      if (this.owner != null) {
         nbt.putUuid("Owner", this.owner);
      }
   }

   public void readNbt(NbtCompound nbt, WrapperLookup registries) {
      super.readNbt(nbt, registries);
      this.displayStack = ItemStack.fromNbtOrEmpty(registries, nbt.getCompound("DisplayStack"));
      this.owner = nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null;
   }

   public Packet<ClientPlayPacketListener> toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   @Override
   public NbtCompound toInitialChunkDataNbt(WrapperLookup registries) {
      NbtCompound nbt = new NbtCompound();
      this.writeNbt(nbt, registries);

      nbt.putBoolean("ALSync", true);
      return nbt;
   }

   private void syncToClients() {
      if (this.world instanceof net.minecraft.server.world.ServerWorld serverWorld) {

         this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
         Packet<ClientPlayPacketListener> packet = this.toUpdatePacket();
         if (packet != null) {
            for (var player : serverWorld.getPlayers(p -> p.squaredDistanceTo(
                    this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) < 64 * 64)) {
               player.networkHandler.sendPacket(packet);
            }
         }
      }
   }
}
