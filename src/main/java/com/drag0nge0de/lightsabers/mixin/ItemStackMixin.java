package com.drag0nge0de.lightsabers.mixin;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.config.ALConfig;
import com.drag0nge0de.lightsabers.item.LightsaberItem;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

   @ModifyArg(method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/AttributeModifiersComponent;applyModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"))
   private BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> al$scaleEquipmentModifiers(
         BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer) {
      return al$wrap(consumer);
   }

   @ModifyArg(method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/AttributeModifiersComponent;applyModifiers(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V"))
   private BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> al$scaleTooltipModifiers(
         BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer) {
      return al$wrap(consumer);
   }

   private BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> al$wrap(
         BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer) {
      ItemStack self = (ItemStack)(Object)this;
      if (!(self.getItem() instanceof LightsaberItem)) {
         return consumer;
      }

      ALConfig config = ALConfig.get();
      float damage = config.damageMultiplier;
      float speed = config.attackSpeedMultiplier;
      if (damage == 1.0F && speed == 1.0F) {
         return consumer;
      }

      return (attribute, modifier) -> {
         double value = modifier.value();
         if (modifier.idMatches(AL.id("saber_damage"))) {
            value *= damage;
         } else if (modifier.idMatches(AL.id("saber_speed"))) {
            value = 2.0F * speed - 4.0F;
         } else {
            consumer.accept(attribute, modifier);
            return;
         }

         consumer.accept(attribute, new EntityAttributeModifier(modifier.id(), value, modifier.operation()));
      };
   }
}
