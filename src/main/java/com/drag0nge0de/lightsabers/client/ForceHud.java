package com.drag0nge0de.lightsabers.client;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.config.ALConfig;
import com.drag0nge0de.lightsabers.force.ForceEffects;
import com.drag0nge0de.lightsabers.force.Power;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ForceHud {
   private static final Identifier ICONS = AL.id("textures/gui/icons.png");
   private static final Identifier WIDGETS = AL.id("textures/gui/widgets.png");
   private static final Identifier SHIELD = AL.id("textures/misc/force_shield.png");
   private static final int BAR_WIDTH = 182;
   private static final int BAR_HEIGHT = 5;
   private static float diffEnergy;
   private static boolean hadBar;

   public static void register() {
      HudRenderCallback.EVENT.register((HudRenderCallback)(context, tickDelta) -> {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientPlayerEntity player = client.player;
         if (player != null && !client.options.hudHidden && !player.isSpectator()
               && ForceClientState.hasSensitivity()) {
            int width = context.getScaledWindowWidth();
            int height = context.getScaledWindowHeight();
            renderShaders(context, width, height);
            renderShield(context, width, height);
            renderForceBar(context, width, height, player);
            renderPowerSelector(context, width, height, player);
            renderStatusEffects(context, width, height, player);
         } else {
            hadBar = false;
         }
      });
   }

   private static void renderShaders(DrawContext context, int width, int height) {
      if (!ALConfig.get().enableShaders) {
         return;
      }

      if (ForceClientState.hasEffect(ForceEffects.GAZE)) {
         context.fill(0, 0, width, height, 822283775);
      } else if (ForceClientState.hasEffect(ForceEffects.STEALTH)) {
         context.fill(0, 0, width, height, 1632087576);
      }
   }

   private static void renderShield(DrawContext context, int width, int height) {
      if (!ALConfig.get().enableShaders || !ForceClientState.hasEffect(ForceEffects.FORTIFY)) {
         return;
      }

      int size = Math.min(width, height);
      int x = (width - size) / 2;
      int y = (height - size) / 2;
      context.drawTexture(SHIELD, x, y, 0.0F, 0.0F, size, size, size, size);
   }

   private static void renderForceBar(DrawContext context, int width, int height, ClientPlayerEntity player) {
      int left = width / 2 - 91;
      int top = height - 32 + 3;
      float cap = ForceClientState.maxEnergy;
      if (!(cap <= 0.0F)) {
         float energy = MathHelper.clamp(ForceClientState.energy, 0.0F, cap);
         if (!hadBar) {
            diffEnergy = energy;
         } else if (energy > diffEnergy) {
            diffEnergy = energy;
         } else {
            diffEnergy = energy + (diffEnergy - energy) * 0.88F;
         }

         hadBar = true;
         context.drawTexture(ICONS, left, top, 0.0F, 74.0F, 182, 5, 256, 256);
         int filled = (int)(energy / cap * 182.0F);
         int filledDiff = (int)(diffEnergy / cap * 182.0F);
         if (filledDiff > filled && filledDiff > 0) {
            context.drawTexture(ICONS, left, top, 0.0F, 79.0F, MathHelper.clamp(filledDiff, 0, 182), 5, 256, 256);
         }

         if (filled > 0) {
            context.drawTexture(ICONS, left, top, 0.0F, 84.0F, MathHelper.clamp(filled, 0, 182), 5, 256, 256);
         }

         String text = MathHelper.floor(energy) + "/" + MathHelper.floor(cap);
         MinecraftClient client = MinecraftClient.getInstance();

         var matrices = context.getMatrices();
         matrices.push();

         matrices.translate(left + 91, top - 0.875F, 0);
         matrices.scale(0.75F, 0.75F, 0.75F);
         int tx = -client.textRenderer.getWidth(text) / 2;
         int ty = 0;
         for (int[] off : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            context.drawText(client.textRenderer, text, tx + off[0], ty + off[1], 0, false);
         }
         context.drawText(client.textRenderer, text, tx, ty, 16777215, false);
         matrices.pop();
      } else {
         hadBar = false;
      }
   }

   private static void renderPowerSelector(DrawContext context, int width, int height, ClientPlayerEntity player) {
      if (ForceClientState.maxEnergy <= 0.0F) {
         return;
      }

      int left = width / 2 - 184;
      int top = height - 22;
      context.drawTexture(WIDGETS, left, top, 0.0F, 0.0F, 62, 22, 256, 256);

      for (int i = 0; i < 3; i++) {
         Power power = ForceClientState.getSlotPower(i);
         if (power != null) {
            context.drawTexture(ICONS, left + 3 + i * 20, top + 3, power.iconX * 16, power.iconY * 16, 16, 16, 256, 256);
         }
      }

      context.drawTexture(WIDGETS, left - 1 + ForceClientState.selectedSlot * 20, top - 1, 0.0F, 22.0F, 24, 24, 256, 256);
   }

   private static void renderStatusEffects(DrawContext context, int width, int height, ClientPlayerEntity player) {
      MinecraftClient client = MinecraftClient.getInstance();
      int left = width - 3;
      int top = height - 28;
      int index = 0;

      for (java.util.Map.Entry<String, int[]> entry : ForceClientState.allEffects().entrySet()) {
         String id = entry.getKey();
         int[] data = entry.getValue();
         int amplifier = data[0];
         int duration = data[1];
         if (duration < 0) {
            continue;
         }

         Power power = Power.forEffect(id, amplifier);
         if (power == null) {
            continue;
         }

         int y = top - 28 * index;
         context.drawTexture(ICONS, left - 26, y - 26, 0, 48, 26, 26, 256, 256);
         context.drawTexture(ICONS, left - 21, y - 21, power.iconX * 16, power.iconY * 16, 16, 16, 256, 256);

         String name = Text.translatable("forcepower.name." + power.getName()).getString();
         String time = ticksToElapsedTime(duration);
         if (amplifier > 0 && amplifier < 10) {
            name += " " + Text.translatable("enchantment.level." + (amplifier + 1)).getString();
         }

         context.drawText(client.textRenderer, name, left - 30 - client.textRenderer.getWidth(name), y - 22, 16777215, true);
         context.drawText(client.textRenderer, time, left - 30 - client.textRenderer.getWidth(time), y - 13, 16777215, true);
         index++;
      }
   }

   private static String ticksToElapsedTime(int ticks) {
      int i = ticks / 20;
      int j = i / 60;
      i %= 60;
      return i < 10 ? j + ":0" + i : j + ":" + i;
   }
}
