package com.drag0nge0de.lightsabers.hilt;

public enum Hilt {
   GRAFLEX("grafx", 8.0F),
   REDEEMER("redeemer", 7.0F),
   MAULER("mauler", 9.0F),
   PRODIGAL_SON("prodigal_son", 7.5F),
   KNIGHTED("knighted", 8.5F),
   VAID_ANCIENT("vaid_ancient", 8.0F),
   VAID_MODERN("vaid_modern", 8.0F),
   DROIDEKA("droideka", 7.0F),
   FULCRUM("fulcrum", 7.5F),
   JUGGERNAUT("juggernaut", 9.5F),
   MECHANICAL("mechanical", 7.0F),
   MANDALORIAN("mandalorian", 8.5F),
   FURY("fury", 9.0F),
   REBEL("rebel", 7.5F),
   IMPERIAL("imperial", 8.0F),
   REBORN("reborn", 9.0F);

   public static final Hilt DEFAULT = GRAFLEX;
   private final String id;
   private final float damageBonus;

   private Hilt(String id, float damageBonus) {
      this.id = id;
      this.damageBonus = damageBonus;
   }

   public String getId() {
      return this.id;
   }

   public float getDamageBonus() {
      return this.damageBonus;
   }

   public static Hilt byName(String name) {
      for (Hilt hilt : values()) {
         if (hilt.id.equals(name)) {
            return hilt;
         }
      }

      return DEFAULT;
   }
}
