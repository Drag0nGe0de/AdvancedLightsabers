package com.drag0nge0de.lightsabers.item;

import com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock;
import com.drag0nge0de.lightsabers.block.blockentity.SithStoneCoffinBlockEntity;
import com.drag0nge0de.lightsabers.registry.ALComponents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SithStoneCoffinBlockItem extends BlockItem {

   public SithStoneCoffinBlockItem(Block block, Settings settings) {
      super(block, settings);
   }

   @Override
   protected boolean postPlacement(BlockPos pos, World world, net.minecraft.entity.player.PlayerEntity player,
         ItemStack stack, net.minecraft.block.BlockState state) {
      if (!world.isClient) {
         BlockState upper = world.getBlockState(pos.up());

         if (upper.isReplaceable()) {
            world.setBlockState(pos.up(),
               (BlockState)((BlockState)state.with(SithStoneCoffinBlock.TOP, true)).with(SithStoneCoffinBlock.OPEN, false),
               Block.NOTIFY_ALL);
         }

         if (world.getBlockEntity(pos) instanceof SithStoneCoffinBlockEntity coffin) {
            java.util.List<ItemStack> equipment = stack.get(ALComponents.COFFIN_EQUIPMENT);

            if (equipment != null) {
               for (ItemStack entry : equipment) {
                  if (!entry.isEmpty()) {
                     coffin.setEquipment(entry);
                     break;
                  }
               }
            }

            coffin.setTaskFinished(true);
         }
      }

      return super.postPlacement(pos, world, player, stack, state);
   }
}
