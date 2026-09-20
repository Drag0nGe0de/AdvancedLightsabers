package com.drag0nge0de.lightsabers.force;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class Power implements Comparable<Power> {
   public static final List<Power> POWERS = new ArrayList<>();
   public static final Map<String, Power> BY_NAME = new HashMap<>();
   private static final Map<String, int[]> ICON_CELLS = Map.ofEntries(
      Map.entry("forceSensitivity", new int[]{14, 0}),
      Map.entry("lightSide", new int[]{13, 1}),
      Map.entry("darkSide", new int[]{13, 2}),
      Map.entry("neutral", new int[]{14, 1}),
      Map.entry("level1", new int[]{11, 1}),
      Map.entry("level2", new int[]{12, 1}),
      Map.entry("level3", new int[]{10, 2}),
      Map.entry("level4", new int[]{11, 2}),
      Map.entry("level5", new int[]{12, 2}),
      Map.entry("heal1", new int[]{0, 0}),
      Map.entry("heal2", new int[]{0, 1}),
      Map.entry("heal3", new int[]{0, 2}),
      Map.entry("fortify1", new int[]{1, 0}),
      Map.entry("fortify2", new int[]{1, 1}),
      Map.entry("fortify3", new int[]{1, 2}),
      Map.entry("stun1", new int[]{2, 0}),
      Map.entry("stun2", new int[]{2, 1}),
      Map.entry("stun3", new int[]{2, 2}),
      Map.entry("drain1", new int[]{3, 0}),
      Map.entry("drain2", new int[]{3, 1}),
      Map.entry("drain3", new int[]{3, 2}),
      Map.entry("lightning1", new int[]{4, 0}),
      Map.entry("lightning2", new int[]{4, 1}),
      Map.entry("lightning3", new int[]{4, 2}),
      Map.entry("wound1", new int[]{5, 0}),
      Map.entry("wound2", new int[]{5, 1}),
      Map.entry("wound3", new int[]{5, 2}),
      Map.entry("stealth", new int[]{11, 0}),
      Map.entry("speed", new int[]{12, 0}),
      Map.entry("rebound", new int[]{13, 0}),
      Map.entry("sight1", new int[]{6, 0}),
      Map.entry("sight2", new int[]{6, 1}),
      Map.entry("sight3", new int[]{6, 2}),
      Map.entry("meditation1", new int[]{7, 0}),
      Map.entry("meditation2", new int[]{7, 1}),
      Map.entry("meditation3", new int[]{7, 2}),
      Map.entry("throw1", new int[]{10, 0}),
      Map.entry("throw2", new int[]{10, 1}),
      Map.entry("resist1", new int[]{8, 0}),
      Map.entry("resist2", new int[]{8, 1}),
      Map.entry("resist3", new int[]{8, 2}),
      Map.entry("push1", new int[]{9, 0}),
      Map.entry("push2", new int[]{9, 1}),
      Map.entry("push3", new int[]{9, 2})
   );
   public static final Power FORCE_SENSITIVITY = new Power("forceSensitivity", 0, 0, null, new PowerStats(160).setBaseBonus(2).setForceBonus(50).setRegen(5));
   public static final Power LIGHT_SIDE = new Power("lightSide", 2, 0, FORCE_SENSITIVITY);
   public static final Power DARK_SIDE = new Power("darkSide", -2, 0, FORCE_SENSITIVITY);
   public static final Power NEUTRAL = new Power("neutral", 0, 2, FORCE_SENSITIVITY);
   public static final Power FORCE_LEVEL1 = new Power("level1", 0, -2, FORCE_SENSITIVITY, new PowerStats(160).setBaseBonus(1).setForceBonus(50).setRegen(10));
   public static final Power FORCE_LEVEL2 = new Power("level2", 0, -1, FORCE_LEVEL1, new PowerStats(247).setBaseBonus(1).setForceBonus(50).setRegen(15));
   public static final Power FORCE_LEVEL3 = new Power("level3", 0, -1, FORCE_LEVEL2, new PowerStats(394).setBaseBonus(1).setForceBonus(50).setRegen(15));
   public static final Power FORCE_LEVEL4 = new Power("level4", 0, -1, FORCE_LEVEL3, new PowerStats(679).setBaseBonus(2).setForceBonus(75).setRegen(25));
   public static final Power FORCE_LEVEL5 = new Power("level5", 0, -1, FORCE_LEVEL4, new PowerStats(1186).setBaseBonus(2).setForceBonus(100).setRegen(30));
   public static final Power HEAL1 = new Power("heal1", 0, -4, LIGHT_SIDE, new PowerStats(320).setBaseReq(1).setUseCost(50.0F));
   public static final Power HEAL2 = new Power("heal2", 0, -1, HEAL1, new PowerStats(966).setUseCost(125.0F));
   public static final Power HEAL3 = new Power("heal3", 0, -1, HEAL2, new PowerStats(1440).setUseCost(175.0F));
   public static final Power FORTIFY1 = new Power("fortify1", 2, -4, LIGHT_SIDE, new PowerStats(320).setBaseReq(1).setUseCost(2.5F).setType(PowerType.PER_SECOND));
   public static final Power FORTIFY2 = new Power("fortify2", 0, -1, FORTIFY1, new PowerStats(788).setUseCost(2.5F).setType(PowerType.PER_SECOND));
   public static final Power FORTIFY3 = new Power("fortify3", 0, -1, FORTIFY2, new PowerStats(1100).setUseCost(2.5F).setType(PowerType.PER_SECOND));
   public static final Power STUN1 = new Power("stun1", 2, 0, LIGHT_SIDE, new PowerStats(320).setBaseReq(1).setUseCost(50.0F));
   public static final Power STUN2 = new Power("stun2", 0, -1, STUN1, new PowerStats(788).setUseCost(100.0F));
   public static final Power STUN3 = new Power("stun3", 0, -1, STUN2, new PowerStats(1100).setUseCost(150.0F));
   public static final Power DRAIN1 = new Power("drain1", 0, -4, DARK_SIDE, new PowerStats(460).setBaseReq(1).setUseCost(100.0F));
   public static final Power DRAIN2 = new Power("drain2", 0, -1, DRAIN1, new PowerStats(1180).setUseCost(150.0F));
   public static final Power DRAIN3 = new Power("drain3", 0, -1, DRAIN2, new PowerStats(1670).setUseCost(200.0F));
   public static final Power LIGHTNING1 = new Power("lightning1", -2, -4, DARK_SIDE, new PowerStats(320).setBaseReq(1).setUseCost(5.0F).setType(PowerType.PER_SECOND));
   public static final Power LIGHTNING2 = new Power("lightning2", 0, -1, LIGHTNING1, new PowerStats(788).setUseCost(10.0F).setType(PowerType.PER_SECOND));
   public static final Power LIGHTNING3 = new Power("lightning3", 0, -1, LIGHTNING2, new PowerStats(1277).setUseCost(20.0F).setType(PowerType.PER_SECOND));
   public static final Power WOUND1 = new Power("wound1", -2, 0, DARK_SIDE, new PowerStats(320).setBaseReq(1).setUseCost(50.0F));
   public static final Power WOUND2 = new Power("wound2", 0, -1, WOUND1, new PowerStats(788).setUseCost(115.0F));
   public static final Power WOUND3 = new Power("wound3", 0, -1, WOUND2, new PowerStats(1110).setUseCost(170.0F));
   public static final Power STEALTH = new Power("stealth", -3, 0, NEUTRAL, new PowerStats(160).setBaseReq(1).setUseCost(5.0F).setType(PowerType.PER_SECOND));
   public static final Power SPEED = new Power("speed", 3, 0, NEUTRAL, new PowerStats(160).setBaseReq(1).setUseCost(30.0F));
   public static final Power REBOUND = new Power("rebound", 3, 1, NEUTRAL, new PowerStats(320).setBaseReq(1).setUseCost(3.0F).setType(PowerType.PASSIVE));
   public static final Power SIGHT1 = new Power("sight1", -2, 2, NEUTRAL, new PowerStats(160).setBaseReq(1).setUseCost(25.0F));
   public static final Power SIGHT2 = new Power("sight2", 0, 1, SIGHT1, new PowerStats(394).setUseCost(25.0F));
   public static final Power SIGHT3 = new Power("sight3", 0, 1, SIGHT2, new PowerStats(550).setUseCost(25.0F));
   public static final Power MEDITATION1 = new Power("meditation1", -1, 2, NEUTRAL, new PowerStats(320).setBaseReq(1).setUseCost(75.0F));
   public static final Power MEDITATION2 = new Power("meditation2", 0, 1, MEDITATION1, new PowerStats(788).setUseCost(100.0F));
   public static final Power MEDITATION3 = new Power("meditation3", 0, 1, MEDITATION2, new PowerStats(1100).setUseCost(150.0F));
   public static final Power THROW1 = new Power("throw1", 0, 2, NEUTRAL, new PowerStats(160).setBaseReq(1).setUseCost(50.0F));
   public static final Power THROW2 = new Power("throw2", 0, 1, THROW1, new PowerStats(394).setUseCost(50.0F));
   public static final Power RESIST1 = new Power("resist1", 1, 2, NEUTRAL, new PowerStats(160).setBaseReq(1).setUseCost(50.0F));
   public static final Power RESIST2 = new Power("resist2", 0, 1, RESIST1, new PowerStats(394).setUseCost(60.0F));
   public static final Power RESIST3 = new Power("resist3", 0, 1, RESIST2, new PowerStats(550).setUseCost(70.0F));
   public static final Power PUSH1 = new Power("push1", 2, 2, NEUTRAL, new PowerStats(320).setBaseReq(1).setUseCost(50.0F));
   public static final Power PUSH2 = new Power("push2", 0, 1, PUSH1, new PowerStats(788).setUseCost(85.0F));
   public static final Power PUSH3 = new Power("push3", 0, 1, PUSH2, new PowerStats(1100).setUseCost(120.0F));
   private final String name;
   public final Power parent;
   public final int tier;
   public final ForceSide side;
   public final int x;
   public final int y;
   public final PowerStats stats;
   public final List<Power> children = new ArrayList<>();
   public final int iconX;
   public final int iconY;

   private Power(String name, int dx, int dy, Power parent, PowerStats stats) {
      this.name = name;
      this.parent = parent;
      this.stats = stats;
      if (parent != null) {
         parent.children.add(this);
         this.x = parent.x + dx;
         this.y = parent.y + dy;
         this.tier = parent.tier + 1;
      } else {
         this.x = dx;
         this.y = dy;
         this.tier = 0;
      }

      ForceSide s = ForceSide.NONE;

      for (Power p = this; p != null; p = p.parent) {
         if (p == LIGHT_SIDE) {
            s = ForceSide.LIGHT;
            break;
         }

         if (p == DARK_SIDE) {
            s = ForceSide.DARK;
            break;
         }

         if (p == NEUTRAL) {
            s = ForceSide.NEUTRAL;
            break;
         }
      }

      this.side = s;
      int[] cell = ICON_CELLS.get(name);
      this.iconX = cell != null ? cell[0] : 0;
      this.iconY = cell != null ? cell[1] : 3;
      POWERS.add(this);
      BY_NAME.put(name, this);
   }

   private Power(String name, int dx, int dy, Power parent) {
      this(name, dx, dy, parent, new PowerStats(0));
   }

   public String getName() {
      return this.name;
   }

   public int id() {
      return POWERS.indexOf(this);
   }

   public String getBaseName() {
      char c = this.name.charAt(this.name.length() - 1);
      return Character.isDigit(c) ? this.name.substring(0, this.name.length() - 1) : this.name;
   }

   public int getTier() {
      return Character.isDigit(this.name.charAt(this.name.length() - 1)) ? this.name.charAt(this.name.length() - 1) - 48 : 1;
   }

   public MutableText getDisplayName() {
      return Text.translatable("forcepower.name." + this.name).formatted(this.side.theme);
   }

   public ForceSide getSide() {
      return this.side;
   }

   public float getUseCost(net.minecraft.server.network.ServerPlayerEntity player) {
      return this.stats.useCost;
   }

   public static Power getSelected(net.minecraft.server.network.ServerPlayerEntity player) {
      return ForceManager.getSelectedPower(player);
   }

   public static int getStunDurationTicks(int chokeAmplifier) {
      float f = 1.5F;
      if (chokeAmplifier > 0) {
         f += 1.0F + (chokeAmplifier - 1) * 0.5F;
      }

      return (int)(f * 20.0F);
   }

   public static Power forEffect(String base, int amplifier) {
      Power fallback = null;

      for (Power p : POWERS) {
         if (p.getBaseName().equals(base)) {
            if (p.getTier() == amplifier + 1) {
               return p;
            }

            if (fallback == null) {
               fallback = p;
            }
         }
      }

      return fallback;
   }

   public MutableText getDescription() {
      return Text.translatable("forcepower.desc." + this.getBaseName());
   }

   public boolean isCastable() {
      return this.stats.useCost > 0.0F && this.stats.powerType != PowerType.PASSIVE;
   }

   public boolean isRegenPercent() {

      return Set.of("level1", "level2", "level3", "level4", "level5").contains(this.name);
   }

   public int getActualXpCost(float oppositeCompletion) {
      int cost = this.stats.xpCost;
      if (this.side.isPolar()) {
         cost += Math.round((float)cost * oppositeCompletion);
      }

      return cost;
   }

   public boolean isDescendantOf(Power of) {
      for (Power p = this.parent; p != null; p = p.parent) {
         if (p == of) {
            return true;
         }
      }

      return false;
   }

   public static Power byName(String name) {
      return BY_NAME.get(name);
   }

   public static List<Power> allCastable() {
      List<Power> list = new ArrayList<>();

      for (Power p : POWERS) {
         if (p.isCastable()) {
            list.add(p);
         }
      }

      return list;
   }

   public static List<Power> sidePowers(ForceSide side) {
      List<Power> list = new ArrayList<>();

      for (Power p : POWERS) {
         if (p.side == side) {
            list.add(p);
         }
      }

      return list;
   }

   public int compareTo(Power o) {
      int result = this.side.compareTo(o.side);
      return result != 0 ? result : this.name.compareTo(o.name);
   }

   @Override
   public String toString() {
      return "Power[" + this.name + "]";
   }
}
