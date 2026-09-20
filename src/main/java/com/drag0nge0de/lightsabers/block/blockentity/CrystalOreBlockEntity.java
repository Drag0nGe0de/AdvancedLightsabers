package com.drag0nge0de.lightsabers.block.blockentity;

import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrystalOreBlockEntity extends BlockEntity {
   private int color = -1;

   public CrystalOreBlockEntity(BlockPos pos, BlockState state) {
      super(ALBlockEntities.CRYSTAL_ORE, pos, state);
   }

   public int getColor() {
      return this.color;
   }

   public void setColor(int rgb) {
      this.color = rgb;
      this.markDirty();
   }

   public void ensureColor(World world) {
      if (this.color == -1) {
         this.color = CrystalColor.rollWeighted(world.getRandom()).rgb;
         this.markDirty();
      }
   }

   @Override
   public NbtCompound toInitialChunkDataNbt(WrapperLookup registries) {
      if (this.world != null) {
         this.ensureColor(this.world);
      }

      return this.createNbt(registries);
   }

   @Override
   public void writeNbt(NbtCompound nbt, WrapperLookup registries) {
      super.writeNbt(nbt, registries);
      nbt.putInt("Color", this.color);
   }

   @Override
   public void readNbt(NbtCompound nbt, WrapperLookup registries) {
      super.readNbt(nbt, registries);
      this.color = nbt.getInt("Color");
   }
}
