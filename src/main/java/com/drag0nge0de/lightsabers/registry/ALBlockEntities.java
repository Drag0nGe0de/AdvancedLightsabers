package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.block.blockentity.CrystalOreBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.HolocronBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.LightsaberForgeBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.LightsaberStandBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.SithSarcophagusBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.SithStoneCoffinBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityType.Builder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ALBlockEntities {
   public static final BlockEntityType<LightsaberStandBlockEntity> LIGHTSABER_STAND = register(
      "lightsaber_stand", Builder.create(LightsaberStandBlockEntity::new, new Block[]{ALBlocks.LIGHTSABER_STAND})
   );
   public static final BlockEntityType<HolocronBlockEntity> HOLOCRON = register(
      "holocron", Builder.create(HolocronBlockEntity::new, new Block[]{ALBlocks.HOLOCRON_JEDI, ALBlocks.HOLOCRON_SITH})
   );
   public static final BlockEntityType<LightsaberForgeBlockEntity> LIGHTSABER_FORGE = register(
      "lightsaber_forge", Builder.create(LightsaberForgeBlockEntity::new, new Block[]{ALBlocks.LIGHTSABER_FORGE, ALBlocks.LIGHTSABER_FORGE_DARK})
   );
   public static final BlockEntityType<CrystalOreBlockEntity> CRYSTAL_ORE = register(
      "crystal_ore", Builder.create(CrystalOreBlockEntity::new, new Block[]{ALBlocks.CRYSTAL_ORE})
   );
   public static final BlockEntityType<DisassemblyStationBlockEntity> DISASSEMBLY_STATION = register(
      "disassembly_station", Builder.create(DisassemblyStationBlockEntity::new, new Block[]{ALBlocks.DISASSEMBLY_STATION})
   );
   public static final BlockEntityType<SithSarcophagusBlockEntity> SITH_SARCOPHAGUS = register(
      "sith_sarcophagus", Builder.create(SithSarcophagusBlockEntity::new, new Block[]{ALBlocks.SITH_SARCOPHAGUS})
   );
   public static final BlockEntityType<SithStoneCoffinBlockEntity> SITH_STONE_COFFIN = register(
      "sith_stone_coffin", Builder.create(SithStoneCoffinBlockEntity::new, new Block[]{ALBlocks.SITH_STONE_COFFIN})
   );

   private static <T extends BlockEntity> BlockEntityType<T> register(String name, Builder<T> builder) {
      return (BlockEntityType<T>)Registry.register(Registries.BLOCK_ENTITY_TYPE, AL.id(name), builder.build(null));
   }

   public static void register() {
   }
}
