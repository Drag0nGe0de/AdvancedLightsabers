package com.drag0nge0de.lightsabers.world;

import com.drag0nge0de.lightsabers.block.CrystalOreBlock;
import com.drag0nge0de.lightsabers.registry.ALBlocks;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class CrystalRelicScanner {

   private CrystalRelicScanner() {
   }

   public static void register() {
      ServerChunkEvents.CHUNK_LOAD.register(CrystalRelicScanner::onChunkLoad);
   }

   private static void onChunkLoad(ServerWorld world, WorldChunk chunk) {
      ChunkSection[] sections = chunk.getSectionArray();
      int bottomY = chunk.getBottomY();
      int baseX = chunk.getPos().getStartX();
      int baseZ = chunk.getPos().getStartZ();

      for (int si = 0; si < sections.length; si++) {
         ChunkSection section = sections[si];
         if (section.isEmpty() || !section.hasAny(CrystalRelicScanner::isBrokenRelicState)) {
            continue;
         }

         int sectionBaseY = bottomY + (si << 4);
         for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
               for (int x = 0; x < 16; x++) {
                  if (isBrokenRelicState(section.getBlockState(x, y, z))) {
                     world.scheduleBlockTick(new BlockPos(baseX + x, sectionBaseY + y, baseZ + z),
                           ALBlocks.CRYSTAL_ORE, 2 + world.getRandom().nextInt(5));
                  }
               }
            }
         }
      }
   }

   private static boolean isBrokenRelicState(BlockState state) {
      return state.getBlock() instanceof CrystalOreBlock && state.get(CrystalOreBlock.ATTACHMENT) == 0;
   }
}
