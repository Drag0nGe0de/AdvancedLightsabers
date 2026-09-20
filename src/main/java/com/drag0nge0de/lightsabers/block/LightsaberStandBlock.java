package com.drag0nge0de.lightsabers.block;

import com.drag0nge0de.lightsabers.block.blockentity.LightsaberStandBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class LightsaberStandBlock extends WallMountedBlock implements BlockEntityProvider {
   public static final MapCodec<LightsaberStandBlock> CODEC = createCodec(LightsaberStandBlock::new);

   private static final VoxelShape FLOOR_SHAPE = VoxelShapes.cuboid(0.175, 0.0, 0.325, 0.83125, 0.1067, 0.675);
   private static final VoxelShape CEILING_SHAPE = VoxelShapes.cuboid(0.175, 0.8933, 0.325, 0.83125, 1.0, 0.675);
   private static final VoxelShape NORTH_WALL_SHAPE = VoxelShapes.cuboid(0.16875, 0.325, 0.0, 0.83125, 0.675, 0.1067);
   private static final VoxelShape SOUTH_WALL_SHAPE = NORTH_WALL_SHAPE;
   private static final VoxelShape WEST_WALL_SHAPE = VoxelShapes.cuboid(0.325, 0.16875, 0.0, 0.675, 0.83125, 0.1067);
   private static final VoxelShape EAST_WALL_SHAPE = WEST_WALL_SHAPE;

   public LightsaberStandBlock(Settings settings) {
      super(settings);
      this.setDefaultState(
         (BlockState)((BlockState)this.getDefaultState().with(FACE, BlockFace.FLOOR)).with(FACING, Direction.NORTH)
      );
   }

   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACE, FACING});
   }

   public MapCodec<? extends WallMountedBlock> getCodec() {
      return CODEC;
   }

   protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return switch ((BlockFace)state.get(FACE)) {
         case FLOOR -> FLOOR_SHAPE;
         case CEILING -> CEILING_SHAPE;
         case WALL -> {
            switch ((Direction)state.get(FACING)) {
               case NORTH:
                  yield NORTH_WALL_SHAPE;
               case SOUTH:
                  yield SOUTH_WALL_SHAPE;
               case WEST:
                  yield WEST_WALL_SHAPE;
               default:
                  yield EAST_WALL_SHAPE;
            }
         }
         default -> throw new MatchException(null, null);
      };
   }

   public LightsaberStandBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new LightsaberStandBlockEntity(pos, state);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
      if (!(world.getBlockEntity(pos) instanceof LightsaberStandBlockEntity stand)) {
         return ActionResult.PASS;
      } else {
         if (!world.isClient) {
            if (!player.isCreativeLevelTwoOp() && !stand.isOwner(player)) {
               player.sendMessage(Text.translatable("message.lightsabers.stand_not_owner").formatted(Formatting.RED), true);
            } else {
               ItemStack display = stand.getDisplayStack();
               ItemStack held = player.getStackInHand(player.getActiveHand());
               if (!held.isEmpty() || !display.isEmpty()) {
                  stand.setDisplayStack(held);
                  player.setStackInHand(player.getActiveHand(), display);
                  world.playSound(null, pos, SoundEvents.BLOCK_METAL_PLACE, SoundCategory.BLOCKS, 0.8F, 1.2F);
                  stand.markDirty();
               }
            }
         }

         return ActionResult.SUCCESS;
      }
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      super.onPlaced(world, pos, state, placer, itemStack);
      if (!world.isClient && placer instanceof ServerPlayerEntity player && world.getBlockEntity(pos) instanceof LightsaberStandBlockEntity stand) {
         stand.setOwner(player.getUuid());
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof LightsaberStandBlockEntity stand) {
         ItemStack display = stand.getDisplayStack();
         if (!display.isEmpty()) {
            ItemScatterer.spawn(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), display);
         }

         world.removeBlockEntity(pos);
      }
   }
}
