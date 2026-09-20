package com.drag0nge0de.lightsabers.item;

import com.drag0nge0de.lightsabers.component.HiltComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.registry.ALComponents;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PartItem extends Item {

    private final String part;

    public PartItem(String part, Settings settings) {
        super(settings);
        this.part = part;
    }

    public String getPart() {
        return part;
    }

    public static Hilt getHilt(ItemStack stack) {
        HiltComponent c = stack.get(ALComponents.HILT);
        return c != null ? Hilt.byName(c.hilt()) : Hilt.DEFAULT;
    }

    public static ItemStack create(String part, Hilt hilt) {
        Item item;
        if (part.equals("emitter")) {
            item = com.drag0nge0de.lightsabers.registry.ALItems.EMITTER;
        } else if (part.equals("switch_section")) {
            item = com.drag0nge0de.lightsabers.registry.ALItems.SWITCH_MODULE;
        } else if (part.equals("body")) {
            item = com.drag0nge0de.lightsabers.registry.ALItems.GRIP;
        } else {
            item = com.drag0nge0de.lightsabers.registry.ALItems.POMMEL;
        }
        ItemStack stack = new ItemStack(item);
        stack.set(ALComponents.HILT, new HiltComponent(hilt.getId()));
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("hilt.lightsabers." + getHilt(stack).getId())
                .formatted(Formatting.GRAY));
    }
}
