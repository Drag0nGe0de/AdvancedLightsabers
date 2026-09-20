package com.drag0nge0de.lightsabers.mixin;

import net.minecraft.world.gen.chunk.placement.StructurePlacement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructurePlacement.class)
public interface StructurePlacementAccessor {

   @Accessor("salt")
   int al$getSalt();
}
