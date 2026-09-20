package com.drag0nge0de.lightsabers.block;

import com.drag0nge0de.lightsabers.block.blockentity.SithSarcophagusBlockEntity;
import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SithSarcophagusBlock extends HorizontalFacingBlock implements BlockEntityProvider {
   public static final MapCodec<SithSarcophagusBlock> CODEC = createCodec(SithSarcophagusBlock::new);
   public static final BooleanProperty FRONT = BooleanProperty.of("front");
   private static final float HEIGHT = 0.9375F;
   private static final VoxelShape SHAPE_BODY_SOUTH = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, HEIGHT, 2.0);
   private static final VoxelShape SHAPE_BODY_WEST = VoxelShapes.cuboid(-1.0, 0.0, 0.0, 1.0, HEIGHT, 1.0);
   private static final VoxelShape SHAPE_BODY_NORTH = VoxelShapes.cuboid(0.0, 0.0, -1.0, 1.0, HEIGHT, 1.0);
   private static final VoxelShape SHAPE_BODY_EAST = VoxelShapes.cuboid(0.0, 0.0, 0.0, 2.0, HEIGHT, 1.0);
   private static final VoxelShape SHAPE_FRONT_SOUTH = VoxelShapes.cuboid(0.0, 0.0, -1.0, 1.0, HEIGHT, 1.0);
   private static final VoxelShape SHAPE_FRONT_WEST = VoxelShapes.cuboid(0.0, 0.0, 0.0, 2.0, HEIGHT, 1.0);
   private static final VoxelShape SHAPE_FRONT_NORTH = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, HEIGHT, 2.0);
   private static final VoxelShape SHAPE_FRONT_EAST = VoxelShapes.cuboid(-1.0, 0.0, 0.0, 1.0, HEIGHT, 1.0);

   public SithSarcophagusBlock(Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)this.getDefaultState().with(FACING, Direction.NORTH).with(FRONT, false));
   }

   public MapCodec<? extends HorizontalFacingBlock> getCodec() {
      return CODEC;
   }

   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, FRONT});
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(FRONT, false);
   }

   public static boolean isFront(BlockState state) {
      return state.contains(FRONT) && state.get(FRONT);
   }

   protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if (isFront(state)) {
         switch (state.get(FACING)) {
            case SOUTH: return SHAPE_FRONT_SOUTH;
            case WEST: return SHAPE_FRONT_WEST;
            case EAST: return SHAPE_FRONT_EAST;
            default: return SHAPE_FRONT_NORTH;
         }
      }

      switch (state.get(FACING)) {
         case SOUTH: return SHAPE_BODY_SOUTH;
         case WEST: return SHAPE_BODY_WEST;
         case EAST: return SHAPE_BODY_EAST;
         default: return SHAPE_BODY_NORTH;
      }
   }

   protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getOutlineShape(state, world, pos, context);
   }

   protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
      if (!world.isClient) {
         BlockPos bodyPos = isFront(state) ? pos.offset(state.get(FACING).getOpposite()) : pos;

         if (world.getBlockEntity(bodyPos) instanceof SithSarcophagusBlockEntity sarcophagus) {
            sarcophagus.interact(player, player.isSneaking());
         }
      }

      return ActionResult.SUCCESS;
   }

   @Override
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return isFront(state) ? null : new SithSarcophagusBlockEntity(pos, state);
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
      return world.isClient || isFront(state)
         ? null
         : checkType(type, ALBlockEntities.SITH_SARCOPHAGUS, SithSarcophagusBlockEntity::serverTick);
   }

   @SuppressWarnings("unchecked")
   private static <A extends BlockEntity, E extends BlockEntity, T extends BlockEntity> BlockEntityTicker<A> checkType(
         BlockEntityType<T> given, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker) {
      return expected == given ? (BlockEntityTicker<A>)ticker : null;
   }

   @Override
   public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient && player.isCreative() && isFront(state)) {
         BlockPos bodyPos = pos.offset(state.get(FACING).getOpposite());

         if (world.getBlockState(bodyPos).isOf(this)) {
            world.removeBlock(bodyPos, false);
         }
      }

      return super.onBreak(world, pos, state, player);
   }

   protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

      if (world.isClient) {
         return;
      }

      if (isFront(state)) {
         if (!world.getBlockState(pos.offset(state.get(FACING).getOpposite())).isOf(this)) {
            world.removeBlock(pos, false);
         }
      } else if (!world.getBlockState(pos.offset(state.get(FACING))).isOf(this)) {
         world.removeBlock(pos, false);
         Block.dropStack(world, pos, new ItemStack(this));
      }
   }

   @Override
   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   @Override
   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock()) && !isFront(state)) {
         BlockEntity be = world.getBlockEntity(pos);

         if (be instanceof SithSarcophagusBlockEntity sarcophagus) {
            net.minecraft.util.ItemScatterer.spawn(world, pos, sarcophagus);
            world.updateComparators(pos, this);
         }

         BlockPos frontPos = pos.offset(state.get(FACING));

         if (world.getBlockState(frontPos).isOf(this)) {
            world.removeBlock(frontPos, false);
         }
      }

      super.onStateReplaced(state, world, pos, newState, moved);
   }
}
