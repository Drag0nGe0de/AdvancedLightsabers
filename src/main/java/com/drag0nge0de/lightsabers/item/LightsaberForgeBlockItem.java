package com.drag0nge0de.lightsabers.item;

import com.drag0nge0de.lightsabers.block.LightsaberForgeBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LightsaberForgeBlockItem extends BlockItem {

    public LightsaberForgeBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean canPlace(ItemPlacementContext ctx, BlockState state) {
        BlockPos panelPos = ctx.getBlockPos().offset(LightsaberForgeBlock.panelDir(state));
        return super.canPlace(ctx, state) && ctx.getWorld().getBlockState(panelPos).isReplaceable();
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, net.minecraft.entity.player.PlayerEntity player,
            ItemStack stack, BlockState state) {
        if (!world.isClient) {
            BlockPos panelPos = pos.offset(LightsaberForgeBlock.panelDir(state));

            if (world.getBlockState(panelPos).isReplaceable()) {
                world.setBlockState(panelPos, (BlockState) state.with(LightsaberForgeBlock.PANEL, true),
                        Block.NOTIFY_ALL);
            }
        }

        return super.postPlacement(pos, world, player, stack, state);
    }
}
