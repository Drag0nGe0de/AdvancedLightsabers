package com.drag0nge0de.lightsabers.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;

public class SaberAssemblyRecipeSerializer implements RecipeSerializer<SaberAssemblyRecipe> {
   public static final MapCodec<SaberAssemblyRecipe> CODEC = MapCodec.unit(SaberAssemblyRecipe::new);
   public static final PacketCodec<RegistryByteBuf, SaberAssemblyRecipe> PACKET_CODEC = PacketCodec.ofStatic((buf, recipe) -> {
   }, buf -> new SaberAssemblyRecipe());

   public MapCodec<SaberAssemblyRecipe> codec() {
      return CODEC;
   }

   public PacketCodec<RegistryByteBuf, SaberAssemblyRecipe> packetCodec() {
      return PACKET_CODEC;
   }
}
