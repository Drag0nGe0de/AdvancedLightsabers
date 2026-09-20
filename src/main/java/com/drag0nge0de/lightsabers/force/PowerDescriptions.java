package com.drag0nge0de.lightsabers.force;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class PowerDescriptions {

   private PowerDescriptions() {
   }

   public static List<Text> lines(Power power) {
      List<Text> list = new ArrayList<>();
      int tier = power.getTier();
      boolean has = false;

      switch (power.getBaseName()) {
         case "lightning" -> {
            has = true;
            list.add(effectSlash(damage(tier, 4, 2), "forcepower.stat.target.target"));
         }
         case "wound" -> {
            has = true;
            list.add(effectSlash(damage(tier, 2, 2), "forcepower.stat.target.target"));
            list.add(effect(stun(tier), "forcepower.stat.target.target"));
         }
         case "drain" -> {
            has = true;
            String from = tier < 3 ? "forcepower.stat.target.target" : "forcepower.stat.target.enemies";
            list.add(Text.translatable("forcepower.stat.absorb",
               yellow(4 + (tier - 1) * 2 + " HP"), gray(Text.translatable(from).getString())));
         }
         case "fortify" -> {
            has = true;
            list.add(Text.translatable("forcepower.stat.divide",
               yellow(Text.translatable("forcepower.stat.unit.force_damage").getString()),
               gray(format(1.25F + 0.25F * (tier - 1)))));
         }
         case "heal" -> {
            has = true;
            float heal = tier == 1 ? 4.0F : (tier == 2 ? 7.0F : 13.0F);
            list.add(to("+" + trim(heal) + " HP", "forcepower.stat.target.caster"));
            if (tier >= 3) {
               list.add(to("+7 HP", "forcepower.stat.target.allies"));
            }
         }
         case "meditation" -> {
            has = true;
            list.add(effect(Text.translatable("forcepower.stat.multiply",
               yellow(Text.translatable("forcepower.stat.unit.attack_damage").getString()),
               gray(format(1.25F + 0.25F * (tier - 1)))).getString() + " 90", "forcepower.stat.target.caster"));
            list.add(to("+" + (4 + (tier - 1) * 2) + " " + Text.translatable("forcepower.stat.unit.absorption").getString(), "forcepower.stat.target.caster"));
         }
         case "push" -> {
            has = true;
            int knockback = (int) (3 + Math.pow(2.0, tier - 1));
            list.add(effect2("+" + knockback + " " + Text.translatable("forcepower.stat.unit.knockback").getString(),
               "forcepower.stat.target.target"));
            list.add(effect2(trim(pushDamage(tier)) + " " + Text.translatable("forcepower.stat.unit.damage").getString(),
               "forcepower.stat.target.target"));
         }
         case "rebound" -> {
            has = true;
            list.add(effect2(Text.translatable("forcepower.stat.unit.fall_resistance").getString(),
               "forcepower.stat.target.caster"));
         }
         case "resist" -> {
            has = true;
            int duration = tier == 1 ? 140 : (tier == 2 ? 180 : 280);
            list.add(effect(Text.translatable("forcepower.stat.divide",
               yellow(Text.translatable("forcepower.stat.unit.energy_damage").getString()),
               gray(format(0.25F * (float) Math.pow(2.0, tier - 1)))).getString() + " " + (duration / 20),
               "forcepower.stat.target.caster"));
         }
         case "speed" -> {
            has = true;
            list.add(effect(Text.translatable("forcepower.stat.multiply",
               yellow(Text.translatable("forcepower.stat.unit.speed").getString()),
               gray("2")).getString() + " 5", "forcepower.stat.target.caster"));
         }
         case "stealth" -> {
            has = true;
            list.add(effect2(Text.translatable("forcepower.stat.unit.invisibility").getString(),
               "forcepower.stat.target.caster"));
         }
         case "throw" -> {
            has = true;
            if (tier >= 2) {
               list.add(Text.translatable("forcepower.stat.multiply",
                  yellow(Text.translatable("forcepower.stat.unit.impact_radius").getString()),
                  gray("2")));
            }
         }
         case "stun" -> {
            has = true;
            float duration = tier == 1 ? 2.0F : (tier == 2 ? 3.5F : 4.0F);
            String to = tier >= 3 ? "forcepower.stat.target.enemies" : "forcepower.stat.target.target";
            list.add(effect(Text.translatable("lightsabers.effect.stun").getString() + " " + trim(duration), to));
         }
         case "sight" -> {
            has = true;
            int duration = tier == 1 ? 200 : (tier == 2 ? 300 : 500);
            String targets = tier == 1
               ? Text.translatable("forcepower.stat.target.mobs").getString()
               : tier == 2
                  ? Text.translatable("forcepower.stat.target.mobs").getString() + " "
                     + Text.translatable("forcepower.list").getString() + " "
                     + Text.translatable("forcepower.stat.target.players").getString()
                  : Text.translatable("forcepower.stat.target.mobs").getString() + " "
                     + Text.translatable("forcepower.list").getString() + " "
                     + Text.translatable("forcepower.stat.target.players").getString() + " "
                     + Text.translatable("forcepower.list").getString() + " "
                     + Text.translatable("forcepower.stat.target.invisible").getString();
            list.add(Text.translatable("forcepower.stat.highlight", yellow(targets), gray(String.valueOf(duration / 20))));
         }
         default -> {
         }
      }

      if (has) {
         list.replaceAll(text -> text.copy().formatted(Formatting.GRAY));
      }

      return list;
   }

   private static float pushDamage(int tier) {
      float f = 1.0F;

      for (int i = 1; i < tier; ++i) {
         f *= f + 0.5F;
      }

      return f;
   }

   private static String damage(int tier, int base, int step) {
      return base + (tier - 1) * step + " " + Text.translatable("forcepower.stat.unit.damage").getString();
   }

   private static String stun(int tier) {
      float duration = tier == 1 ? 1.5F : (tier == 2 ? 2.5F : 3.0F);
      return Text.translatable("lightsabers.effect.stun").getString() + " " + trim(duration);
   }

   private static MutableText effect(String value, String targetKey) {
      return Text.translatable("forcepower.stat.effect", yellow(value), gray(Text.translatable(targetKey).getString()));
   }

   private static MutableText effectSlash(String value, String targetKey) {
      return Text.translatable("forcepower.stat.effect", yellow(value + "/"), gray(Text.translatable(targetKey).getString()));
   }

   private static MutableText effect2(String value, String targetKey) {
      return Text.translatable("forcepower.stat.effect2", yellow(value), gray(Text.translatable(targetKey).getString()));
   }

   private static MutableText to(String value, String targetKey) {
      return Text.translatable("forcepower.stat.to", yellow(value), gray(Text.translatable(targetKey).getString()));
   }

   private static String trim(float value) {
      return value == (long) value ? String.valueOf((long) value) : String.valueOf(value);
   }

   private static String format(float value) {
      return trim(value);
   }

   private static String yellow(String value) {
      return value;
   }

   private static String gray(String value) {
      return value;
   }
}
