package com.drag0nge0de.lightsabers.component;

public enum CrystalColor {
   DEEP_BLUE(255),
   MEDIUM_BLUE(27647),
   LIGHT_BLUE(5880319),
   ARCTIC_BLUE(14546687),
   WHITE(16777215),
   INDIGO(6095103),
   PURPLE(11337901),
   MAGENTA(16711935),
   PINK(16748474),
   RED(16711680),
   BLOOD_ORANGE(16744448),
   AMBER(16758272),
   YELLOW(16776960),
   GOLD(16777018),
   LIME_GREEN(12582656),
   GREEN(65280),
   MINT_GREEN(65435),
   CYAN(65535);

   public final int rgb;

   private CrystalColor(int rgb) {
      this.rgb = rgb;
   }

   public static CrystalColor rollWeighted(net.minecraft.util.math.random.Random random) {
      int total = 9 * 90 + 4 * 30 + 3 * 10 + 2 * 1;
      int roll = random.nextInt(total);
      int weight = 90;

      for (CrystalColor c : COMMON) {
         if ((roll -= weight) < 0) {
            return c;
         }
      }

      weight = 30;

      for (CrystalColor c : UNCOMMON) {
         if ((roll -= weight) < 0) {
            return c;
         }
      }

      weight = 10;

      for (CrystalColor c : RARE) {
         if ((roll -= weight) < 0) {
            return c;
         }
      }

      return EPIC.length > 0 && (roll -= 1) < 0 ? EPIC[0] : EPIC[1];
   }

   public static final CrystalColor[] COMMON = {
      DEEP_BLUE, MEDIUM_BLUE, LIGHT_BLUE, AMBER, YELLOW, GOLD, LIME_GREEN, GREEN, MINT_GREEN
   };
   public static final CrystalColor[] UNCOMMON = {MAGENTA, PINK, RED, BLOOD_ORANGE};
   public static final CrystalColor[] RARE = {INDIGO, PURPLE, CYAN};
   public static final CrystalColor[] EPIC = {ARCTIC_BLUE, WHITE};

   public static CrystalColor nearest(int rgb) {
      CrystalColor best = DEEP_BLUE;
      int bestDist = Integer.MAX_VALUE;
      for (CrystalColor c : values()) {
         if (c.rgb == rgb) {
            return c;
         }
         int dr = ((c.rgb >> 16) & 255) - ((rgb >> 16) & 255);
         int dg = ((c.rgb >> 8) & 255) - ((rgb >> 8) & 255);
         int db = (c.rgb & 255) - (rgb & 255);
         int dist = dr * dr + dg * dg + db * db;
         if (dist < bestDist) {
            bestDist = dist;
            best = c;
         }
      }
      return best;
   }
}
