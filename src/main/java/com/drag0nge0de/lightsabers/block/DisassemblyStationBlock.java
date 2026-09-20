package com.drag0nge0de.lightsabers.block;

import com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
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

public class DisassemblyStationBlock extends BlockWithEntity {

    public static final MapCodec<DisassemblyStationBlock> CODEC = createCodec(DisassemblyStationBlock::new);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public static final BooleanProperty LIT = BooleanProperty.of("lit");

    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0, 0, 0, 1, 0.8125, 1);

    public DisassemblyStationBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    @Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {

        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
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
        return SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DisassemblyStationBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
            BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof DisassemblyStationBlockEntity be) {
            player.openHandledScreen(be);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        if (world.isClient) {
            return null;
        }
        return validateTicker(type, com.drag0nge0de.lightsabers.registry.ALBlockEntities.DISASSEMBLY_STATION,
                (world2, pos, state2, be) -> DisassemblyStationBlockEntity
                        .tick(world2, pos, state2, be));
    }
}
