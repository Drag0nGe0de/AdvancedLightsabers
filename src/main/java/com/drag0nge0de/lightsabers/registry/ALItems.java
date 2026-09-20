package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.item.DoubleLightsaberItem;
import com.drag0nge0de.lightsabers.item.CrystalPouchItem;
import com.drag0nge0de.lightsabers.item.FocusingCrystalItem;
import com.drag0nge0de.lightsabers.item.HiltItem;
import com.drag0nge0de.lightsabers.item.KyberCrystalItem;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.item.PartItem;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ALItems {
   public static final Item CIRCUITRY = register("lightsaber_circuitry", Item::new, new Settings());

   public static final Item SITH_GHOST_SPAWN_EGG = register("sith_ghost_spawn_egg",
      settings -> new SpawnEggItem(ALEntities.SITH_GHOST, 0x212121, 0xE2CECE, settings), new Settings());
   public static final Item CRYSTAL_POUCH = register("crystal_pouch", CrystalPouchItem::new, new Settings().maxCount(1));
   public static final Item FOCUSING_CRYSTAL = register("focusing_crystal", FocusingCrystalItem::new, new Settings().rarity(net.minecraft.util.Rarity.EPIC));
   public static final Item KYBER_CRYSTAL = register("kyber_crystal", KyberCrystalItem::new,

      new Settings().component(ALComponents.CRYSTAL, CrystalComponent.DEFAULT));
   public static final PartItem EMITTER = register("lightsaber_blade_emitter", settings -> new PartItem("emitter", settings), new Settings().maxCount(16));
   public static final PartItem SWITCH_MODULE = register("lightsaber_switch_module", settings -> new PartItem("switch_section", settings), new Settings().maxCount(16));
   public static final PartItem GRIP = register("lightsaber_grip", settings -> new PartItem("body", settings), new Settings().maxCount(16));
   public static final PartItem POMMEL = register("lightsaber_pommel", settings -> new PartItem("pommel", settings), new Settings().maxCount(16));
   public static final Item LIGHTSABER = register(
      "lightsaber",
      LightsaberItem::new,
      new Settings().maxCount(1).component(ALComponents.LIGHTSABER, LightsaberComponent.DEFAULT).attributeModifiers(saberAttributes())
   );
   public static final Item DOUBLE_LIGHTSABER = register(
      "double_lightsaber",
      DoubleLightsaberItem::new,
      new Settings()
         .maxCount(1)
         .component(ALComponents.LIGHTSABER, new LightsaberComponent(false, Hilt.DEFAULT.getId(), 16777215, true))
         .attributeModifiers(saberAttributes())
   );
   public static final Map<Hilt, Item> HILTS = new LinkedHashMap<>();

   public static AttributeModifiersComponent saberAttributes() {
      return new AttributeModifiersComponent(
         List.of(
            new Entry(
               EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(AL.id("saber_damage"), 7.0, Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND
            ),
            new Entry(
               EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(AL.id("saber_speed"), -2.0, Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND
            )
         ),
         false
      );
   }

   private static <T extends Item> T register(String name, Function<Settings, T> factory, Settings settings) {
      return (T)Registry.register(Registries.ITEM, AL.id(name), factory.apply(settings));
   }

   public static void register() {
   }

   static {
      for (Hilt hilt : Hilt.values()) {
         HILTS.put(hilt, register("hilt_" + hilt.getId(), settings -> new HiltItem(hilt, settings), new Settings().maxCount(16)));
      }
   }
}
