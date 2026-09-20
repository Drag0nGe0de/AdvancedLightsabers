package com.drag0nge0de.lightsabers.item;

import com.drag0nge0de.lightsabers.component.FocusingComponent;
import com.drag0nge0de.lightsabers.component.FocusingCrystalType;
import com.drag0nge0de.lightsabers.registry.ALComponents;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class FocusingCrystalItem extends Item {

    public FocusingCrystalItem(Settings settings) {
        super(settings);
    }

    public static FocusingCrystalType getType(ItemStack stack) {
        FocusingComponent c = stack.get(ALComponents.FOCUSING);
        return c != null ? c.type() : FocusingCrystalType.COMPRESSED;
    }

    public static ItemStack create(FocusingCrystalType type) {
        ItemStack stack = new ItemStack(com.drag0nge0de.lightsabers.registry.ALItems.FOCUSING_CRYSTAL);
        applyType(stack, type);
        return stack;
    }

    public static void applyType(ItemStack stack, FocusingCrystalType type) {
        stack.set(ALComponents.FOCUSING, new FocusingComponent(type));
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_MODEL_DATA,
                new net.minecraft.component.type.CustomModelDataComponent(type.ordinal()));

        stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME,
                net.minecraft.text.Text.translatable("item.lightsabers.focusing_crystal." + type.asString()));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("lightsaber.lightsabers.focusingCrystal."
                + getType(stack).asString()).formatted(Formatting.GRAY));
    }
}
