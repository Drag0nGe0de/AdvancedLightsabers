package com.drag0nge0de.lightsabers.mixin;

import com.drag0nge0de.lightsabers.config.ALConfig;

import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(RandomSpreadStructurePlacement.class)
public abstract class RandomSpreadStructurePlacementMixin {

   @Mutable
   @Shadow
   @Final
   private int spacing;

   @Shadow
   @Final
   private int separation;

   private static final Map<RandomSpreadStructurePlacement, Integer> BASE_SPACING = new WeakHashMap<>();

   @Inject(method = "getStartChunk", at = @At("HEAD"))
   private void al$applySpawnRate(long seed, int chunkX, int chunkZ, CallbackInfoReturnable<?> cir) {
      RandomSpreadStructurePlacement self = (RandomSpreadStructurePlacement)(Object)this;
      int salt = ((StructurePlacementAccessor)(Object)this).al$getSalt();
      float rate = switch (salt) {
         case 712456831 -> ALConfig.get().jediTempleSpawnRate;
         case 519934221 -> ALConfig.get().sithTombSpawnRate;
         default -> 1.0F;
      };

      if (rate == 1.0F) {
         Integer restored = BASE_SPACING.remove(self);
         if (restored != null) {
            this.spacing = restored;
         }

         return;
      }

      Integer base = BASE_SPACING.get(self);
      if (base == null) {
         base = this.spacing;
         BASE_SPACING.put(self, base);
      }

      int scaled;
      if (rate <= 0.001F) {
         scaled = Integer.MAX_VALUE / 4;
      } else {
         scaled = Math.max(this.separation + 2, Math.round(base / (float) Math.sqrt(rate)));
      }

      this.spacing = scaled;
   }
}
