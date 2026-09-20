package com.drag0nge0de.lightsabers.world;

import com.drag0nge0de.lightsabers.AL;

import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StructureGuard {
   private static final Set<Identifier> GUARDED = Set.of(AL.id("jedi_temple"), AL.id("sith_tomb"));
   private static final Map<Identifier, Set<Long>> AIR_SETS = new HashMap<>();
   private static final Map<Identifier, Set<Long>> SOLID_SETS = new HashMap<>();
   private static final Map<Identifier, Map<Long, StructureStart>> RESOLVED_STARTS = new HashMap<>();
   private static final Set<QueuedChunk> QUEUED = new HashSet<>();
   private static final ArrayDeque<QueuedChunk> QUEUE = new ArrayDeque<>();
   private static final int CHUNKS_PER_TICK = 64;

   private record QueuedChunk(ServerWorld world, long pos) {}

   public static void clearSessionState() {
      RESOLVED_STARTS.clear();
      QUEUED.clear();
      QUEUE.clear();
   }

   public static boolean hasGuardedStructure(net.minecraft.world.StructureWorldAccess world, BlockPos pos) {
      Chunk chunk = world.getChunk(pos);
      ChunkPos chunkPos = new ChunkPos(pos);
      BlockBox chunkBox = new BlockBox(
         chunkPos.getStartX(), world.getBottomY(), chunkPos.getStartZ(),
         chunkPos.getStartX() + 15, world.getTopY() - 1, chunkPos.getStartZ() + 15);
      Registry<Structure> registry = world.toServerWorld().getRegistryManager().get(RegistryKeys.STRUCTURE);
      ServerWorld serverWorld = world.toServerWorld();

      for (Identifier id : GUARDED) {
         Structure structure = registry.get(id);

         if (structure == null) {
            continue;
         }

         StructureStart own = chunk.getStructureStart(structure);

         if (own != null && own.hasChildren() && own.getBoundingBox().intersects(chunkBox)) {
            return true;
         }

         LongSet references = chunk.getStructureReferences(structure);

         if (references == null) {
            continue;
         }

         for (long ref : references) {
            StructureStart start = resolveStart(serverWorld, structure, id, new ChunkPos(ref));

            if (start != null && start.hasChildren() && start.getBoundingBox().intersects(chunkBox)) {
               return true;
            }
         }
      }

      return false;
   }

   public static void onChunkLoad(ServerWorld world, WorldChunk chunk) {
      ChunkPos center = chunk.getPos();

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            enqueue(world, new ChunkPos(center.x + dx, center.z + dz));
         }
      }
   }

   public static void onServerTick(MinecraftServer server) {
      int budget = CHUNKS_PER_TICK;

      while (budget > 0 && !QUEUE.isEmpty()) {
         QueuedChunk entry = QUEUE.pollFirst();

         if (entry == null || !QUEUED.remove(entry)) {
            continue;
         }

         budget--;

         try {
            ServerWorld world = entry.world();

            if (!world.isChunkLoaded(ChunkPos.getPackedX(entry.pos()), ChunkPos.getPackedZ(entry.pos()))) {
               continue;
            }

            clearInChunk(world, new ChunkPos(entry.pos()));
         } catch (Exception e) {
            AL.LOGGER.warn("structure guard: failed to clear chunk {}", entry.pos(), e);
         }
      }
   }

   private static void enqueue(ServerWorld world, ChunkPos pos) {
      QueuedChunk entry = new QueuedChunk(world, pos.toLong());

      if (QUEUED.add(entry)) {
         QUEUE.addLast(entry);
      }
   }

   private static void clearInChunk(ServerWorld world, ChunkPos center) {
      WorldChunk chunk = world.getChunk(center.x, center.z);
      Registry<Structure> registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
      BlockBox chunkBox = new BlockBox(
         center.getStartX(), world.getBottomY(), center.getStartZ(),
         center.getStartX() + 15, world.getTopY() - 1, center.getStartZ() + 15);

      for (Identifier id : GUARDED) {
         Structure structure = registry.get(id);

         if (structure == null) {
            continue;
         }

         Set<Long> airSet = loadAirSet(id);

         if (airSet.isEmpty()) {
            continue;
         }

         StructureStart own = chunk.getStructureStart(structure);

         if (own != null && own.hasChildren()) {
            clearStructureInChunk(world, chunkBox, id, airSet, own);
         }

         LongSet references = chunk.getStructureReferences(structure);

         if (references == null) {
            continue;
         }

         for (long ref : references) {
            if (ref == center.toLong()) {
               continue;
            }

            StructureStart start = resolveStart(world, structure, id, new ChunkPos(ref));

            if (start != null && start.hasChildren()) {
               clearStructureInChunk(world, chunkBox, id, airSet, start);
            }
         }
      }
   }

   private static StructureStart resolveStart(ServerWorld world, Structure structure, Identifier id, ChunkPos startChunkPos) {
      Map<Long, StructureStart> cache = RESOLVED_STARTS.computeIfAbsent(id, key -> new HashMap<>());
      long key = startChunkPos.toLong();

      if (cache.containsKey(key)) {
         return cache.get(key);
      }

      ServerChunkManager manager = world.getChunkManager();
      ChunkGenerator generator = manager.getChunkGenerator();
      StructureStart start = structure.createStructureStart(
         world.getRegistryManager(), generator, generator.getBiomeSource(), manager.getNoiseConfig(),
         world.getStructureTemplateManager(), world.getSeed(), startChunkPos, 0, world,
         biome -> true);

      if (!start.hasChildren()) {
         start = null;
      }

      cache.put(key, start);
      return start;
   }

   private static final int RIPPLE = 8;
   private static final int BELOW_DEPTH = 8;
   private static final int ABOVE_DEPTH = 8;

   private static void clearStructureInChunk(ServerWorld world, BlockBox chunkBox, Identifier id, Set<Long> airSet, StructureStart start) {
      for (StructurePiece piece : start.getChildren()) {
         if (!(piece instanceof PoolStructurePiece poolPiece)) {
            continue;
         }

         BlockBox box = poolPiece.getBoundingBox();

         if (!box.intersects(chunkBox)) {
            continue;
         }

         BlockPos origin = poolPiece.getPos();
         BlockRotation rotation = poolPiece.getRotation();
         int minX = Math.max(box.getMinX(), chunkBox.getMinX());
         int maxX = Math.min(box.getMaxX(), chunkBox.getMaxX());
         int minZ = Math.max(box.getMinZ(), chunkBox.getMinZ());
         int maxZ = Math.min(box.getMaxZ(), chunkBox.getMaxZ());

         for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
               for (int y = box.getMinY(); y <= box.getMaxY(); y++) {
                  BlockPos worldPos = new BlockPos(x, y, z);
                  BlockPos rel = unrotate(worldPos.subtract(origin), rotation);
                  long packed = pack(rel.getX(), rel.getY(), rel.getZ());
                  boolean airCell = airSet.contains(packed);

                  if (!airCell && SOLID_SETS.get(id).contains(packed)) {
                     continue;
                  }

                  BlockState state = world.getBlockState(worldPos);

                  if (isVegetation(state) && (airCell || state.getFluidState().isEmpty())) {
                     world.setBlockState(worldPos, Blocks.AIR.getDefaultState(), 2);
                  } else if (airCell && isStrayLiquid(state)) {
                     world.setBlockState(worldPos, Blocks.AIR.getDefaultState(), 2);
                  }
               }
            }
         }

         clearTreeRippleInChunk(world, chunkBox, box);
      }
   }

   private static void clearTreeRippleInChunk(ServerWorld world, BlockBox chunkBox, BlockBox box) {
      int minX = Math.max(box.getMinX() - RIPPLE, chunkBox.getMinX());
      int maxX = Math.min(box.getMaxX() + RIPPLE, chunkBox.getMaxX());
      int minZ = Math.max(box.getMinZ() - RIPPLE, chunkBox.getMinZ());
      int maxZ = Math.min(box.getMaxZ() + RIPPLE, chunkBox.getMaxZ());
      int minY = Math.max(box.getMinY() - BELOW_DEPTH, chunkBox.getMinY());
      int maxY = Math.min(box.getMaxY() + ABOVE_DEPTH, chunkBox.getMaxY());

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            boolean outsideFootprint = x < box.getMinX() || x > box.getMaxX()
               || z < box.getMinZ() || z > box.getMaxZ();

            for (int y = minY; y <= maxY; y++) {
               if (!outsideFootprint && y >= box.getMinY() && y <= box.getMaxY()) {
                  continue;
               }

               BlockPos worldPos = new BlockPos(x, y, z);
               BlockState state = world.getBlockState(worldPos);

               if (state.getFluidState().isEmpty() && isVegetation(state)) {
                  world.setBlockState(worldPos, Blocks.AIR.getDefaultState(), 2);
               }
            }
         }
      }
   }

   private static Set<Long> loadAirSet(Identifier id) {
      Set<Long> set = AIR_SETS.computeIfAbsent(id, key -> new HashSet<>());
      Set<Long> solids = SOLID_SETS.computeIfAbsent(id, key -> new HashSet<>());

      if (!set.isEmpty() || !solids.isEmpty()) {
         return set;
      }

      String path = "/data/" + id.getNamespace() + "/structure/" + id.getPath() + ".nbt";

      try (InputStream stream = StructureGuard.class.getResourceAsStream(path)) {
         if (stream == null) {
            AL.LOGGER.warn("structure guard: template {} not found", id);
            return set;
         }

         NbtCompound root = NbtIo.readCompressed(stream, NbtSizeTracker.ofUnlimitedBytes());
         NbtList palette = root.getList("palette", NbtElement.COMPOUND_TYPE);
         NbtList blocks = root.getList("blocks", NbtElement.COMPOUND_TYPE);
         Set<Integer> airStates = new HashSet<>();

         for (int i = 0; i < palette.size(); i++) {
            if ("minecraft:air".equals(palette.getCompound(i).getString("Name"))) {
               airStates.add(i);
            }
         }

         for (int i = 0; i < blocks.size(); i++) {
            NbtCompound block = blocks.getCompound(i);
            NbtList pos = block.getList("pos", NbtElement.INT_TYPE);
            long packed = pack(pos.getInt(0), pos.getInt(1), pos.getInt(2));

            if (airStates.contains(block.getInt("state"))) {
               set.add(packed);
            } else {
               solids.add(packed);
            }
         }

         AL.LOGGER.info("structure guard: template {} loaded with {} air cells and {} solid records", id, set.size(), solids.size());
      } catch (Exception e) {
         AL.LOGGER.warn("structure guard: failed to load template {}", id, e);
      }

      return set;
   }

   private static long pack(int x, int y, int z) {
      return ((long) (x + 512) << 40) | ((long) (y + 512) << 20) | (long) (z + 512);
   }

   private static BlockPos unrotate(BlockPos pos, BlockRotation rotation) {
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();

      switch (rotation) {
         case CLOCKWISE_90:
            return new BlockPos(z, y, -x);
         case CLOCKWISE_180:
            return new BlockPos(-x, y, -z);
         case COUNTERCLOCKWISE_90:
            return new BlockPos(-z, y, x);
         default:
            return pos;
      }
   }

   private static boolean isVegetation(BlockState state) {
      return !state.isAir() && (
         state.isReplaceable()
            || state.isIn(BlockTags.LOGS)
            || state.isIn(BlockTags.LEAVES)
            || state.isIn(BlockTags.SAPLINGS)
            || state.isIn(BlockTags.FLOWERS)
            || state.isOf(Blocks.VINE)
      );
   }

   private static boolean isStrayLiquid(BlockState state) {
      return !state.getFluidState().isEmpty()
         || state.isOf(Blocks.ICE)
         || state.isOf(Blocks.FROSTED_ICE)
         || state.isOf(Blocks.PACKED_ICE)
         || state.isOf(Blocks.BLUE_ICE)
         || state.isOf(Blocks.SNOW);
   }
}
