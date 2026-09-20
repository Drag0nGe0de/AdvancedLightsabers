package com.drag0nge0de.lightsabers.world;

import com.drag0nge0de.lightsabers.registry.ALWorldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class WaterSafeJigsawStructure extends Structure {
   public static final MapCodec<WaterSafeJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      Structure.configCodecBuilder(instance),
      StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
      Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
      Codec.intRange(0, 7).fieldOf("size").forGetter(structure -> structure.maxDepth),
      HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
      Codec.BOOL.fieldOf("use_expansion_hack").forGetter(structure -> structure.useExpansionHack),
      Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
      Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
      StructurePoolAliasBinding.CODEC.listOf().optionalFieldOf("pool_aliases", List.of()).forGetter(structure -> structure.poolAliasBindings),
      DimensionPadding.CODEC.optionalFieldOf("dimension_padding", DimensionPadding.NONE).forGetter(structure -> structure.dimensionPadding),
      StructureLiquidSettings.codec.optionalFieldOf("liquid_settings", StructureLiquidSettings.APPLY_WATERLOGGING).forGetter(structure -> structure.liquidSettings),
      Codec.intRange(0, 256).optionalFieldOf("water_check_radius", 0).forGetter(structure -> structure.waterCheckRadius)
   ).apply(instance, WaterSafeJigsawStructure::new));

   private static final int SURFACE_PROBE_DEPTH = 4;
   private static final int SAMPLE_STEP = 4;
   private static final int POND_SCAN_RADIUS = 24;

   private static final Map<Long, Map<Long, Boolean>> LIQUID_CACHE = new ConcurrentHashMap<>();

   private final RegistryEntry<StructurePool> startPool;
   private final Optional<Identifier> startJigsawName;
   private final int maxDepth;
   private final HeightProvider startHeight;
   private final boolean useExpansionHack;
   private final Optional<Heightmap.Type> projectStartToHeightmap;
   private final int maxDistanceFromCenter;
   private final List<StructurePoolAliasBinding> poolAliasBindings;
   private final DimensionPadding dimensionPadding;
   private final StructureLiquidSettings liquidSettings;
   private final int waterCheckRadius;
   private final JigsawStructure delegate;

   public WaterSafeJigsawStructure(Structure.Config config, RegistryEntry<StructurePool> startPool, Optional<Identifier> startJigsawName, int maxDepth, HeightProvider startHeight, boolean useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter, List<StructurePoolAliasBinding> poolAliasBindings, DimensionPadding dimensionPadding, StructureLiquidSettings liquidSettings, int waterCheckRadius) {
      super(config);
      this.startPool = startPool;
      this.startJigsawName = startJigsawName;
      this.maxDepth = maxDepth;
      this.startHeight = startHeight;
      this.useExpansionHack = useExpansionHack;
      this.projectStartToHeightmap = projectStartToHeightmap;
      this.maxDistanceFromCenter = maxDistanceFromCenter;
      this.poolAliasBindings = poolAliasBindings;
      this.dimensionPadding = dimensionPadding;
      this.liquidSettings = liquidSettings;
      this.waterCheckRadius = waterCheckRadius;
      this.delegate = new JigsawStructure(config, startPool, startJigsawName, maxDepth, startHeight, useExpansionHack, projectStartToHeightmap, maxDistanceFromCenter, poolAliasBindings, dimensionPadding, liquidSettings);
   }

   @Override
   public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
      return delegate.getStructurePosition(context);
   }

   @Override
   public Optional<Structure.StructurePosition> getValidStructurePosition(Structure.Context context) {
      return super.getValidStructurePosition(context).filter(position -> !hasLiquidAtSurface(context, position.position()));
   }

   @Override
   public StructureType<?> getType() {
      return ALWorldgen.WATER_SAFE_JIGSAW;
   }

   private boolean hasLiquidAtSurface(Structure.Context context, BlockPos anchor) {
      if (waterCheckRadius <= 0) {
         return false;
      }

      long seedKey = context.seed();
      long anchorKey = anchor.asLong();
      Map<Long, Boolean> byAnchor = LIQUID_CACHE.computeIfAbsent(seedKey, k -> new ConcurrentHashMap<>());
      Boolean cached = byAnchor.get(anchorKey);
      if (cached != null) {
         return cached;
      }

      boolean verdict = scanLiquid(context, anchor);
      byAnchor.put(anchorKey, verdict);
      return verdict;
   }

   private boolean scanLiquid(Structure.Context context, BlockPos anchor) {

      net.minecraft.world.biome.source.BiomeSource biomeSource = context.biomeSource();
      net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler sampler =
         context.noiseConfig().getMultiNoiseSampler();
      int seaY = net.minecraft.world.biome.source.BiomeCoords.fromBlock(63);

      for (int dx = -waterCheckRadius; dx <= waterCheckRadius; dx += 8) {
         for (int dz = -waterCheckRadius; dz <= waterCheckRadius; dz += 8) {
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.biome.Biome> biome =
               biomeSource.getBiome(net.minecraft.world.biome.source.BiomeCoords.fromBlock(anchor.getX() + dx),
                  seaY, net.minecraft.world.biome.source.BiomeCoords.fromBlock(anchor.getZ() + dz), sampler);

            if (biome.isIn(net.minecraft.registry.tag.BiomeTags.IS_OCEAN)
                  || biome.matchesKey(net.minecraft.world.biome.BiomeKeys.RIVER)
                  || biome.matchesKey(net.minecraft.world.biome.BiomeKeys.FROZEN_RIVER)) {
               return true;
            }
         }
      }

      ChunkGenerator generator = context.chunkGenerator();
      NoiseConfig noiseConfig = context.noiseConfig();
      HeightLimitView world = context.world();
      int bottom = world.getBottomY();
      int top = world.getTopY() - 1;

      int radius = Math.min(waterCheckRadius, POND_SCAN_RADIUS);

      for (int dx = -radius; dx <= radius; dx += SAMPLE_STEP) {
         for (int dz = -radius; dz <= radius; dz += SAMPLE_STEP) {
            BlockColumn column = generator.getColumnSample(anchor.getX() + dx, anchor.getZ() + dz, world, noiseConfig);
            int y = top;

            while (y >= bottom && column.getState(y).isAir()) {
               y--;
            }

            if (y < bottom || isLiquid(column.getState(y))) {
               return true;
            }

            for (int depth = 1; depth <= SURFACE_PROBE_DEPTH && y - depth >= bottom; depth++) {
               if (isLiquid(column.getState(y - depth))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static boolean isLiquid(BlockState state) {
      return !state.getFluidState().isEmpty()
         || state.isOf(Blocks.ICE)
         || state.isOf(Blocks.FROSTED_ICE)
         || state.isOf(Blocks.PACKED_ICE)
         || state.isOf(Blocks.BLUE_ICE);
   }
}
