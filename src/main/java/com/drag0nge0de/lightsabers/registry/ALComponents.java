package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.FocusingComponent;
import com.drag0nge0de.lightsabers.component.HiltComponent;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ALComponents {
   public static final ComponentType<LightsaberComponent> LIGHTSABER = register(
      "lightsaber", ComponentType.<LightsaberComponent>builder().codec(LightsaberComponent.CODEC).packetCodec(LightsaberComponent.PACKET_CODEC).build()
   );
   public static final ComponentType<CrystalComponent> CRYSTAL = register(
      "crystal", ComponentType.<CrystalComponent>builder().codec(CrystalComponent.CODEC).packetCodec(CrystalComponent.PACKET_CODEC).build()
   );
   public static final ComponentType<HiltComponent> HILT = register(
      "hilt", ComponentType.<HiltComponent>builder().codec(HiltComponent.CODEC).packetCodec(HiltComponent.PACKET_CODEC).build()
   );
   public static final ComponentType<FocusingComponent> FOCUSING = register(
      "focusing", ComponentType.<FocusingComponent>builder().codec(FocusingComponent.CODEC).packetCodec(FocusingComponent.PACKET_CODEC).build()
   );

   public static final ComponentType<java.util.List<ItemStack>> COFFIN_EQUIPMENT = register(
      "coffin_equipment", ComponentType.<java.util.List<ItemStack>>builder()
         .codec(ItemStack.OPTIONAL_CODEC.listOf())
         .packetCodec(ItemStack.OPTIONAL_PACKET_CODEC.collect(net.minecraft.network.codec.PacketCodecs.toList()))
         .build()
   );

   public static final ComponentType<java.util.List<ItemStack>> POUCH_CONTENTS = register(
      "pouch_contents", ComponentType.<java.util.List<ItemStack>>builder()
         .codec(ItemStack.OPTIONAL_CODEC.listOf())
         .packetCodec(ItemStack.OPTIONAL_PACKET_CODEC.collect(net.minecraft.network.codec.PacketCodecs.toList()))
         .build()
   );

   private static <T> ComponentType<T> register(String name, ComponentType<T> type) {
      return (ComponentType<T>)Registry.register(Registries.DATA_COMPONENT_TYPE, AL.id(name), type);
   }

   public static void register() {
   }
}
