package com.drag0nge0de.lightsabers.loot;

import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.block.blockentity.CrystalOreBlockEntity;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;

public record CrystalColorLootFunction() implements LootFunction {
   public static final CrystalColorLootFunction INSTANCE = new CrystalColorLootFunction();
   public static final MapCodec<CrystalColorLootFunction> CODEC = MapCodec.unit(INSTANCE);
   public static final LootFunctionType<CrystalColorLootFunction> TYPE = new LootFunctionType<>(CODEC);

   @Override
   public ItemStack apply(ItemStack stack, LootContext context) {
      int color = -1;
      if (context.get(LootContextParameters.BLOCK_ENTITY) instanceof CrystalOreBlockEntity ore) {
         color = ore.getColor();
      }

      if (color == -1) {
         color = CrystalColor.rollWeighted(context.getRandom()).rgb;
      }

      stack.set(ALComponents.CRYSTAL, new CrystalComponent(color));
      return stack;
   }

   @Override
   public LootFunctionType<? extends LootFunction> getType() {
      return TYPE;
   }
}
