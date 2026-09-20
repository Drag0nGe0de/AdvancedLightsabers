package com.drag0nge0de.lightsabers.item;

import com.drag0nge0de.lightsabers.hilt.Hilt;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HiltItem extends Item {
   private final Hilt hilt;

   public HiltItem(Hilt hilt, Settings settings) {
      super(settings);
      this.hilt = hilt;
   }

   public Hilt getHilt() {
      return this.hilt;
   }

   public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
      tooltip.add(Text.translatable("tooltip.lightsabers.hilt_bonus", new Object[]{this.hilt.getDamageBonus()}).formatted(Formatting.GRAY));
   }
}
