package com.drag0nge0de.lightsabers.block.blockentity;

import java.util.List;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.block.SithSarcophagusBlock;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.entity.SithGhostEntity;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALEntities;
import com.drag0nge0de.lightsabers.registry.ALSounds;
import com.drag0nge0de.lightsabers.screen.SithSarcophagusScreenHandler;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class SithSarcophagusBlockEntity extends LootableContainerBlockEntity {

   public static final int LID_OPEN_MAX = 60;

   private DefaultedList<ItemStack> stacks = DefaultedList.ofSize(28, ItemStack.EMPTY);
   private boolean guardSpawned = false;
   public boolean isLidOpen = false;
   public int lidOpenTimer = 0;
   public int prevLidOpenTimer = 0;
   private boolean hasBeenOpened = false;
   private int syncedTimer = -1;

   public SithSarcophagusBlockEntity(BlockPos pos, BlockState state) {
      super(ALBlockEntities.SITH_SARCOPHAGUS, pos, state);

      this.setLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, AL.id("chests/sith_coffin")));
   }

   @Override
   public int size() {
      return 28;
   }

   @Override
   protected DefaultedList<ItemStack> getHeldStacks() {
      return stacks;
   }

   @Override
   protected void setHeldStacks(DefaultedList<ItemStack> stacks) {
      this.stacks = stacks;
   }

   @Override
   protected Text getContainerName() {
      return Text.translatable("gui.lightsabers.sith_coffin");
   }

   @Override
   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new SithSarcophagusScreenHandler(syncId, playerInventory, this);
   }

   @Override
   public BlockEntityType<?> getType() {
      return ALBlockEntities.SITH_SARCOPHAGUS;
   }

   public static void serverTick(World world, BlockPos pos, BlockState state, SithSarcophagusBlockEntity sarcophagus) {
      sarcophagus.prevLidOpenTimer = sarcophagus.lidOpenTimer;

      if (!sarcophagus.isLidOpen) {
         if (sarcophagus.lidOpenTimer > 0) {
            sarcophagus.lidOpenTimer--;
         }
      } else if (sarcophagus.lidOpenTimer < LID_OPEN_MAX) {
         sarcophagus.lidOpenTimer++;
      }

      if (sarcophagus.lidOpenTimer != sarcophagus.syncedTimer) {
         sarcophagus.syncedTimer = sarcophagus.lidOpenTimer;
         ALNetwork.sendToTracking((ServerWorld) world, pos,
            new ALNetwork.SyncLidPayload(pos, sarcophagus.lidOpenTimer));
      }

      if (!sarcophagus.hasBeenOpened) {
         if (sarcophagus.lidOpenTimer < LID_OPEN_MAX) {
            if (sarcophagus.lidOpenTimer > 0) {
               sarcophagus.spawnLidParticles((ServerWorld) world, pos, state);
            }
         } else {
            sarcophagus.hasBeenOpened = true;
         }
      }
   }

   private void spawnLidParticles(ServerWorld world, BlockPos pos, BlockState state) {
      double radius = 0.5D;
      Direction facing = state.contains(SithSarcophagusBlock.FACING)
         ? state.get(SithSarcophagusBlock.FACING) : Direction.NORTH;

      for (double y = 0; y <= lidOpenTimer / 5F; y += 0.025) {
         Random rand = world.getRandom();
         double d = Math.cos(y * 2D);
         double x = radius * Math.cos(y + lidOpenTimer / 2F) * d;
         double z = radius * Math.sin(y + lidOpenTimer / 2F) * d;
         double motionX = (rand.nextFloat() - 0.5F) * 0.5F * d;
         double motionY = (rand.nextFloat() - 0.5F) * 0.1F;
         double motionZ = (rand.nextFloat() - 0.5F) * 0.5F * d;
         world.spawnParticles(ParticleTypes.SMOKE,
            pos.getX() + 0.5F + x + facing.getOffsetX() * 0.5F,
            pos.getY() + 0.8F + y,
            pos.getZ() + 0.5F + z + facing.getOffsetZ() * 0.5F,
            1, motionX, motionY, motionZ, 0.0D);
      }
   }

   public void interact(PlayerEntity player, boolean sneaking) {
      if (this.lidOpenTimer == 0) {
         this.isLidOpen = true;
         this.markDirty();

         if (this.world != null) {
            this.world.playSound(null, this.pos, ALSounds.SARCOPHAGUS_OPEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }
      } else if (this.lidOpenTimer >= LID_OPEN_MAX) {
         if (this.hasBeenOpened && !sneaking) {
            if (this.world != null) {
               player.openHandledScreen(this);
            }
         } else {
            this.isLidOpen = false;
            this.markDirty();

            if (this.world != null) {
               this.world.playSound(null, this.pos, ALSounds.SARCOPHAGUS_CLOSE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
         }
      }
   }

   public float getLidOpenTimer(float partialTicks) {
      float f = lidOpenTimer - prevLidOpenTimer;
      return (prevLidOpenTimer + f * partialTicks) / LID_OPEN_MAX;
   }

   public void syncLid(int timer) {
      this.prevLidOpenTimer = this.lidOpenTimer;
      this.lidOpenTimer = timer;
   }

   public SithGhostEntity summonGhost(PlayerEntity target) {
      World world = this.world;

      if (this.guardSpawned || !(world instanceof ServerWorld serverWorld)) {
         return null;
      }

      this.guardSpawned = true;
      this.markDirty();
      world.playSound(null, this.pos, ALSounds.SARCOPHAGUS_OPEN, SoundCategory.BLOCKS, 0.8F, 0.7F);

      SithGhostEntity ghost = ALEntities.SITH_GHOST.create(serverWorld);

      if (ghost == null) {
         return null;
      }

      Direction facing = this.getCachedState().get(SithSarcophagusBlock.FACING);
      ghost.refreshPositionAndAngles(this.pos.getX() + 0.5, this.pos.getY() + 0.1875, this.pos.getZ() + 0.5,
         facing.asRotation(), 0.0F);
      ghost.initialize(serverWorld, world.getLocalDifficulty(this.pos), SpawnReason.MOB_SUMMONED, null);

      int saberSlot = this.findSaberSlot();

      if (saberSlot >= 0) {

         ItemStack stored = this.stacks.get(saberSlot);

         if (stored.contains(ALComponents.LIGHTSABER) && stored.get(ALComponents.LIGHTSABER).active()) {
            LightsaberItem.setActive(stored, false);
         }

         this.stacks.set(saberSlot, ItemStack.EMPTY);
         ghost.equipStack(EquipmentSlot.MAINHAND, stored);
      }

      ghost.setRestingPlace(this.pos);
      if (target != null) {
         ghost.setTarget(target);
      }

      serverWorld.spawnEntity(ghost);

      if (Boolean.getBoolean("al.behaviorTest")) {
         AL.LOGGER.info("[AL-GHOST] summon: ghost #{} from {} rest={} target={}", ghost.getId(),
            this.pos.toShortString(), ghost.hasRestingPlace, target);
      }

      return ghost;
   }

   public void markGuardSpawned() {
      this.guardSpawned = true;
      this.markDirty();
   }

   private int findSaberSlot() {
      for (int i = 0; i < this.stacks.size(); i++) {
         ItemStack stack = this.stacks.get(i);

         if (stack.contains(ALComponents.LIGHTSABER)) {
            return i;
         }
      }

      return -1;
   }

   @Override
   protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
      super.writeNbt(nbt, registryLookup);
      if (!this.writeLootTable(nbt)) {
         net.minecraft.inventory.Inventories.writeNbt(nbt, this.stacks, registryLookup);
      }

      nbt.putBoolean("GuardSpawned", this.guardSpawned);
      nbt.putBoolean("HasBeenOpened", this.hasBeenOpened);
   }

   @Override
   public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
      super.readNbt(nbt, registryLookup);
      if (!this.readLootTable(nbt)) {
         this.setLootTable(null);
         net.minecraft.inventory.Inventories.readNbt(nbt, this.stacks, registryLookup);
      }

      this.guardSpawned = nbt.getBoolean("GuardSpawned");
      this.hasBeenOpened = nbt.getBoolean("HasBeenOpened");

      if (nbt.contains("LidOpenTimer")) {
         this.prevLidOpenTimer = nbt.getInt("LidOpenTimer");
         this.lidOpenTimer = this.prevLidOpenTimer;
      }
   }

   @Override
   public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
      NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
      nbt.putInt("LidOpenTimer", this.lidOpenTimer);
      return nbt;
   }
}
