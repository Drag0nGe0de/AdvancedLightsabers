package com.drag0nge0de.lightsabers.world;

import com.drag0nge0de.lightsabers.block.CrystalOreBlock;
import com.drag0nge0de.lightsabers.block.blockentity.CrystalOreBlockEntity;
import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class CrystalCaveFeature extends Feature<DefaultFeatureConfig> {
   private static final long MAGIC_X2 = 0x4c1906L;
   private static final long MAGIC_X = 0x5ac0dbL;
   private static final long MAGIC_Z2 = 0x4307a7L;
   private static final long MAGIC_Z = 0x5f24fL;
   private static final long MAGIC_SEED = 0x3ad8025fL;

   public CrystalCaveFeature(Codec<DefaultFeatureConfig> codec) {
      super(codec);
   }

   public static boolean isCrystalCaveChunk(long seed, int chunkX, int chunkZ) {
      float rate = com.drag0nge0de.lightsabers.config.ALConfig.get().crystalCaveRate;
      int window = rate >= 32.0F ? 1 : Math.max(1, Math.round(33.0F / Math.max(rate, 0.001F)));
      long value = seed
         + (long) chunkX * (long) chunkX * MAGIC_X2
         + (long) chunkX * MAGIC_X
         + (long) chunkZ * (long) chunkZ * MAGIC_Z2
         + (long) chunkZ * MAGIC_Z
         ^ MAGIC_SEED;
      net.minecraft.util.math.random.Random rand = net.minecraft.util.math.random.Random.create(value);
      return rand.nextInt(window) == 0;
   }

   @Override
   public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
      StructureWorldAccess world = context.getWorld();
      BlockPos origin = context.getOrigin();
      if (world.toServerWorld().getRegistryKey() != World.OVERWORLD) {
         return false;
      }

      if (world.getBiome(origin).isIn(BiomeTags.IS_OCEAN)) {
         return false;
      }

      int chunkX = origin.getX() >> 4;
      int chunkZ = origin.getZ() >> 4;
      long seed = world.getSeed();
      if (!isCrystalCaveChunk(seed, chunkX, chunkZ)) {
         return false;
      }

      if (com.drag0nge0de.lightsabers.world.StructureGuard.hasGuardedStructure(world, origin)) {
         return false;
      }

      java.util.Random random = new java.util.Random(seed ^ (long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
      List<BlockPos> airBlocks = new ArrayList<>();
      int baseX = chunkX << 4;
      int baseZ = chunkZ << 4;
      int bottom = world.getBottomY() + 1;

      for (int i = 0; i < 16; i++) {
         for (int j = 0; j < 16; j++) {
            int x = baseX + i;
            int z = baseZ + j;
            int top = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) - 10;

            for (int y = bottom; y < top; y++) {
               BlockPos pos = new BlockPos(x, y, z);
               if (world.getBlockState(pos).isAir()) {
                  airBlocks.add(pos);
               }
            }
         }
      }

      if (airBlocks.size() <= 1024) {
         return false;
      }

      BlockPos start = airBlocks.get(random.nextInt(airBlocks.size()));
      int x1 = start.getX();
      int y1 = start.getY();
      int z1 = start.getZ();
      int count = 0;
      int surface = world.getTopY(Heightmap.Type.WORLD_SURFACE, x1, z1);

      while (y1 < surface) {
         if (count < 10 + random.nextInt(10)) {
            y1++;
         } else {
            if (random.nextInt(3) == 0) {
               x1 += (random.nextInt(3) - 1) * 2;
            }

            if (random.nextInt(9) == 0) {
               y1++;
            }

            if (random.nextInt(3) == 0) {
               z1 += (random.nextInt(3) - 1) * 2;
            }
         }

         x1 = MathHelper.clamp(x1, baseX, baseX + 15);
         z1 = MathHelper.clamp(z1, baseZ, baseZ + 15);
         carveEntrance(world, random, x1, y1, z1, 32, baseX, baseZ);
         count++;
      }

      generateCrystals(world, random, baseX, baseZ);

      return true;
   }

   private void generateCrystals(StructureWorldAccess world, java.util.Random random, int chunkX, int chunkZ) {
      for (int i = 0; i < 100; i++) {
         int x = chunkX + random.nextInt(16);
         int y = random.nextInt(64);
         int z = chunkZ + random.nextInt(16);
         BlockPos pos = new BlockPos(x, y, z);
         if (!world.getBlockState(pos).isAir()) {
            continue;
         }

         int attachment = CrystalOreBlock.autoAttach(world, pos);
         if (attachment == 0) {
            continue;
         }

         if (world.setBlockState(pos, ALBlocks.CRYSTAL_ORE.getDefaultState()
               .with(CrystalOreBlock.ATTACHMENT, attachment), 2)) {
            if (world.getBlockEntity(pos) instanceof CrystalOreBlockEntity be) {
               be.setColor(CrystalColor.rollWeighted(world.getRandom()).rgb);
            }
         }
      }
   }

   private boolean inChunk(BlockPos pos, int baseX, int baseZ) {
      return pos.getX() >= baseX && pos.getX() < baseX + 16 && pos.getZ() >= baseZ && pos.getZ() < baseZ + 16;
   }

   private void carveEntrance(StructureWorldAccess world, java.util.Random random, int x, int y, int z, int numberOfBlocks, int baseX, int baseZ) {
      double d0 = x + random.nextInt(3) - 2;
      double d1 = x + random.nextInt(3) - 2;
      double d2 = z + random.nextInt(3) - 2;
      double d3 = z + random.nextInt(3) - 2;
      double d4 = y + random.nextInt(3) - 2;
      double d5 = y + random.nextInt(3) - 2;

      for (int l = 0; l <= numberOfBlocks; l++) {
         double d6 = d0 + (d1 - d0) * l / numberOfBlocks;
         double d7 = d4 + (d5 - d4) * l / numberOfBlocks;
         double d8 = d2 + (d3 - d2) * l / numberOfBlocks;
         double d9 = random.nextDouble() * numberOfBlocks / 16.0;
         double d10 = (MathHelper.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
         double d11 = (MathHelper.sin(l * (float) Math.PI / numberOfBlocks) + 1.0F) * d9 + 1.0;
         int i1 = MathHelper.floor(d6 - d10 / 2.0);
         int j1 = MathHelper.floor(d7 - d11 / 2.0);
         int k1 = MathHelper.floor(d8 - d10 / 2.0);
         int l1 = MathHelper.floor(d6 + d10 / 2.0);
         int i2 = MathHelper.floor(d7 + d11 / 2.0);
         int j2 = MathHelper.floor(d8 + d10 / 2.0);

         for (int k2 = i1; k2 <= l1; k2++) {
            double d12 = (k2 + 0.5 - d6) / (d10 / 2.0);
            if (d12 * d12 < 1.0) {
               for (int l2 = j1; l2 <= i2; l2++) {
                  double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
                  if (d12 * d12 + d13 * d13 < 1.0) {
                     for (int i3 = k1; i3 <= j2; i3++) {
                        double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
                        if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0) {
                           BlockPos pos = new BlockPos(k2, l2, i3);
                           if (inChunk(pos, baseX, baseZ)) {
                              if (!world.isOutOfHeightLimit(pos) && !world.getBlockState(pos).isOf(Blocks.CHEST)
                                 && !world.getBlockState(pos).isOf(Blocks.BEDROCK)) {
                                 boolean nextToAir = !world.getBlockState(pos.north()).isAir()
                                    || !world.getBlockState(pos.south()).isAir()
                                    || !world.getBlockState(pos.up()).isAir()
                                    || !world.getBlockState(pos.down()).isAir()
                                    || !world.getBlockState(pos.east()).isAir()
                                    || !world.getBlockState(pos.west()).isAir();
                                 if (nextToAir) {
                                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                                 }
                              }

                              createWalls(world, pos.east(), baseX, baseZ);
                              createWalls(world, pos.west(), baseX, baseZ);
                              createWalls(world, pos.up(), baseX, baseZ);
                              createWalls(world, pos.down(), baseX, baseZ);
                              createWalls(world, pos.south(), baseX, baseZ);
                              createWalls(world, pos.north(), baseX, baseZ);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void createWalls(StructureWorldAccess world, BlockPos pos, int baseX, int baseZ) {
      if (!inChunk(pos, baseX, baseZ)) {
         return;
      }

      BlockState state = world.getBlockState(pos);
      if (!state.isAir() && !state.isOf(Blocks.BEDROCK) && !state.isOf(Blocks.CHEST)
         && !state.isOf(ALBlocks.CRYSTAL_ORE) && state.isSideSolidFullSquare(world, pos, Direction.UP)) {
         world.setBlockState(pos, Blocks.STONE.getDefaultState(), 2);
      }
   }
}
