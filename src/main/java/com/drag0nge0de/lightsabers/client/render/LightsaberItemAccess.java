package com.drag0nge0de.lightsabers.client.render;

import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.registry.ALComponents;

import net.minecraft.item.ItemStack;

public final class LightsaberItemAccess {

    private LightsaberItemAccess() {
    }

    public static LightsaberComponent component(ItemStack stack) {
        LightsaberComponent component = stack.get(ALComponents.LIGHTSABER);
        return component != null ? component : LightsaberComponent.DEFAULT;
    }

    public static String partHilt(ItemStack stack) {
        return com.drag0nge0de.lightsabers.item.PartItem.getHilt(stack).getId();
    }

    public static float[] crystalColor(ItemStack stack) {
        CrystalComponent c = stack.get(ALComponents.CRYSTAL);
        return HiltRenderer.rgb(c != null ? c.color() : 0x59B9FF);
    }
}
