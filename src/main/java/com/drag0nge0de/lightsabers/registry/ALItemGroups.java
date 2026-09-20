package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class ALItemGroups {
   public static void register() {
      Registry.register(
         Registries.ITEM_GROUP,
         AL.id("main"),
         FabricItemGroup.builder()
            .icon(() -> new ItemStack(ALItems.LIGHTSABER))
            .displayName(Text.translatable("itemgroup.lightsabers"))
            .entries((context, entries) -> {
               for (Hilt hilt : Hilt.values()) {
                  if (!com.drag0nge0de.lightsabers.hilt.HiltStatsTable.stats(hilt.getId()).doubleSaber()) {
                     entries.add(LightsaberItem.createSaber(hilt,
                           com.drag0nge0de.lightsabers.hilt.HiltStatsTable.stats(hilt.getId()).defaultColor(), false));
                  }
               }

               for (Hilt hilt : Hilt.values()) {
                  if (com.drag0nge0de.lightsabers.hilt.HiltStatsTable.stats(hilt.getId()).doubleSaber()) {
                     entries.add(LightsaberItem.createSaber(hilt,
                           com.drag0nge0de.lightsabers.hilt.HiltStatsTable.stats(hilt.getId()).defaultColor(), true));
                  }
               }

               String[] partTypes = {"emitter", "switch_section", "body", "pommel"};
               for (String part : partTypes) {
                  for (Hilt hilt : Hilt.values()) {
                     entries.add(com.drag0nge0de.lightsabers.item.PartItem.create(part, hilt));
                  }
               }

               entries.add(new ItemStack(ALItems.CIRCUITRY));

               for (com.drag0nge0de.lightsabers.component.FocusingCrystalType type
                     : com.drag0nge0de.lightsabers.component.FocusingCrystalType.values()) {
                  entries.add(com.drag0nge0de.lightsabers.item.FocusingCrystalItem.create(type));
               }

               for (CrystalColor color : CrystalColor.values()) {
                  entries.add(com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(color));
               }

               for (CrystalColor color : CrystalColor.values()) {
                  ItemStack pouch = new ItemStack(ALItems.CRYSTAL_POUCH);
                  pouch.set(ALComponents.CRYSTAL, new CrystalComponent(color.rgb));
                  entries.add(pouch);
               }
               entries.add(new ItemStack(ALBlocks.LIGHTSABER_FORGE));
               entries.add(new ItemStack(ALBlocks.LIGHTSABER_FORGE_DARK));
               entries.add(new ItemStack(ALBlocks.DISASSEMBLY_STATION));
               entries.add(new ItemStack(ALBlocks.CRYSTAL_ORE));
               entries.add(new ItemStack(ALBlocks.LIGHTSABER_STAND));
               entries.add(new ItemStack(ALBlocks.HOLOCRON_JEDI));
               entries.add(new ItemStack(ALBlocks.HOLOCRON_SITH));
               entries.add(new ItemStack(ALBlocks.SITH_SARCOPHAGUS));
               entries.add(new ItemStack(ALBlocks.SITH_STONE_COFFIN));
               entries.add(new ItemStack(ALBlocks.FORCESTONE));
               entries.add(new ItemStack(ALBlocks.FORCESTONE_CRACKED));
               entries.add(new ItemStack(ALBlocks.FORCESTONE_MOSSY));
               entries.add(new ItemStack(ALBlocks.FORCESTONE_INSCRIBED));
               entries.add(new ItemStack(ALBlocks.FORCESTONE_PILLAR));
               entries.add(new ItemStack(ALBlocks.FORCESTONE_STAIRS));
               entries.add(new ItemStack(ALBlocks.FORCESTONE_SLAB));
               entries.add(new ItemStack(ALBlocks.LIGHT_FORCESTONE));
               entries.add(new ItemStack(ALBlocks.LIGHT_FORCESTONE_CRACKED));
               entries.add(new ItemStack(ALBlocks.LIGHT_FORCESTONE_MOSSY));
               entries.add(new ItemStack(ALBlocks.LIGHT_FORCESTONE_INSCRIBED));
               entries.add(new ItemStack(ALBlocks.LIGHT_FORCESTONE_PILLAR));
               entries.add(new ItemStack(ALBlocks.LIGHT_ACTIVATED_FORCESTONE_PILLAR));
               entries.add(new ItemStack(ALBlocks.LIGHT_FORCESTONE_STAIRS));
               entries.add(new ItemStack(ALBlocks.LIGHT_FORCESTONE_SLAB));
               entries.add(new ItemStack(ALBlocks.DARK_FORCESTONE));
               entries.add(new ItemStack(ALBlocks.DARK_FORCESTONE_CRACKED));
               entries.add(new ItemStack(ALBlocks.DARK_FORCESTONE_MOSSY));
               entries.add(new ItemStack(ALBlocks.DARK_FORCESTONE_INSCRIBED));
               entries.add(new ItemStack(ALBlocks.DARK_FORCESTONE_PILLAR));
               entries.add(new ItemStack(ALBlocks.DARK_ACTIVATED_FORCESTONE_PILLAR));
               entries.add(new ItemStack(ALBlocks.DARK_FORCESTONE_STAIRS));
               entries.add(new ItemStack(ALBlocks.DARK_FORCESTONE_SLAB));

               entries.add(new ItemStack(ALItems.SITH_GHOST_SPAWN_EGG));
            })
            .build()
      );
   }
}
