package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.recipe.SaberAssemblyRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ALRecipes {
   public static final SaberAssemblyRecipeSerializer SABER_ASSEMBLY = (SaberAssemblyRecipeSerializer)Registry.register(
      Registries.RECIPE_SERIALIZER, AL.id("saber_assembly"), new SaberAssemblyRecipeSerializer()
   );

   public static void register() {
   }
}
