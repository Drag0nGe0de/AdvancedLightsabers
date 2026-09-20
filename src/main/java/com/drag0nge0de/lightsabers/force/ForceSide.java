package com.drag0nge0de.lightsabers.force;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum ForceSide {
   NONE(Formatting.WHITE),
   LIGHT(Formatting.AQUA),
   DARK(Formatting.RED),
   NEUTRAL(Formatting.YELLOW);

   public final Formatting theme;

   private ForceSide(Formatting theme) {
      this.theme = theme;
   }

   public Power getRoot() {
      return switch (this) {
         case LIGHT -> Power.LIGHT_SIDE;
         case DARK -> Power.DARK_SIDE;
         case NEUTRAL -> Power.NEUTRAL;
         default -> null;
      };
   }

   public ForceSide getOpposite() {
      return switch (this) {
         case LIGHT -> DARK;
         case DARK -> LIGHT;
         default -> this;
      };
   }

   public boolean isPolar() {
      return this == LIGHT || this == DARK;
   }

   public Text getDisplayName() {
      return Text.translatable("force.side." + this.name().toLowerCase());
   }
}
