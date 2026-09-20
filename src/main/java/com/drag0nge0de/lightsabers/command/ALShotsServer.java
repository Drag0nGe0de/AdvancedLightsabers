package com.drag0nge0de.lightsabers.command;

import java.util.ArrayList;
import java.util.List;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.block.blockentity.SithSarcophagusBlockEntity;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.FocusingComponent;
import com.drag0nge0de.lightsabers.component.FocusingCrystalType;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.entity.SithGhostEntity;
import com.drag0nge0de.lightsabers.force.ForceManager;
import com.drag0nge0de.lightsabers.entity.ThrownLightsaberEntity;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.item.CrystalPouchItem;
import com.drag0nge0de.lightsabers.item.PartItem;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.registry.ALWorldgen;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class ALShotsServer {

    public static final int CX = 0;
    public static final int CY = 120;
    public static final int CZ = 0;

    public static final BlockPos STAND_POS = new BlockPos(CX + 4, CY, CZ + 3);

    private static boolean built = false;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld world = server.getOverworld();
            buildPlatform(world);
            built = true;
            AL.LOGGER.info("[AL-SHOTS] platform built");
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!built) {
                return;
            }
            ServerPlayerEntity player = handler.player;

            if (Boolean.getBoolean("al.shots") && !player.getServer().getPlayerManager().isOperator(player.getGameProfile())) {
                player.getServer().getPlayerManager().addToOperators(player.getGameProfile());
            }
            equip(player);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> registerCommands(dispatcher));
    }

    private static int giveSaber(String hilt, int focusing,
            com.mojang.brigadier.context.CommandContext<net.minecraft.server.command.ServerCommandSource> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        boolean dbl = "mauler".equals(hilt);
        ItemStack s = new ItemStack(dbl ? ALItems.DOUBLE_LIGHTSABER : ALItems.LIGHTSABER);
        s.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                true, hilt, com.drag0nge0de.lightsabers.hilt.HiltStatsTable.stats(hilt).defaultColor(),
                dbl, focusing));
        p.getInventory().setStack(0, s);
        AL.LOGGER.info("[AL-SHOTS] gave {} saber {} focusing={}", p.getName().getString(), hilt, focusing);
        return 1;
    }

    private static void registerCommands(CommandDispatcher<net.minecraft.server.command.ServerCommandSource> d) {
        d.register(net.minecraft.server.command.CommandManager.literal("altest")
            .requires(src -> Boolean.getBoolean("al.shots"))
            .then(net.minecraft.server.command.CommandManager.literal("saber")
                .then(net.minecraft.server.command.CommandManager.argument("hilt",
                        com.mojang.brigadier.arguments.StringArgumentType.word())
                    .executes(ctx -> giveSaber(
                            com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "hilt"), 0, ctx))
                    .then(net.minecraft.server.command.CommandManager.argument("focusing",
                            com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
                        .executes(ctx -> giveSaber(
                            com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "hilt"),
                            com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "focusing"),
                            ctx)))))
            .then(net.minecraft.server.command.CommandManager.literal("scene2")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    p.getInventory().clear();
                    p.getInventory().setStack(0, stack(ALItems.CIRCUITRY, 1));
                    ItemStack kyberRed = new ItemStack(ALItems.KYBER_CRYSTAL);
                    kyberRed.set(ALComponents.CRYSTAL, new CrystalComponent(0xFF0000));
                    p.getInventory().setStack(1, kyberRed);
                    p.getInventory().setStack(2, com.drag0nge0de.lightsabers.item.PartItem.create("emitter",
                            com.drag0nge0de.lightsabers.hilt.Hilt.byName("knighted")));
                    p.getInventory().setStack(3, com.drag0nge0de.lightsabers.item.PartItem.create("switch_section",
                            com.drag0nge0de.lightsabers.hilt.Hilt.byName("knighted")));
                    p.getInventory().setStack(4, com.drag0nge0de.lightsabers.item.PartItem.create("body",
                            com.drag0nge0de.lightsabers.hilt.Hilt.byName("knighted")));
                    p.getInventory().setStack(5, com.drag0nge0de.lightsabers.item.PartItem.create("pommel",
                            com.drag0nge0de.lightsabers.hilt.Hilt.byName("knighted")));
                    p.getInventory().setStack(7, com.drag0nge0de.lightsabers.item.LightsaberItem.createSaber(
                            com.drag0nge0de.lightsabers.hilt.Hilt.byName("knighted"),
                            com.drag0nge0de.lightsabers.hilt.HiltStatsTable.stats("knighted").defaultColor(), false));
                    p.getInventory().setStack(6, new ItemStack(ALBlocks.LIGHTSABER_FORGE_DARK));
                    AL.LOGGER.info("[AL-SHOTS] scene2 inventory staged for saber core comparison");
                    return 1;
                }))
            .then(net.minecraft.server.command.CommandManager.literal("ore")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    ServerWorld w = p.getServerWorld();
                    BlockPos best = null;

                    BlockPos base = p.getBlockPos();
                    BlockPos bestBuried = null;
                    for (BlockPos pos : BlockPos.iterate(base.add(-20, -8, -20), base.add(20, 8, 20))) {
                        if (w.getBlockState(pos).isOf(ALBlocks.CRYSTAL_ORE)) {
                            boolean open = w.getBlockState(pos.up()).isAir();
                            for (net.minecraft.util.math.Direction dd : Direction.Type.HORIZONTAL) {
                                open |= w.getBlockState(pos.offset(dd)).isAir();
                            }
                            if (open) {
                                best = pos.toImmutable();
                                break;
                            }
                            bestBuried = pos.toImmutable();
                        }
                    }
                    if (best == null) {
                        best = bestBuried;
                    }
                    if (best != null) {
                        p.teleport(w, best.getX() + 1.6, best.getY() + 1.1, best.getZ() + 1.6, 135, 32);
                        AL.LOGGER.info("[AL-SHOTS] teleported to crystal ore display {}", best);
                    } else {
                        AL.LOGGER.error("[AL-SHOTS] no crystal_ore found near player");
                    }
                    return 1;
                }))
            .then(net.minecraft.server.command.CommandManager.literal("stand")
                .then(net.minecraft.server.command.CommandManager.argument("hilt",
                        com.mojang.brigadier.arguments.StringArgumentType.word())
                    .executes(ctx -> {
                        String hilt = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "hilt");
                        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                        ServerWorld w = p.getServerWorld();
                        boolean dbl = "mauler".equals(hilt);
                        ItemStack s = new ItemStack(dbl ? ALItems.DOUBLE_LIGHTSABER : ALItems.LIGHTSABER);
                        s.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                                true, hilt, 0x4FA8FF, dbl, 0));
                        if (w.getBlockEntity(STAND_POS) instanceof com.drag0nge0de.lightsabers.block.blockentity.LightsaberStandBlockEntity stand) {
                            stand.setDisplayStack(s);
                            AL.LOGGER.info("[AL-SHOTS] stand now holds {}", hilt);
                        } else {
                            AL.LOGGER.error("[AL-SHOTS] stand BE missing at {}", STAND_POS);
                        }
                        return 1;
                    })))
            .then(net.minecraft.server.command.CommandManager.literal("clientunlock")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    ForceManager.setXP(p, 100000);
                    boolean changed = true;
                    while (changed) {
                        changed = false;
                        for (com.drag0nge0de.lightsabers.force.Power power : com.drag0nge0de.lightsabers.force.Power.POWERS) {
                            if (com.drag0nge0de.lightsabers.force.ForceManager.unlockPowerUngated(p, power)) {
                                changed = true;
                            }
                        }
                    }
                    ForceManager.setEnergy(p, ForceManager.getMaxEnergy(p));
                    AL.LOGGER.info("[AL-SHOTS] clientunlock for {} (max energy {})",
                            p.getName().getString(), (int) ForceManager.getMaxEnergy(p));
                    return 1;
                }))
            .then(net.minecraft.server.command.CommandManager.literal("ghostcheck")
                .then(net.minecraft.server.command.CommandManager.argument("label",
                        com.mojang.brigadier.arguments.StringArgumentType.word())
                    .executes(ctx -> {
                        String label = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "label");
                        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                        ServerWorld w = p.getServerWorld();
                        StringBuilder sb = new StringBuilder("[AL-SHOTS] GHOSTCHECK ").append(label).append(":");
                        int ghostIdx = 0;
                        for (SithGhostEntity g : w.getEntitiesByClass(SithGhostEntity.class,
                                new Box(p.getBlockPos()).expand(48), gh -> true)) {
                            ghostIdx++;
                            ItemStack held = g.getMainHandStack();
                            LightsaberComponent lc = held.get(ALComponents.LIGHTSABER);
                            sb.append(" ghost#").append(ghostIdx)
                              .append(" hp=").append(String.format("%.1f", g.getHealth()))
                              .append(" held=").append(held.getItem().toString())
                              .append(" color=").append(lc != null ? Integer.toHexString(lc.color()) : "?")
                              .append(" active=").append(lc != null && lc.active())
                              .append(" rest=").append(g.hasRestingPlace)
                              .append(" task=").append(g.taskFinished)
                              .append(" at=").append(g.getBlockPos().toShortString());
                        }
                        sb.append(" ghostCount=").append(ghostIdx);
                        sb.append(" thrownSABERS=").append(w.getEntitiesByClass(ThrownLightsaberEntity.class,
                                new Box(p.getBlockPos()).expand(48), t -> true).size());
                        for (BlockPos pos : BlockPos.iterate(p.getBlockPos().add(-24, -8, -24), p.getBlockPos().add(24, 8, 24))) {
                            if (w.getBlockEntity(pos) instanceof SithSarcophagusBlockEntity sarc) {
                                StringBuilder inv = new StringBuilder();
                                for (int i = 0; i < sarc.size(); i++) {
                                    ItemStack s = sarc.getStack(i);
                                    if (!s.isEmpty()) {
                                        inv.append("slot").append(i).append("=").append(s.getItem()).append(" ");
                                    }
                                }
                                sb.append(" SARC@").append(pos.toShortString())
                                  .append("(").append(inv.length() == 0 ? "empty" : inv).append(")");
                            }
                        }
                        sb.append(" breakWall=").append(w.getBlockState(new BlockPos(0, 121, 15)).getBlock());
                        AL.LOGGER.info(sb.toString());
                        return 1;
                    })))
            .then(net.minecraft.server.command.CommandManager.literal("temple")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    ServerWorld w = p.getServerWorld();
                    Registry<net.minecraft.world.gen.structure.Structure> reg =
                            w.getRegistryManager().get(RegistryKeys.STRUCTURE);
                    var entry = reg.getEntry(RegistryKey.of(RegistryKeys.STRUCTURE, AL.id("jedi_temple")));
                    if (entry.isEmpty()) {
                        AL.LOGGER.error("[AL-SHOTS] jedi_temple not registered");
                        return 0;
                    }
                    var pair = w.getChunkManager().getChunkGenerator().locateStructure(w,
                            RegistryEntryList.of(entry.get()), BlockPos.ofFloored(p.getPos()), 100, false);
                    if (pair != null) {

                        BlockPos c = pair.getFirst();
                        w.getChunk(c.getX() >> 4, c.getZ() >> 4, net.minecraft.world.chunk.ChunkStatus.FULL, true);
                        int topY = w.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, c.getX(), c.getZ());
                        p.teleport(w, c.getX() + 0.5, topY + 1.5, c.getZ() + 0.5, 180, 10);
                        AL.LOGGER.info("[AL-SHOTS] worldgen temple at {}, landed on top ({})", c, topY);
                    } else {
                        AL.LOGGER.error("[AL-SHOTS] no worldgen jedi_temple within 100 chunks");
                    }
                    return 1;
                }))
            .then(net.minecraft.server.command.CommandManager.literal("floor")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    ServerWorld w = p.getServerWorld();
                    BlockPos base = p.getBlockPos();
                    BlockPos bestInterior = null;
                    BlockPos bestAny = null;
                    for (BlockPos pos : BlockPos.iterate(base.add(-14, -8, -14), base.add(14, 8, 14))) {
                        if (w.getBlockState(pos).isAir() && w.getBlockState(pos.up()).isAir()
                                && w.getBlockState(pos.down()).isSideSolidFullSquare(w, pos.down(), Direction.UP)) {
                            if (bestAny == null || pos.getSquaredDistance(base) < bestAny.getSquaredDistance(base)) {
                                bestAny = pos.toImmutable();
                            }
                            if (!w.isSkyVisible(pos) && (bestInterior == null
                                    || pos.getSquaredDistance(base) < bestInterior.getSquaredDistance(base))) {
                                bestInterior = pos.toImmutable();
                            }
                        }
                    }
                    BlockPos best = bestInterior != null ? bestInterior : bestAny;
                    if (best != null) {
                        p.teleport(w, best.getX() + 0.5, best.getY(), best.getZ() + 0.5, p.getYaw(), p.getPitch());
                        AL.LOGGER.info("[AL-SHOTS] floor spot {} (interior={})", best, bestInterior != null);
                    } else {
                        AL.LOGGER.error("[AL-SHOTS] no floor spot near player");
                    }
                    return 1;
                }))
            .then(net.minecraft.server.command.CommandManager.literal("frame")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    ServerWorld w = p.getServerWorld();

                    BlockPos framePos = new BlockPos(CX + 2, CY, CZ - 5);
                    w.setBlockState(new BlockPos(CX + 2, CY, CZ - 6), Blocks.SMOOTH_STONE.getDefaultState(), Block.NOTIFY_ALL);

                    for (net.minecraft.entity.decoration.ItemFrameEntity e :
                            w.getEntitiesByType(net.minecraft.entity.EntityType.ITEM_FRAME, e -> true)) {
                        e.discard();
                    }
                    net.minecraft.entity.decoration.ItemFrameEntity frame =
                            new net.minecraft.entity.decoration.ItemFrameEntity(w, framePos, Direction.SOUTH);
                    ItemStack frameCrystal = new ItemStack(ALItems.KYBER_CRYSTAL);
                    frameCrystal.set(ALComponents.CRYSTAL, new CrystalComponent(0x59B9FF));
                    frame.setHeldItemStack(frameCrystal);
                    frame.setInvulnerable(true);
                    boolean ok = w.spawnEntity(frame);
                    AL.LOGGER.info("[AL-SHOTS] item frame spawn ok={} at {}", ok, framePos);
                    return 1;
                })));
    }

    private static void buildPlatform(ServerWorld world) {
        BlockPos base = new BlockPos(CX - 6, CY - 1, CZ - 6);
        BlockState floor = Blocks.POLISHED_ANDESITE.getDefaultState();
        BlockState glass = Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState();
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                world.setBlockState(new BlockPos(CX + x, CY - 1, CZ + z), floor, Block.NOTIFY_ALL);
                for (int y = 0; y < 6; y++) {
                    world.setBlockState(new BlockPos(CX + x, CY + 1 + y, CZ + z), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                }
            }
        }

        place(world, new BlockPos(CX, CY, CZ), ALBlocks.LIGHTSABER_FORGE.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.SOUTH));
        place(world, new BlockPos(CX + 3, CY, CZ), ALBlocks.DISASSEMBLY_STATION.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.SOUTH));
        place(world, new BlockPos(CX - 3, CY, CZ), ALBlocks.SITH_SARCOPHAGUS.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.SOUTH));

        if (world.getBlockEntity(new BlockPos(CX - 3, CY, CZ)) instanceof SithSarcophagusBlockEntity platformSarcophagus) {
            platformSarcophagus.markGuardSpawned();
        }
        place(world, new BlockPos(CX + 5, CY, CZ + 4), ALBlocks.CRYSTAL_ORE.getDefaultState());
        place(world, new BlockPos(CX - 5, CY, CZ + 4), ALBlocks.CRYSTAL_ORE.getDefaultState());

        place(world, new BlockPos(CX - 5, CY, CZ - 2), ALBlocks.LIGHTSABER_FORGE_DARK.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.SOUTH));
        place(world, new BlockPos(CX + 5, CY, CZ - 2), ALBlocks.HOLOCRON_SITH.getDefaultState());
        place(world, new BlockPos(CX + 4, CY, CZ - 4), ALBlocks.HOLOCRON_JEDI.getDefaultState());
        place(world, new BlockPos(CX - 4, CY, CZ - 4), ALBlocks.SITH_STONE_COFFIN.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.SOUTH));

        if (world.getBlockEntity(new BlockPos(CX + 3, CY, CZ))
                instanceof com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity station) {
            ItemStack disSaber = new ItemStack(ALItems.LIGHTSABER);
            disSaber.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                    false, "graflex", 0x4FA8FF, false, 0));
            station.setStack(0, disSaber);
            station.setStack(1, new ItemStack(Items.REDSTONE, 32));
        }
        for (int x = -6; x <= 6; x++) {
            world.setBlockState(new BlockPos(CX + x, CY, CZ - 6), glass, Block.NOTIFY_ALL);
        }

        BlockPos framePos = new BlockPos(CX + 2, CY, CZ - 6);
        world.setBlockState(framePos, Blocks.SMOOTH_STONE.getDefaultState(), Block.NOTIFY_ALL);

        place(world, new BlockPos(CX + 4, CY, CZ + 3), ALBlocks.LIGHTSABER_STAND.getDefaultState());
        if (world.getBlockEntity(new BlockPos(CX + 4, CY, CZ + 3))
                instanceof com.drag0nge0de.lightsabers.block.blockentity.LightsaberStandBlockEntity stand) {
            ItemStack saber = new ItemStack(ALItems.LIGHTSABER);
            saber.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                    true, "knighted", CrystalComponent.DEFAULT.color(), false, 0));
            saber.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));
            stand.setDisplayStack(saber);
        }

        place(world, new BlockPos(CX + 6, CY, CZ + 2), Blocks.SMOOTH_STONE.getDefaultState());
        place(world, new BlockPos(CX + 6, CY, CZ + 3), ALBlocks.LIGHTSABER_STAND.getDefaultState()
                .with(com.drag0nge0de.lightsabers.block.LightsaberStandBlock.FACE, net.minecraft.block.enums.BlockFace.WALL)
                .with(net.minecraft.state.property.Properties.HORIZONTAL_FACING, net.minecraft.util.math.Direction.SOUTH));
        place(world, new BlockPos(CX + 8, CY + 1, CZ + 3), Blocks.SMOOTH_STONE.getDefaultState());
        place(world, new BlockPos(CX + 8, CY, CZ + 3), ALBlocks.LIGHTSABER_STAND.getDefaultState()
                .with(com.drag0nge0de.lightsabers.block.LightsaberStandBlock.FACE, net.minecraft.block.enums.BlockFace.CEILING));

        for (int x = 14; x <= 44; x++) {
            for (int z = 14; z <= 44; z++) {
                world.setBlockState(new BlockPos(x, CY - 1, z), Blocks.GRASS_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
                if ((x * 7 + z * 13) % 3 == 0) {
                    world.setBlockState(new BlockPos(x, CY, z),
                            ((x + z) % 5 == 0 ? Blocks.POPPY : Blocks.SHORT_GRASS).getDefaultState(), Block.NOTIFY_ALL);
                }
            }
        }
        var templateOpt = world.getStructureTemplateManager().getTemplate(AL.id("jedi_temple"));
        if (templateOpt.isPresent()) {
            var template = templateOpt.get();
            var placement = new net.minecraft.structure.StructurePlacementData();
            template.place(world, new BlockPos(14, CY, 14), new BlockPos(0, 0, 0), placement,
                    world.getRandom(), Block.NOTIFY_ALL);
            AL.LOGGER.info("[AL-SHOTS] jedi temple placed at (14,{},14)", CY);
        } else {
            AL.LOGGER.error("[AL-SHOTS] jedi_temple template MISSING");
        }
    }

    private static void place(ServerWorld world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state, Block.NOTIFY_ALL);
    }

    private static void equip(ServerPlayerEntity player) {
        player.getInventory().clear();
        player.getInventory().insertStack(stack(ALItems.CIRCUITRY, 1));
        player.getInventory().insertStack(stack(ALItems.KYBER_CRYSTAL, 1));
        ItemStack focusing = stack(ALItems.FOCUSING_CRYSTAL, 1);
        focusing.set(ALComponents.FOCUSING, new FocusingComponent(FocusingCrystalType.CRACKED));
        player.getInventory().insertStack(focusing);

        for (String part : new String[]{"emitter", "switch_section", "body", "pommel"}) {
            player.getInventory().insertStack(PartItem.create(part, Hilt.byName("knighted")));
        }
        ItemStack prismatic = stack(ALItems.FOCUSING_CRYSTAL, 1);
        prismatic.set(ALComponents.FOCUSING, new FocusingComponent(FocusingCrystalType.PRISMATIC));
        player.getInventory().insertStack(prismatic);
        player.getInventory().insertStack(stack(ALItems.KYBER_CRYSTAL, 1));

        ItemStack saber = new ItemStack(ALItems.LIGHTSABER);
        saber.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                true, "knighted", 0x4FA8FF, false, 0));
        saber.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));
        player.getInventory().insertStack(saber);
        player.getInventory().insertStack(new ItemStack(ALBlocks.DISASSEMBLY_STATION));

        ItemStack saber2 = new ItemStack(ALItems.LIGHTSABER);
        saber2.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                false, "graflex", 0xFF4F4F, false, 0));
        player.getInventory().insertStack(saber2);
        ItemStack dbl = new ItemStack(ALItems.DOUBLE_LIGHTSABER);
        dbl.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                false, "mauler", 0x9B59FF, true, 0));
        player.getInventory().insertStack(dbl);
        player.getInventory().insertStack(new ItemStack(ALBlocks.LIGHTSABER_FORGE_DARK));
        player.getInventory().insertStack(new ItemStack(ALBlocks.SITH_STONE_COFFIN));
        player.getInventory().insertStack(new ItemStack(Items.REDSTONE, 32));

        ItemStack porkchops = new ItemStack(Items.PORKCHOP, 64);
        player.getInventory().insertStack(porkchops);

        ItemStack pouch = new ItemStack(ALItems.CRYSTAL_POUCH);
        List<ItemStack> contents = new ArrayList<>();
        ItemStack red = new ItemStack(ALItems.KYBER_CRYSTAL);
        red.set(ALComponents.CRYSTAL, new CrystalComponent(0xFF0000));
        contents.add(red);
        ItemStack crack2 = new ItemStack(ALItems.FOCUSING_CRYSTAL);
        crack2.set(ALComponents.FOCUSING, new FocusingComponent(FocusingCrystalType.CRACKED));
        contents.add(crack2);
        for (int i = 0; i < 16; i++) {
            contents.add(ItemStack.EMPTY);
        }
        pouch.set(ALComponents.POUCH_CONTENTS, contents);
        player.getInventory().insertStack(pouch);

        player.getServer().getPlayerManager().addToOperators(player.getGameProfile());

        var inv = player.getInventory();
        for (int i = 9; i < inv.size(); i++) {
            if (inv.getStack(i).getItem() == ALItems.CRYSTAL_POUCH) {
                ItemStack displaced = inv.getStack(8);
                inv.setStack(8, inv.getStack(i));
                inv.setStack(i, displaced);
                break;
            }
        }

        player.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);

        int landY = player.getServerWorld().getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, CX, CZ + 4) + 1;
        player.teleport(player.getServerWorld(), CX + 0.5, Math.max(CY + 1, landY), CZ + 4.5, 0, 0);
        AL.LOGGER.info("[AL-SHOTS] equipped {}", player.getName().getString());
    }

    private static ItemStack stack(Item item, int count) {
        return new ItemStack(item, count);
    }
}
