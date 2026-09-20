package com.drag0nge0de.lightsabers.block;

import com.drag0nge0de.lightsabers.block.blockentity.LightsaberForgeBlockEntity;
import com.drag0nge0de.lightsabers.screen.LightsaberForgeScreenHandler;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class LightsaberForgeBlock extends BlockWithEntity {

    public static final MapCodec<LightsaberForgeBlock> CODEC = createCodec(LightsaberForgeBlock::new);

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public static final BooleanProperty PANEL = BooleanProperty.of("panel");

    private static final VoxelShape BASE_SHAPE = VoxelShapes.cuboid(0, 0, 0, 1, 0.8125, 1);

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, VoxelShapes.union(BASE_SHAPE, VoxelShapes.cuboid(1, 0, 0, 2, 0.8125, 1)),
            Direction.EAST, VoxelShapes.union(BASE_SHAPE, VoxelShapes.cuboid(0, 0, 1, 1, 0.8125, 2)),
            Direction.SOUTH, VoxelShapes.union(BASE_SHAPE, VoxelShapes.cuboid(-1, 0, 0, 0, 0.8125, 1)),
            Direction.WEST, VoxelShapes.union(BASE_SHAPE, VoxelShapes.cuboid(0, 0, -1, 1, 0.8125, 0)));

    public LightsaberForgeBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(PANEL, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PANEL);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {

        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing()).with(PANEL, false);
    }

    public static Direction panelDir(BlockState state) {
        return state.get(FACING).rotateYClockwise();
    }

    public static boolean isPanel(BlockState state) {
        return state.contains(PANEL) && state.get(PANEL);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return isPanel(state) ? SHAPES.get(state.get(FACING).getOpposite()) : SHAPES.get(state.get(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getOutlineShape(state, world, pos, context);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return isPanel(state) ? null : new LightsaberForgeBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public com.mojang.serialization.MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
            BlockHitResult hit) {
        BlockPos usePos = isPanel(state) ? pos.offset(panelDir(state).getOpposite()) : pos;

        if (!world.isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inventory, p) -> new LightsaberForgeScreenHandler(syncId, inventory,
                            ScreenHandlerContext.create(world, usePos)),
                    Text.translatable("gui.lightsabers.lightsaber_forge")));
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && isPanel(state)) {
            BlockPos mainPos = pos.offset(panelDir(state).getOpposite());

            if (world.getBlockState(mainPos).isOf(this)) {
                world.removeBlock(mainPos, false);

                if (!player.isCreative()) {
                    Block.dropStack(world, pos, new ItemStack(this.asItem(), 1));
                }
            }
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock,
            BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

        if (world.isClient) {
            return;
        }

        if (isPanel(state)) {
            BlockPos mainPos = pos.offset(panelDir(state).getOpposite());

            if (world.isPosLoaded(mainPos.getX(), mainPos.getZ())
                    && !world.getBlockState(mainPos).isOf(this)) {
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !isPanel(state)) {
            BlockPos panelPos = pos.offset(panelDir(state));

            if (world.getBlockState(panelPos).isOf(this)) {
                world.removeBlock(panelPos, false);
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

}
