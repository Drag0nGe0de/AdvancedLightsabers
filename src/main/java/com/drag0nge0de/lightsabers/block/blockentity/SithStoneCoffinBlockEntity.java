package com.drag0nge0de.lightsabers.block.blockentity;

import java.util.List;

import com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock;
import com.drag0nge0de.lightsabers.entity.SithGhostEntity;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import com.drag0nge0de.lightsabers.registry.ALEntities;
import com.drag0nge0de.lightsabers.registry.ALComponents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SithStoneCoffinBlockEntity extends BlockEntity {

   private static final int WAKE_RADIUS = 14;
   private static final int LINK_RANGE = 48;
   private static final int LINK_RETRY_TICKS = 200;

   private ItemStack equipment = ItemStack.EMPTY;
   private boolean taskFinished = false;
   private BlockPos mainCoffinPos = null;
   private boolean linkResolved = false;

   public SithStoneCoffinBlockEntity(BlockPos pos, BlockState state) {
      super(ALBlockEntities.SITH_STONE_COFFIN, pos, state);
   }

   public ItemStack getEquipment() {
      return equipment;
   }

   public void setEquipment(ItemStack stack) {
      this.equipment = stack == null ? ItemStack.EMPTY : stack;
      this.markDirty();
   }

   public boolean isOpen() {
      return this.getCachedState().contains(SithStoneCoffinBlock.OPEN) && this.getCachedState().get(SithStoneCoffinBlock.OPEN);
   }

   public boolean isTaskFinished() {
      return taskFinished;
   }

   public void setTaskFinished(boolean finished) {
      this.taskFinished = finished;
      this.markDirty();
   }

   public static void serverTick(World world, BlockPos pos, BlockState state, SithStoneCoffinBlockEntity coffin) {
      if (!coffin.linkResolved || (coffin.mainCoffinPos == null && world.getTime() % LINK_RETRY_TICKS == 0)) {
         coffin.resolveLink(world);
      }

      if (state.get(SithStoneCoffinBlock.OPEN)) {
         return;
      }

      if (!coffin.taskFinished && coffin.mainCoffinPos != null && world.getTime() % 20 == 0) {
         Box box = new Box(coffin.mainCoffinPos).expand(WAKE_RADIUS, 2, WAKE_RADIUS);
         List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, box, p -> true);

         if (!players.isEmpty()) {
            coffin.wake(world, pos, players.get(0));
         }
      }

      if (world.isReceivingRedstonePower(pos)) {
         coffin.wake(world, pos, null);
      }
   }

   private void resolveLink(World world) {
      BlockPos closest = null;
      double best = Double.MAX_VALUE;
      BlockPos min = new BlockPos(this.pos.getX() - LINK_RANGE, Math.max(world.getBottomY(), this.pos.getY() - 12), this.pos.getZ() - LINK_RANGE);
      BlockPos max = new BlockPos(this.pos.getX() + LINK_RANGE, Math.min(world.getTopY() - 1, this.pos.getY() + 12), this.pos.getZ() + LINK_RANGE);

      for (BlockPos candidate : BlockPos.iterate(min, max)) {
         if (world.getBlockState(candidate).isOf(ALBlocks.SITH_SARCOPHAGUS)) {
            double distance = candidate.getSquaredDistance(this.pos);

            if (distance < best) {
               best = distance;
               closest = candidate.toImmutable();
            }
         }
      }

      linkResolved = true;

      if (closest != null) {
         mainCoffinPos = closest;
         markDirty();
      }
   }

   private void wake(World world, BlockPos pos, PlayerEntity target) {
      if (isOpen() || !(world instanceof ServerWorld serverWorld)) {
         return;
      }

      taskFinished = true;
      markDirty();
      world.setBlockState(pos, world.getBlockState(pos).with(SithStoneCoffinBlock.OPEN, true), Block.NOTIFY_LISTENERS);
      spawnGhost(serverWorld, pos, target);
   }

   private void spawnGhost(ServerWorld world, BlockPos pos, PlayerEntity target) {
      SithGhostEntity ghost = ALEntities.SITH_GHOST.create(world);

      if (ghost == null) {
         return;
      }

      Direction facing = this.getCachedState().get(SithStoneCoffinBlock.FACING);
      ghost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.1875, pos.getZ() + 0.5,
         facing.asRotation(), 0.0F);
      ghost.initialize(world, world.getLocalDifficulty(pos), SpawnReason.MOB_SUMMONED, null);

      if (!this.equipment.isEmpty()) {
         ItemStack stored = this.equipment.copy();

         if (stored.contains(ALComponents.LIGHTSABER)
               && stored.get(ALComponents.LIGHTSABER).active()) {
            LightsaberItem.setActive(stored, false);
         }

         ghost.equipStack(EquipmentSlot.MAINHAND, stored);
      }

      this.setOpenState(world);
      markDirty();
      ghost.setRestingPlace(this.pos.toImmutable());
      if (target != null) {
         ghost.setTarget(target);
      }

      world.spawnEntity(ghost);
   }

   private void setOpenState(World world) {
      BlockState state = world.getBlockState(this.pos);

      if (state.contains(SithStoneCoffinBlock.OPEN) && !state.get(SithStoneCoffinBlock.OPEN)) {
         world.setBlockState(this.pos, state.with(SithStoneCoffinBlock.OPEN, true), Block.NOTIFY_LISTENERS);
      }
   }

   public boolean punchRetrieve(ServerWorld world, BlockPos pos, PlayerEntity player) {
      if (this.isOpen() || !this.taskFinished) {
         return false;
      }

      BlockState state = world.getBlockState(pos);
      world.syncWorldEvent(null, 2001, pos, Block.getRawIdFromState(state));

      ItemStack stack = new ItemStack(ALBlocks.SITH_STONE_COFFIN.asItem());

      if (!this.equipment.isEmpty()) {
         stack.set(ALComponents.COFFIN_EQUIPMENT, java.util.List.of(this.equipment.copy()));
         this.equipment = ItemStack.EMPTY;
      }

      world.removeBlock(pos, false);

      net.minecraft.entity.ItemEntity item = new net.minecraft.entity.ItemEntity(
         world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
      world.spawnEntity(item);
      return true;
   }

   @Override
   protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
      super.writeNbt(nbt, registryLookup);

      if (!equipment.isEmpty()) {
         nbt.put("Equipment", equipment.encodeAllowEmpty(registryLookup));
      }

      nbt.putBoolean("TaskFinished", taskFinished);

      if (mainCoffinPos != null) {
         nbt.putInt("CoffinX", mainCoffinPos.getX());
         nbt.putInt("CoffinY", mainCoffinPos.getY());
         nbt.putInt("CoffinZ", mainCoffinPos.getZ());
      }
   }

   @Override
   public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
      super.readNbt(nbt, registryLookup);
      equipment = nbt.contains("Equipment")
         ? ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound("Equipment"))
         : ItemStack.EMPTY;
      taskFinished = nbt.getBoolean("TaskFinished");

      if (nbt.contains("CoffinX")) {
         mainCoffinPos = new BlockPos(nbt.getInt("CoffinX"), nbt.getInt("CoffinY"), nbt.getInt("CoffinZ"));
      }
   }

   @Override
   public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
      NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
      nbt.putBoolean("ALSync", true);
      return nbt;
   }
}
