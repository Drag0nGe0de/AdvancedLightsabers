package com.drag0nge0de.lightsabers.block.blockentity;

import com.drag0nge0de.lightsabers.registry.ALBlockEntities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class LightsaberForgeBlockEntity extends BlockEntity {

    public LightsaberForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ALBlockEntities.LIGHTSABER_FORGE, pos, state);
    }
}
