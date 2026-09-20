package com.drag0nge0de.lightsabers.force;

public class PowerStats {
   public final int xpCost;
   public int baseBonus;
   public int forceBonus;
   public int regen;
   public float useCost;
   public int baseRequirement;
   public PowerType powerType = PowerType.PER_USE;

   public PowerStats(int xpCost) {
      this.xpCost = xpCost;
   }

   public PowerStats setBaseReq(int amount) {
      this.baseRequirement = amount;
      return this;
   }

   public PowerStats setBaseBonus(int amount) {
      this.baseBonus = amount;
      return this;
   }

   public PowerStats setForceBonus(int amount) {
      this.forceBonus = amount;
      return this;
   }

   public PowerStats setRegen(int amount) {
      this.regen = amount;
      return this;
   }

   public PowerStats setUseCost(float cost) {
      this.useCost = cost;
      return this;
   }

   public PowerStats setType(PowerType type) {
      this.powerType = type;
      return this;
   }
}
