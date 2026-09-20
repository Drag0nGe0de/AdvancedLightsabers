package com.drag0nge0de.lightsabers.block;

import com.drag0nge0de.lightsabers.block.blockentity.HolocronBlockEntity;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HolocronBlock extends BlockWithEntity {
   public static final MapCodec<HolocronBlock> CODEC = createCodec(settings -> new HolocronBlock(false, settings));

   private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.25, 0.0, 0.25, 0.75, 0.5, 0.75);
   public final boolean sith;

   public HolocronBlock(boolean sith, Settings settings) {
      super(settings);
      this.sith = sith;
   }

   public MapCodec<? extends BlockWithEntity> getCodec() {
      return CODEC;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public BlockRenderType getRenderType(BlockState state) {

      return BlockRenderType.MODEL;
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      HolocronBlockEntity entity = new HolocronBlockEntity(pos, state);
      entity.sith = this.sith;
      return entity;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
      if (world.getBlockEntity(pos) instanceof HolocronBlockEntity holocron) {
         if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            holocron.addUser(serverPlayer.getUuid());
            ServerPlayNetworking.send(serverPlayer, new ALNetwork.OpenForcePowersPayload(pos));

            ALNetwork.sendToTracking((net.minecraft.server.world.ServerWorld) world, pos,
                  new ALNetwork.HolocronStatePayload(pos, true));
         }

         return ActionResult.SUCCESS;
      } else {
         return ActionResult.PASS;
      }
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
      return world.isClient
         ? validateTicker(type, ALBlockEntities.HOLOCRON, (w, p, s, be) -> be.clientTick())
         : validateTicker(type, ALBlockEntities.HOLOCRON, (w, p, s, be) -> be.serverTick(w, p, s));
   }
}
