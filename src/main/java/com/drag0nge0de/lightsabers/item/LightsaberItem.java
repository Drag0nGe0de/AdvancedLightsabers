package com.drag0nge0de.lightsabers.item;

import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.registry.ALSounds;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class LightsaberItem extends Item {

   public static final float MIN_LENGTH_CM = 19.0F;

   public LightsaberItem(Settings settings) {
      super(settings);
   }

   public static void register() {
      net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
         ALNetwork.ToggleSaberPayload.ID,
         (payload, context) -> context.player().getServer().execute(() -> {
            PlayerEntity user = context.player();
            Hand hand = payload.mainHand() ? Hand.MAIN_HAND : Hand.OFF_HAND;
            ItemStack stack = user.getStackInHand(hand);
            if (!stack.isEmpty() && stack.getItem() instanceof LightsaberItem) {
               setActive(stack, !getComponent(stack).active());
               user.getWorld().playSound(null, user.getBlockPos(),
                     getComponent(stack).active() ? ALSounds.LIGHTSABER_ON : ALSounds.LIGHTSABER_OFF,
                     net.minecraft.sound.SoundCategory.PLAYERS, 0.7F, 1.0F);

               if (getComponent(stack).active() && user instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                  com.drag0nge0de.lightsabers.advancement.ALAdvancements.trigger(serverPlayer,
                        com.drag0nge0de.lightsabers.advancement.ALAdvancements.IGNITE_SABER);
               }
            }
         })
      );
   }

   public static LightsaberComponent getComponent(ItemStack stack) {
      LightsaberComponent component = (LightsaberComponent)stack.get(ALComponents.LIGHTSABER);
      return component != null ? component : LightsaberComponent.DEFAULT;
   }

   public static void setActive(ItemStack stack, boolean active) {
      stack.set(ALComponents.LIGHTSABER, getComponent(stack).withActive(active));
      stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(active ? 1 : 0));
   }

   public static ItemStack createSaber(Hilt hilt, int color, boolean doubleSaber) {
      ItemStack stack = new ItemStack(doubleSaber ? ALItems.DOUBLE_LIGHTSABER : ALItems.LIGHTSABER);

      stack.set(ALComponents.LIGHTSABER, new LightsaberComponent(false, hilt.getId(), color,
            doubleSaber, com.drag0nge0de.lightsabers.hilt.HiltStatsTable.defaultFocusing(hilt.getId())));
      return stack;
   }

   @Override
   public int getEnchantability() {
      return 30;
   }

   public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      LightsaberComponent component = getComponent(stack);
      if (component.active()) {
         attacker.getWorld()
            .playSound(null, target.getBlockPos(), ALSounds.LIGHTSABER_HIT, SoundCategory.PLAYERS, 0.7F, 1.0F);
      }

      return true;
   }

   public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
      LightsaberComponent component = getComponent(stack);
      this.addBladeTooltip(tooltip, component);

      if (component.second().isPresent()) {
         this.addBladeTooltip(tooltip, component.second().get());
      }

      tooltip.add(
         Text.translatable(component.active() ? "tooltip.lightsabers.active" : "tooltip.lightsabers.inactive")
            .formatted(new Formatting[]{Formatting.DARK_GRAY, Formatting.ITALIC})
      );
   }

   private void addBladeTooltip(List<Text> tooltip, LightsaberComponent component) {
      CrystalColor color = CrystalColor.nearest(component.color());
      tooltip.add(Text.translatable("lightsaber.color").formatted(Formatting.GRAY));
      tooltip.add(indent(Text.translatable("lightsabers.color." + color.name().toLowerCase())));
      tooltip.add(Text.translatable("lightsaber.hilt").formatted(Formatting.GRAY));

      String[] hilts = component.hilts();

      if (component.isHiltUniform()) {
         tooltip.add(indent(Text.translatable("hilt.lightsabers." + component.hilt())));
      } else {
         for (String hilt : hilts) {
            tooltip.add(indent(Text.translatable("hilt.lightsabers." + hilt)));
         }
      }

      appendFocusingCrystals(tooltip, component.focusing());
   }

   private void addBladeTooltip(List<Text> tooltip, LightsaberComponent.Blade blade) {
      CrystalColor color = CrystalColor.nearest(blade.color());
      tooltip.add(Text.translatable("lightsaber.color").formatted(Formatting.GRAY));
      tooltip.add(indent(Text.translatable("lightsabers.color." + color.name().toLowerCase())));
      tooltip.add(Text.translatable("lightsaber.hilt").formatted(Formatting.GRAY));

      if (blade.emitterHilt().equals(blade.switchHilt()) && blade.switchHilt().equals(blade.gripHilt())
            && blade.gripHilt().equals(blade.pommelHilt())) {
         tooltip.add(indent(Text.translatable("hilt.lightsabers." + blade.gripHilt())));
      } else {
         for (String hilt : blade.hilts()) {
            tooltip.add(indent(Text.translatable("hilt.lightsabers." + hilt)));
         }
      }

      appendFocusingCrystals(tooltip, blade.focusing());
   }

   private void appendFocusingCrystals(List<Text> tooltip, int focusing) {
      if (focusing == 0) {
         return;
      }

      tooltip.add(Text.translatable("lightsaber.focusingCrystals").formatted(Formatting.GRAY));

      for (com.drag0nge0de.lightsabers.component.FocusingCrystalType type
            : com.drag0nge0de.lightsabers.component.FocusingCrystalType.values()) {
         if ((focusing & (1 << type.ordinal())) != 0) {
            tooltip.add(indent(Text.translatable("lightsaber.lightsabers.focusingCrystal." + type.asString())));
         }
      }
   }

   private static Text indent(Text value) {
      return Text.literal("  ").append(value).formatted(Formatting.GRAY);
   }
}
