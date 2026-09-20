package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.block.CrystalOreBlock;
import com.drag0nge0de.lightsabers.block.DisassemblyStationBlock;
import com.drag0nge0de.lightsabers.block.HolocronBlock;
import com.drag0nge0de.lightsabers.block.LightsaberForgeBlock;
import com.drag0nge0de.lightsabers.block.LightsaberStandBlock;
import com.drag0nge0de.lightsabers.block.SithSarcophagusBlock;
import com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public class ALBlocks {
   public static final List<Block> FORCESTONE_FAMILY = new ArrayList<>();
   public static final Block LIGHTSABER_FORGE = registerForge(
      "lightsaber_forge", LightsaberForgeBlock::new, Settings.create().strength(3.5F).requiresTool().nonOpaque().sounds(BlockSoundGroup.METAL)
   );

   public static final Block LIGHTSABER_FORGE_DARK = registerForge(
      "lightsaber_forge_dark", LightsaberForgeBlock::new, Settings.create().strength(3.5F).requiresTool().nonOpaque().sounds(BlockSoundGroup.METAL)
   );
   public static final Block CRYSTAL_ORE = register(
      "crystal_ore", CrystalOreBlock::new,

      Settings.create().strength(2.0F, 10.0F).sounds(BlockSoundGroup.GLASS)
         .nonOpaque().noCollision().luminance(state -> 3)
   );
   public static final Block LIGHTSABER_STAND = register(
      "lightsaber_stand", LightsaberStandBlock::new, Settings.create().strength(1.5F).sounds(BlockSoundGroup.METAL).nonOpaque()
   );
   public static final Block HOLOCRON_JEDI = register(
      "holocron_jedi",
      settings -> new HolocronBlock(false, settings),
      Settings.create().strength(3.0F).requiresTool().luminance(state -> 7).nonOpaque().sounds(BlockSoundGroup.GLASS)
   );
   public static final Block HOLOCRON_SITH = register(
      "holocron_sith",
      settings -> new HolocronBlock(true, settings),
      Settings.create().strength(3.0F).requiresTool().luminance(state -> 5).nonOpaque().sounds(BlockSoundGroup.GLASS)
   );
   public static final Block SITH_SARCOPHAGUS = register(
      "sith_sarcophagus", SithSarcophagusBlock::new, Settings.create().strength(50.0F, 2000.0F).nonOpaque().sounds(BlockSoundGroup.STONE).dropsNothing()
   );

   public static final Block SITH_STONE_COFFIN = registerStoneCoffin(
      "sith_stone_coffin", SithStoneCoffinBlock::new, Settings.create().strength(50.0F, 2000.0F).nonOpaque().sounds(BlockSoundGroup.STONE).dropsNothing()
   );
   public static final Block DISASSEMBLY_STATION = register(
      "disassembly_station", DisassemblyStationBlock::new,

      Settings.create().strength(3.5F).requiresTool().nonOpaque().sounds(BlockSoundGroup.METAL)
   );
   public static final Block FORCESTONE = forcestone("forcestone");
   public static final Block DARK_FORCESTONE = forcestone("dark_forcestone");
   public static final Block LIGHT_FORCESTONE = forcestone("light_forcestone");
   public static final Block FORCESTONE_CRACKED = forcestone("forcestone_cracked");
   public static final Block FORCESTONE_MOSSY = forcestone("forcestone_mossy");
   public static final Block FORCESTONE_INSCRIBED = forcestone("forcestone_inscribed");
   public static final Block DARK_FORCESTONE_CRACKED = forcestone("dark_forcestone_cracked");
   public static final Block DARK_FORCESTONE_MOSSY = forcestone("dark_forcestone_mossy");
   public static final Block DARK_FORCESTONE_INSCRIBED = forcestone("dark_forcestone_inscribed");
   public static final Block LIGHT_FORCESTONE_CRACKED = forcestone("light_forcestone_cracked");
   public static final Block LIGHT_FORCESTONE_MOSSY = forcestone("light_forcestone_mossy");
   public static final Block LIGHT_FORCESTONE_INSCRIBED = forcestone("light_forcestone_inscribed");
   public static final Block FORCESTONE_PILLAR = pillar("forcestone_pillar");
   public static final Block DARK_FORCESTONE_PILLAR = pillar("dark_forcestone_pillar");
   public static final Block LIGHT_FORCESTONE_PILLAR = pillar("light_forcestone_pillar");
   public static final Block DARK_ACTIVATED_FORCESTONE_PILLAR = pillarGlowing("dark_activated_forcestone_pillar");
   public static final Block LIGHT_ACTIVATED_FORCESTONE_PILLAR = pillarGlowing("light_activated_forcestone_pillar");
   public static final Block FORCESTONE_STAIRS = register(
      "forcestone_stairs", settings -> new StairsBlock(FORCESTONE.getDefaultState(), settings), forcestoneSettings()
   );
   public static final Block DARK_FORCESTONE_STAIRS = register(
      "dark_forcestone_stairs", settings -> new StairsBlock(DARK_FORCESTONE.getDefaultState(), settings), forcestoneSettings()
   );
   public static final Block LIGHT_FORCESTONE_STAIRS = register(
      "light_forcestone_stairs", settings -> new StairsBlock(LIGHT_FORCESTONE.getDefaultState(), settings), forcestoneSettings()
   );
   public static final Block FORCESTONE_SLAB = register("forcestone_slab", SlabBlock::new, forcestoneSettings());
   public static final Block DARK_FORCESTONE_SLAB = register("dark_forcestone_slab", SlabBlock::new, forcestoneSettings());
   public static final Block LIGHT_FORCESTONE_SLAB = register("light_forcestone_slab", SlabBlock::new, forcestoneSettings());

   private static Block registerStoneCoffin(String name, Function<Settings, Block> factory, Settings settings) {
      Block block = (Block)Registry.register(Registries.BLOCK, AL.id(name), factory.apply(settings));
      Registry.register(Registries.ITEM, AL.id(name),
         new com.drag0nge0de.lightsabers.item.SithStoneCoffinBlockItem(block, new net.minecraft.item.Item.Settings()));
      return block;
   }

   private static Block registerForge(String name, Function<Settings, Block> factory, Settings settings) {
      Block block = (Block)Registry.register(Registries.BLOCK, AL.id(name), factory.apply(settings));
      Registry.register(Registries.ITEM, AL.id(name),
         new com.drag0nge0de.lightsabers.item.LightsaberForgeBlockItem(block, new net.minecraft.item.Item.Settings()));
      return block;
   }

   private static Settings forcestoneSettings() {
      return Settings.create().strength(2.0F, 6.0F).requiresTool().sounds(BlockSoundGroup.STONE);
   }

   private static Block forcestone(String name) {
      Block block = register(name, Block::new, forcestoneSettings());
      FORCESTONE_FAMILY.add(block);
      return block;
   }

   private static Block pillar(String name) {
      Block block = register(name, PillarBlock::new, forcestoneSettings());
      FORCESTONE_FAMILY.add(block);
      return block;
   }

   private static Block pillarGlowing(String name) {
      Block block = register(name, PillarBlock::new, forcestoneSettings().luminance(state -> 15));
      FORCESTONE_FAMILY.add(block);
      return block;
   }

   private static Block register(String name, Function<Settings, Block> factory, Settings settings) {
      Block block = (Block)Registry.register(Registries.BLOCK, AL.id(name), factory.apply(settings));
      Registry.register(Registries.ITEM, AL.id(name), new BlockItem(block, new net.minecraft.item.Item.Settings()));
      return block;
   }

   public static void register() {
   }
}
