package com.drag0nge0de.lightsabers.block;

import com.drag0nge0de.lightsabers.block.blockentity.CrystalOreBlockEntity;
import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.item.KyberCrystalItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.List;

public class CrystalOreBlock extends Block implements BlockEntityProvider {

   public static final MapCodec<CrystalOreBlock> CODEC = createCodec(CrystalOreBlock::new);
   public static final IntProperty ATTACHMENT = IntProperty.of("attachment", 0, 6);
   private static final Logger LOGGER = LoggerFactory.getLogger("lightsabers/crystal");

   private static final VoxelShape[] SHAPES = buildShapes();

   public CrystalOreBlock(Settings settings) {
      super(settings);
      this.setDefaultState(this.stateManager.getDefaultState().with(ATTACHMENT, 0));
   }

   @Override
   protected MapCodec<? extends CrystalOreBlock> getCodec() {
      return CODEC;
   }

   @Override
   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(ATTACHMENT);
   }

   @Override
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new CrystalOreBlockEntity(pos, state);
   }

   @Override
   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockState current = world.getBlockState(pos);
      if (!current.isOf(this)) {
         return;
      }

      int attachment = current.get(ATTACHMENT);
      if (attachment != 0 && legacySupport(attachment, world, pos)) {
         return;
      }

      int fixed = autoAttach(world, pos);
      if (fixed == 0) {
         fixed = 5;
      }

      if (fixed != attachment) {
         world.setBlockState(pos, current.with(ATTACHMENT, fixed), Block.NOTIFY_ALL);
         LOGGER.debug("repaired relic crystal at {} -> attachment {}", pos.toShortString(), fixed);
      }
   }

   @Override
   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   @Override
   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPES[state.get(ATTACHMENT)];
   }

   @Override
   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.empty();
   }

   @Override
   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return legacySupport(state.get(ATTACHMENT), world, pos);
   }

   @Override
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      World world = ctx.getWorld();
      BlockPos pos = ctx.getBlockPos();

      int att = 0;
      switch (ctx.getSide()) {
         case DOWN -> { if (legacySupport(6, world, pos)) att = 6; }
         case UP -> { if (legacySupport(5, world, pos)) att = 5; }
         case NORTH -> { if (legacySupport(4, world, pos)) att = 4; }
         case SOUTH -> { if (legacySupport(3, world, pos)) att = 3; }
         case WEST -> { if (legacySupport(2, world, pos)) att = 2; }
         case EAST -> { if (legacySupport(1, world, pos)) att = 1; }
      }
      if (att == 0) {
         att = autoAttach(world, pos);
      }
      return this.getDefaultState().with(ATTACHMENT, att);
   }

   public static int autoAttach(WorldView world, BlockPos pos) {
      for (int att = 1; att <= 6; att++) {
         if (legacySupport(att, world, pos)) {
            return att;
         }
      }
      return 0;
   }

   private static boolean legacySupport(int attachment, WorldView world, BlockPos pos) {
      Direction supportSide = switch (attachment) {
         case 1 -> Direction.EAST;
         case 2 -> Direction.WEST;
         case 3 -> Direction.SOUTH;
         case 4 -> Direction.NORTH;
         default -> Direction.UP;
      };
      BlockPos neighbor = switch (attachment) {
         case 1 -> pos.offset(Direction.WEST);
         case 2 -> pos.offset(Direction.EAST);
         case 3 -> pos.offset(Direction.NORTH);
         case 4 -> pos.offset(Direction.SOUTH);
         case 5 -> pos.offset(Direction.DOWN);
         case 6 -> pos.offset(Direction.UP);
         default -> null;
      };
      if (neighbor == null) {
         return false;
      }
      BlockState state = world.getBlockState(neighbor);

      return state.isSideSolidFullSquare(world, neighbor,
            attachment == 6 ? Direction.DOWN : Direction.UP);
   }

   @Override
   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
         WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      int att = state.get(ATTACHMENT);
      if (att == 0) {
         if (world instanceof ServerWorld serverWorld) {
            serverWorld.scheduleBlockTick(pos.toImmutable(), this, 2);
         }
         return state;
      }
      if (!legacySupport(att, world, pos)) {
         if (world instanceof ServerWorld serverWorld) {
            BlockEntity be = world.getBlockEntity(pos);
            int color = be instanceof CrystalOreBlockEntity ore ? ore.getColor() : -1;
            if (color == -1) {
               color = CrystalColor.rollWeighted(serverWorld.getRandom()).rgb;
            }
            Block.dropStack(serverWorld, pos, KyberCrystalItem.create(CrystalColor.nearest(color)));
         }
         return Blocks.AIR.getDefaultState();
      }
      return state;
   }

   @Override
   public ActionResult onUse(BlockState state, World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, BlockHitResult hit) {
      if (!world.isClient) {
         BlockEntity be = world.getBlockEntity(pos);
         int color = be instanceof CrystalOreBlockEntity ore ? ore.getColor() : -1;
         if (color == -1) {
            color = CrystalColor.rollWeighted(world.getRandom()).rgb;
         }
         Block.dropStack(world, pos, KyberCrystalItem.create(CrystalColor.nearest(color)));
         world.removeBlock(pos, false);
      }
      return ActionResult.SUCCESS;
   }

   private static VoxelShape[] buildShapes() {
      float w = 0.375F;
      float half = (1.0F - w) / 2.0F;
      VoxelShape floor = VoxelShapes.cuboid(half, 0.0, half, 1 - half, w, 1 - half);
      VoxelShape[] shapes = new VoxelShape[7];
      shapes[5] = floor;
      shapes[6] = VoxelShapes.cuboid(half, 1 - w, half, 1 - half, 1.0, 1 - half);
      shapes[1] = VoxelShapes.cuboid(0.0, half, half, w, 1 - half, 1 - half);
      shapes[2] = VoxelShapes.cuboid(1 - w, half, half, 1.0, 1 - half, 1 - half);
      shapes[3] = VoxelShapes.cuboid(half, half, 0.0, 1 - half, 1 - half, w);
      shapes[4] = VoxelShapes.cuboid(half, half, 1 - w, 1 - half, 1 - half, 1.0);
      shapes[0] = shapes[6];
      for (VoxelShape shape : shapes) {
         if (VoxelShapes.matchesAnywhere(shape, VoxelShapes.fullCube(), BooleanBiFunction.ONLY_FIRST)) {
            throw new IllegalStateException("crystal shape exceeds block bounds");
         }
      }
      return shapes;
   }
}
