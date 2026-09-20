package com.drag0nge0de.lightsabers.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import com.drag0nge0de.lightsabers.AL;

public class ALItemTags {

    public static final TagKey<Item> CRYSTALS = TagKey.of(Registries.ITEM.getKey(), AL.id("crystals"));
}
