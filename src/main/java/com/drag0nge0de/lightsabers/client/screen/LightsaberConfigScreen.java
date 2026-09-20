package com.drag0nge0de.lightsabers.client.screen;

import com.drag0nge0de.lightsabers.config.ALConfig;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class LightsaberConfigScreen extends Screen {

   private static final int COLUMN_LEFT = -155;
   private static final int COLUMN_RIGHT = 5;
   private static final int COLUMN_WIDTH = 150;

   private final Screen parent;

   public LightsaberConfigScreen(Screen parent) {
      super(Text.translatable("config.lightsabers.title"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      int centerX = this.width / 2;
      ALConfig config = ALConfig.get();

      this.addDrawableChild(this.toggle(centerX + COLUMN_LEFT, 38, "config.lightsabers.first_person",
            config.firstPersonAnimations, value -> config.firstPersonAnimations = value));
      this.addDrawableChild(this.toggle(centerX + COLUMN_RIGHT, 38, "config.lightsabers.third_person",
            config.thirdPersonAnimations, value -> config.thirdPersonAnimations = value));

      this.addDrawableChild(this.slider(centerX + COLUMN_LEFT, 74, "config.lightsabers.damage",
            config.damageMultiplier, 0.0F, 5.0F, value -> config.damageMultiplier = value));
      this.addDrawableChild(this.slider(centerX + COLUMN_RIGHT, 74, "config.lightsabers.attack_speed",
            config.attackSpeedMultiplier, 0.1F, 3.0F, value -> config.attackSpeedMultiplier = value));

      this.addDrawableChild(this.slider(centerX + COLUMN_LEFT, 142, "config.lightsabers.jedi_temple",
            config.jediTempleSpawnRate, 0.0F, 3.0F, value -> config.jediTempleSpawnRate = value));
      this.addDrawableChild(this.slider(centerX + COLUMN_RIGHT, 142, "config.lightsabers.sith_tomb",
            config.sithTombSpawnRate, 0.0F, 3.0F, value -> config.sithTombSpawnRate = value));
      this.addDrawableChild(this.slider(centerX + COLUMN_LEFT, 166, "config.lightsabers.crystal_cave",
            config.crystalCaveRate, 0.0F, 3.0F, value -> config.crystalCaveRate = value));

      this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
            .dimensions(centerX - 100, this.height - 27, 200, 20)
            .build());
   }

   private ButtonWidget toggle(int x, int y, String key, boolean value, Consumer<Boolean> setter) {
      return ButtonWidget.builder(this.toggleLabel(key, value), widget -> {
         boolean next = !value;
         setter.accept(next);
         widget.setMessage(this.toggleLabel(key, next));
      }).dimensions(x, y, COLUMN_WIDTH, 20).build();
   }

   private Text toggleLabel(String key, boolean value) {
      return Text.translatable(key, Text.translatable(value ? "config.lightsabers.on" : "config.lightsabers.off"));
   }

   private FloatSlider slider(int x, int y, String key, float value, float min, float max, Consumer<Float> setter) {
      return new FloatSlider(x, y, COLUMN_WIDTH, 20, key, value, min, max, setter);
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      int centerX = this.width / 2;

      context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 12, 0xFFFFFF);
      context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("config.lightsabers.animations"),
            centerX, 27, 0xA0A0A0);
      context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("config.lightsabers.combat"),
            centerX, 63, 0xA0A0A0);
      context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("config.lightsabers.structures"),
            centerX, 101, 0xA0A0A0);
      context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("config.lightsabers.structures_note"),
            centerX, 111, 0x707070);
   }

   @Override
   public void close() {
      ALConfig.save();
      this.client.setScreen(this.parent);
   }

   @Override
   public void removed() {
      ALConfig.save();
      super.removed();
   }

   public static class FloatSlider extends SliderWidget {

      private final String labelKey;
      private final float min;
      private final float max;
      private final Consumer<Float> setter;

      public FloatSlider(int x, int y, int width, int height, String labelKey, float value, float min, float max,
            Consumer<Float> setter) {
         super(x, y, width, height, Text.empty(), (MathHelper.clamp(value, min, max) - min) / (max - min));
         this.labelKey = labelKey;
         this.min = min;
         this.max = max;
         this.setter = setter;
         this.updateMessage();
      }

      private float currentValue() {
         float raw = this.min + (float) this.value * (this.max - this.min);
         return Math.round(raw * 10.0F) / 10.0F;
      }

      private Text label() {
         return Text.translatable(this.labelKey, String.format("x%.1f", this.currentValue()));
      }

      @Override
      protected void updateMessage() {
         this.setMessage(this.label());
      }

      @Override
      protected void applyValue() {
         this.setter.accept(this.currentValue());
      }
   }
}
