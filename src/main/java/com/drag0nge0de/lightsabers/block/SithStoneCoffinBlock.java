package com.drag0nge0de.lightsabers.block;

import com.mojang.serialization.MapCodec;
import com.drag0nge0de.lightsabers.block.blockentity.SithStoneCoffinBlockEntity;
import com.drag0nge0de.lightsabers.registry.ALBlockEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SithStoneCoffinBlock extends HorizontalFacingBlock implements BlockEntityProvider {
   public static final MapCodec<SithStoneCoffinBlock> CODEC = createCodec(SithStoneCoffinBlock::new);
   public static final BooleanProperty OPEN = BooleanProperty.of("open");
   public static final BooleanProperty TOP = BooleanProperty.of("top");
   private static final VoxelShape SHAPE_CLOSED_NS = VoxelShapes.cuboid(0.0625, 0.0, 0.1875, 0.9375, 1.875, 0.8125);
   private static final VoxelShape SHAPE_CLOSED_EW = VoxelShapes.cuboid(0.1875, 0.0, 0.0625, 0.8125, 1.875, 0.9375);
   private static final VoxelShape SHAPE_OPEN = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.1875, 1.0);
   private static final VoxelShape SHAPE_TOP_CLOSED = VoxelShapes.cuboid(0.0, -1.0, 0.0, 1.0, 1.0, 1.0);
   private static final VoxelShape SHAPE_EMPTY = VoxelShapes.empty();

   public SithStoneCoffinBlock(Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)this.getDefaultState().with(FACING, Direction.NORTH).with(OPEN, false).with(TOP, false));
   }

   public MapCodec<? extends HorizontalFacingBlock> getCodec() {
      return CODEC;
   }

   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, OPEN, TOP});
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(TOP, false);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return state.get(TOP) ? null : new SithStoneCoffinBlockEntity(pos, state);
   }

   protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return getShapeFor(state, world, pos);
   }

   protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if (state.get(TOP)) {
         return SHAPE_EMPTY;
      }

      return getShapeFor(state, world, pos);
   }

   private VoxelShape getShapeFor(BlockState state, BlockView world, BlockPos pos) {
      if (state.get(TOP)) {
         BlockState below = world.getBlockState(pos.down());

         if (below.isOf(this) && below.contains(OPEN) && below.get(OPEN)) {
            return SHAPE_EMPTY;
         }

         return SHAPE_TOP_CLOSED;
      }

      if (state.get(OPEN)) {
         return SHAPE_OPEN;
      }

      return state.get(FACING).getAxis() == Direction.Axis.X ? SHAPE_CLOSED_EW : SHAPE_CLOSED_NS;
   }

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
      return world.isClient || state.get(TOP)
         ? null
         : checkType(type, ALBlockEntities.SITH_STONE_COFFIN, SithStoneCoffinBlockEntity::serverTick);
   }

   @SuppressWarnings("unchecked")
   private static <A extends BlockEntity, E extends BlockEntity, T extends BlockEntity> BlockEntityTicker<A> checkType(
         BlockEntityType<T> given, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker) {
      return expected == given ? (BlockEntityTicker<A>)ticker : null;
   }

   protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

      if (world.isClient) {
         return;
      }

      if (state.get(TOP)) {
         if (!world.getBlockState(pos.down()).isOf(this)) {
            world.removeBlock(pos, false);
         }
      } else if (!state.get(OPEN) && !world.getBlockState(pos.up()).isOf(this)) {
         world.removeBlock(pos, false);
      }
   }

   @Override
   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }
}
