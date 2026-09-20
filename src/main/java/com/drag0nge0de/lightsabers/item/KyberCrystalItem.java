package com.drag0nge0de.lightsabers.item;

import java.util.List;
import java.util.Locale;

import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.registry.ALComponents;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

public class KyberCrystalItem extends Item {

   public KyberCrystalItem(Settings settings) {
      super(settings);
   }

   public static ItemStack create(CrystalColor color) {
      ItemStack stack = new ItemStack(com.drag0nge0de.lightsabers.registry.ALItems.KYBER_CRYSTAL);
      stack.set(ALComponents.CRYSTAL, new CrystalComponent(color.rgb));
      return stack;
   }

   public static Rarity rarityOf(CrystalColor color) {
      for (CrystalColor epic : CrystalColor.EPIC) {
         if (epic == color) {
            return Rarity.EPIC;
         }
      }

      for (CrystalColor rare : CrystalColor.RARE) {
         if (rare == color) {
            return Rarity.RARE;
         }
      }

      for (CrystalColor uncommon : CrystalColor.UNCOMMON) {
         if (uncommon == color) {
            return Rarity.UNCOMMON;
         }
      }

      return Rarity.COMMON;
   }

   private static CrystalColor colorOf(ItemStack stack) {
      CrystalComponent component = stack.get(ALComponents.CRYSTAL);
      return component != null ? CrystalColor.nearest(component.color()) : null;
   }

   @Override
   public void inventoryTick(ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
      if (stack.getOrDefault(DataComponentTypes.RARITY, Rarity.COMMON) != Rarity.COMMON) {
         stack.set(DataComponentTypes.RARITY, Rarity.COMMON);
      }
      super.inventoryTick(stack, world, entity, slot, selected);
   }

   @Override
   public Text getName(ItemStack stack) {
      CrystalColor color = colorOf(stack);
      if (color != null) {
         return Text.translatable(this.getTranslationKey(stack));
      }

      return super.getName(stack);
   }

   @Override
   public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
      CrystalColor color = colorOf(stack);
      if (color != null) {
         Rarity rarity = rarityOf(color);
         String label = Text.translatable("lightsabers.rarity." + rarity.name().toLowerCase(Locale.ROOT)).getString();
         MutableText line = Text.translatable("lightsabers.color." + color.name().toLowerCase(Locale.ROOT))
            .formatted(Formatting.GRAY);
         line.append(Text.literal(" (" + label + ")").formatted(Formatting.GRAY));
         tooltip.add(line);
      }
   }
}
