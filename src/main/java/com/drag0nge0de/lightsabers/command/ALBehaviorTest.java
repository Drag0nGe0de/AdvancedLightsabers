package com.drag0nge0de.lightsabers.command;

import java.util.List;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.block.SithSarcophagusBlock;
import com.drag0nge0de.lightsabers.block.blockentity.SithSarcophagusBlockEntity;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.entity.SithGhostEntity;
import com.drag0nge0de.lightsabers.entity.ThrownLightsaberEntity;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALEntities;
import com.drag0nge0de.lightsabers.registry.ALItems;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;

public final class ALBehaviorTest {

    private static final String TAG = "[AL-BEHAVIOR]";

    private static MinecraftServer server;
    private static ServerWorld world;
    private static int phase = 0;
    private static int phaseTick = 0;
    private static int failures = 0;
    private static int passes = 0;

    private static int groundY;
    private static BlockPos sarc1;
    private static BlockPos sarc2;
    private static SithGhostEntity ghost;
    private static CowEntity dummy;
    private static boolean throwSeen;
    private static boolean reEquipSeen;
    private static boolean summonedTwice;
    private static boolean brokeOnHard;
    private static boolean heldOnNormal;
    private static boolean deposited;
    private static boolean droppedNothing;
    private static int itemsBeforeKill;

    private ALBehaviorTest() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            if (Boolean.getBoolean("al.behaviorTest")) {
                start(s);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(ALBehaviorTest::tick);
    }

    private static void start(MinecraftServer s) {
        server = s;
        world = s.getOverworld();
        groundY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, 200, 200);
        server.setDifficulty(Difficulty.HARD, false);
        world.getGameRules().get(GameRules.DO_MOB_GRIEFING).set(true, server);

        sarc1 = new BlockPos(200, groundY, 212);
        sarc2 = new BlockPos(204, groundY, 212);

        for (int cx = 11; cx <= 13; cx++) {
            for (int cz = 12; cz <= 14; cz++) {
                world.setChunkForced(cx, cz, true);
            }
        }

        Box arena = new Box(new BlockPos(200, groundY, 212)).expand(28, 16, 28);
        testArena = arena;

        for (BlockPos pos : BlockPos.iterate(186, groundY, 198, 214, groundY + 9, 228)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
        }

        for (BlockPos pos : BlockPos.iterate(186, groundY - 1, 198, 214, groundY - 1, 228)) {
            world.setBlockState(pos, Blocks.POLISHED_ANDESITE.getDefaultState(), Block.FORCE_STATE);
        }

        for (int x = 186; x <= 214; x++) {
            for (int dy = 0; dy <= 2; dy++) {
                world.setBlockState(new BlockPos(x, groundY + dy, 198), Blocks.POLISHED_ANDESITE.getDefaultState(), Block.FORCE_STATE);
                world.setBlockState(new BlockPos(x, groundY + dy, 228), Blocks.POLISHED_ANDESITE.getDefaultState(), Block.FORCE_STATE);
            }
        }

        for (int z = 198; z <= 228; z++) {
            for (int dy = 0; dy <= 2; dy++) {
                world.setBlockState(new BlockPos(186, groundY + dy, z), Blocks.POLISHED_ANDESITE.getDefaultState(), Block.FORCE_STATE);
                world.setBlockState(new BlockPos(214, groundY + dy, z), Blocks.POLISHED_ANDESITE.getDefaultState(), Block.FORCE_STATE);
            }
        }

        place(sarc1, Blocks.AIR);
        place(sarc2, Blocks.AIR);
        place(sarc1, sarcophagusState());
        place(sarc2, sarcophagusState());

        AL.LOGGER.info("{} starting: groundY={} sarc1={} sarc2={}", TAG, groundY, sarc1, sarc2);
        phase = 1;
        phaseTick = 0;
    }

    private static Box testArena;

    private static void wipeMobs() {
        for (SithGhostEntity leftover : world.getEntitiesByClass(SithGhostEntity.class, testArena, g -> true)) {
            leftover.discard();
        }

        for (CowEntity leftoverCow : world.getEntitiesByClass(CowEntity.class, testArena, c -> true)) {
            leftoverCow.discard();
        }

        for (ThrownLightsaberEntity leftoverSaber : world.getEntitiesByClass(ThrownLightsaberEntity.class, testArena, t -> true)) {
            leftoverSaber.discard();
        }

        for (ItemEntity leftoverItem : world.getEntitiesByClass(ItemEntity.class, testArena, i -> true)) {
            leftoverItem.discard();
        }
    }

    private static net.minecraft.block.BlockState sarcophagusState() {

        return ALBlocks.SITH_SARCOPHAGUS.getDefaultState()
            .with(Properties.HORIZONTAL_FACING, Direction.SOUTH);
    }

    private static void place(BlockPos pos, Block block) {
        world.setBlockState(pos, block.getDefaultState(), Block.FORCE_STATE);
    }

    private static void place(BlockPos pos, net.minecraft.block.BlockState state) {
        world.setBlockState(pos, state, Block.FORCE_STATE);
    }

    private static void pass(String what) {
        passes++;
        AL.LOGGER.info("{} PASS {}", TAG, what);
    }

    private static void fail(String what) {
        failures++;
        AL.LOGGER.error("{} FAIL {}", TAG, what);
    }

    private static SithGhostEntity findGhost() {
        List<SithGhostEntity> list = world.getEntitiesByClass(SithGhostEntity.class,
            new Box(sarc1).expand(24), g -> true);
        return list.isEmpty() ? null : list.get(0);
    }

    private static CowEntity spawnDummy(BlockPos pos) {
        CowEntity cow = EntityType.COW.create(world);
        cow.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        cow.setInvulnerable(true);
        cow.setPersistent();
        world.spawnEntity(cow);
        return cow;
    }

    private static void tick(MinecraftServer s) {
        if (phase == 0) {
            return;
        }

        phaseTick++;

        switch (phase) {
            case 1 -> {
                if (phaseTick <= 120) {
                    wipeMobs();
                }

                if (phaseTick == 120) {
                    SithGhostEntity summoned = null;

                    if (world.getBlockEntity(sarc1) instanceof SithSarcophagusBlockEntity sarc) {
                        summoned = sarc.summonGhost(null);
                        summonedTwice = sarc.summonGhost(null) == null;
                        AL.LOGGER.info("{} summon first={} secondRejected={}", TAG, summoned != null, summonedTwice);

                        if (summoned != null) {
                            summoned.setPersistent();
                        }
                    } else {
                        fail("sarcophagus BE missing");
                    }
                }

                if (phaseTick >= 200) {
                    ghost = findGhost();

                    if (ghost == null) {
                        fail("no ghost rose from the sarcophagus");
                        finish();
                        return;
                    }

                    ItemStack held = ghost.getMainHandStack();
                    LightsaberComponent lc = held.get(ALComponents.LIGHTSABER);

                    if (held.getItem() != ALItems.LIGHTSABER) {
                        fail("ghost did not spawn with a lightsaber, held=" + held.getItem());
                    } else {
                        pass("spawns holding a lightsaber");
                    }

                    if (lc == null) {
                        fail("held saber has no lightsaber component");
                    } else {
                        if (lc.color() == 0xFF0000) {
                            pass("saber colour is RED (0xFF0000)");
                        } else {
                            fail("saber colour is not RED: " + Integer.toHexString(lc.color()));
                        }

                        if (lc.active()) {
                            pass("saber auto-ignited after 5 ticks");
                        } else {
                            fail("saber not ignited");
                        }
                    }

                    if (ghost.getHealth() == 60.0F) {
                        pass("max health 60");
                    } else {
                        fail("max health is " + ghost.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH));
                    }

                    if (ghost.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).getBaseValue() == 4.0
                          && ghost.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue() == 0.6
                          && ghost.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).getBaseValue() == 1.0
                          && ghost.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).getBaseValue() == 32.0) {
                        pass("attributes: attack 4, speed 0.6, kb-resist 1, follow 32");
                    } else {
                        fail("attributes mismatch");
                    }

                    if (ghost.hasRestingPlace) {
                        pass("sarcophagus-spawned ghost remembers its resting place");
                    } else {
                        fail("hasRestingPlace not set");
                    }

                    if (summonedTwice) {
                        pass("summon is one-shot (second call rejected)");
                    } else {
                        fail("second summon not rejected");
                    }

                    dummy = spawnDummy(new BlockPos(200, groundY + 8, 218));
                    world.setBlockState(new BlockPos(200, groundY, 218), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    world.setBlockState(new BlockPos(200, groundY + 1, 218), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    world.setBlockState(new BlockPos(200, groundY + 2, 218), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    world.setBlockState(new BlockPos(200, groundY + 3, 218), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    world.setBlockState(new BlockPos(200, groundY + 4, 218), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    world.setBlockState(new BlockPos(200, groundY + 5, 218), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    world.setBlockState(new BlockPos(200, groundY + 6, 218), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    world.setBlockState(new BlockPos(200, groundY + 7, 218), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    ghost.setTarget(dummy);
                    phase = 2;
                    phaseTick = 0;
                }
            }
            case 2 -> {
                List<ThrownLightsaberEntity> thrown = world.getEntitiesByClass(ThrownLightsaberEntity.class,
                    new Box(sarc1).expand(24), t -> true);

                if (!thrown.isEmpty() && !throwSeen) {
                    throwSeen = true;
                    pass("ghost threw its saber at the target from beyond 5 blocks");
                }

                if (phaseTick == 500) {

                    dummy.discard();
                }

                if (phaseTick == 550 || phaseTick == 650 || phaseTick == 750 || phaseTick == 850) {
                    SithGhostEntity tracked = findGhost();

                    if (tracked != null) {
                        ItemStack held = tracked.getMainHandStack();
                        AL.LOGGER.info("{} t{}: ghost at {} task={} hand={} alive={}", TAG, phaseTick,
                            tracked.getBlockPos().toShortString(), tracked.taskFinished,
                            held.getItem().toString(), tracked.isAlive());
                    } else {
                        AL.LOGGER.info("{} t{}: no ghost found", TAG, phaseTick);
                    }
                }

                if (phaseTick >= 900) {
                    boolean sarc1HasSaber = false;

                    if (world.getBlockEntity(sarc1) instanceof SithSarcophagusBlockEntity sarc1be) {
                        for (int i = 0; i < sarc1be.size(); i++) {
                            if (sarc1be.getStack(i).contains(ALComponents.LIGHTSABER)) {
                                sarc1HasSaber = true;
                            }
                        }
                    }

                    boolean ghostGone = findGhost() == null;

                    if (ghostGone) {
                        AL.LOGGER.info("{} t900: no ghost found", TAG);
                    } else {
                        SithGhostEntity remaining = findGhost();
                        AL.LOGGER.info("{} t900: ghost still present at {} task={} alive={} removed={}", TAG,
                            remaining.getBlockPos().toShortString(), remaining.taskFinished,
                            remaining.isAlive(), remaining.isRemoved());
                    }

                    if (throwSeen && ghostGone && sarc1HasSaber) {
                        pass("full loop: throw -> boomerang re-equip -> rest -> saber deposited");
                    } else {
                        fail("full loop incomplete (throwSeen=" + throwSeen + " ghostGone=" + ghostGone
                            + " sarc1HasSaber=" + sarc1HasSaber + ")");
                    }

                    world.removeBlock(new BlockPos(200, groundY, 218), false);
                    world.removeBlock(new BlockPos(200, groundY + 1, 218), false);
                    world.removeBlock(new BlockPos(200, groundY + 2, 218), false);
                    world.removeBlock(new BlockPos(200, groundY + 3, 218), false);
                    world.removeBlock(new BlockPos(200, groundY + 4, 218), false);
                    world.removeBlock(new BlockPos(200, groundY + 5, 218), false);
                    world.removeBlock(new BlockPos(200, groundY + 6, 218), false);
                    world.removeBlock(new BlockPos(200, groundY + 7, 218), false);

                    for (int x = 187; x <= 213; x++) {
                        world.setBlockState(new BlockPos(x, groundY + 1, 215), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                        world.setBlockState(new BlockPos(x, groundY + 2, 215), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    }

                    dummy = spawnDummy(new BlockPos(200, groundY + 1, 219));
                    ghost = ALEntities.SITH_GHOST.create(world);
                    ghost.refreshPositionAndAngles(sarc1.getX() + 0.5, sarc1.getY() + 0.2, sarc1.getZ() + 0.5, 0.0F, 0.0F);
                    ghost.initialize(world, world.getLocalDifficulty(sarc1), net.minecraft.entity.SpawnReason.MOB_SUMMONED, null);
                    world.spawnEntity(ghost);
                    ghost.setPersistent();
                    ghost.setTarget(dummy);
                    phase = 3;
                    phaseTick = 0;
                }
            }
            case 3 -> {
                boolean anyAir = false;

                for (int x = 194; x <= 210; x++) {
                    if (world.getBlockState(new BlockPos(x, groundY + 2, 215)).isOf(Blocks.AIR)
                          || world.getBlockState(new BlockPos(x, groundY + 1, 215)).isOf(Blocks.AIR)) {
                        anyAir = true;
                        break;
                    }
                }

                if (!brokeOnHard && anyAir) {
                    brokeOnHard = true;
                    pass("ghost broke through the wall on HARD difficulty (mobGriefing)");
                }

                if (phaseTick >= 400) {
                    if (!brokeOnHard) {
                        fail("wall still intact after 400 ticks on HARD");
                    }

                    server.setDifficulty(Difficulty.NORMAL, false);
                    for (int x = 187; x <= 213; x++) {
                        world.setBlockState(new BlockPos(x, groundY + 1, 215), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                        world.setBlockState(new BlockPos(x, groundY + 2, 215), Blocks.DIRT.getDefaultState(), Block.FORCE_STATE);
                    }

                    ghost.kill();
                    dummy.discard();
                    dummy = spawnDummy(new BlockPos(200, groundY + 1, 219));
                    ghost = ALEntities.SITH_GHOST.create(world);
                    ghost.refreshPositionAndAngles(sarc1.getX() + 0.5, sarc1.getY() + 0.2, sarc1.getZ() + 0.5, 0.0F, 0.0F);
                    ghost.initialize(world, world.getLocalDifficulty(sarc1), net.minecraft.entity.SpawnReason.MOB_SUMMONED, null);
                    world.spawnEntity(ghost);
                    ghost.setPersistent();
                    ghost.setTarget(dummy);
                    heldOnNormal = false;
                    phase = 4;
                    phaseTick = 0;
                }
            }
            case 4 -> {
                boolean allIntact = true;

                for (int x = 194; x <= 210; x++) {
                    if (world.getBlockState(new BlockPos(x, groundY + 2, 215)).isOf(Blocks.AIR)
                          || world.getBlockState(new BlockPos(x, groundY + 1, 215)).isOf(Blocks.AIR)) {
                        allIntact = false;
                        break;
                    }
                }

                if (allIntact) {
                    heldOnNormal = true;
                }

                if (phaseTick >= 400) {
                    if (heldOnNormal) {
                        pass("wall survives on NORMAL (pop is HARD-gated, like the original)");
                    } else {
                        fail("wall was removed on NORMAL difficulty");
                    }

                    for (int x = 187; x <= 213; x++) {
                        world.removeBlock(new BlockPos(x, groundY + 1, 215), false);
                        world.removeBlock(new BlockPos(x, groundY + 2, 215), false);
                    }

                    ghost.kill();
                    dummy.discard();
                    if (world.getBlockEntity(sarc2) instanceof SithSarcophagusBlockEntity sarc2be) {
                        ghost = sarc2be.summonGhost(null);
                        if (ghost != null) {
                            ghost.setPersistent();
                        }
                    } else {
                        fail("sarcophagus 2 BE missing");
                    }

                    dummy = spawnDummy(new BlockPos(204, groundY + 1, 216));
                    if (ghost != null) {
                        ghost.setTarget(dummy);
                    }

                    phase = 5;
                    phaseTick = 0;
                }
            }
            case 5 -> {
                if (phaseTick >= 60 && dummy != null && !dummy.isRemoved() && ghost != null
                        && !ghost.getMainHandStack().isEmpty()) {

                    dummy.discard();

                    if (ghost.taskFinished == 1) {
                        pass("taskFinished reached 1 while hunting");
                    } else {
                        fail("taskFinished was " + ghost.taskFinished + " while hunting");
                    }
                }

                boolean sarc2HasSaber = false;

                if (world.getBlockEntity(sarc2) instanceof SithSarcophagusBlockEntity sarc2be) {
                    for (int i = 0; i < sarc2be.size(); i++) {
                        ItemStack stack = sarc2be.getStack(i);

                        if (stack.contains(ALComponents.LIGHTSABER)) {
                            sarc2HasSaber = true;
                        }
                    }
                }

                boolean ghostGone = ghost == null || !ghost.isAlive();

                if (ghostGone && sarc2HasSaber && !deposited) {
                    deposited = true;
                    pass("ghost returned to its sarcophagus, deposited the saber and dissipated");
                }

                if (phaseTick >= 800) {
                    if (!deposited) {
                        fail("no saber deposited (ghostGone=" + ghostGone + " sarc2HasSaber=" + sarc2HasSaber + ")");
                    }

                    ghost = ALEntities.SITH_GHOST.create(world);
                    ghost.refreshPositionAndAngles(sarc1.getX() + 0.5, sarc1.getY() + 0.2, sarc1.getZ() + 0.5, 0.0F, 0.0F);
                    ghost.initialize(world, world.getLocalDifficulty(sarc1), net.minecraft.entity.SpawnReason.MOB_SUMMONED, null);
                    world.spawnEntity(ghost);
                    ghost.kill();

                    itemsBeforeKill = world.getEntitiesByClass(ItemEntity.class,
                        new Box(sarc1).expand(6), d -> true).size();
                    phase = 6;
                    phaseTick = 0;
                }
            }
            case 6 -> {
                List<ItemEntity> drops = world.getEntitiesByClass(ItemEntity.class,
                    new Box(sarc1).expand(6), d -> true);

                if (phaseTick >= 60) {
                    int newDrops = drops.size() - itemsBeforeKill;

                    if (newDrops <= 0) {
                        droppedNothing = true;
                        pass("ghost drops nothing on death");
                    } else {
                        StringBuilder names = new StringBuilder();

                        for (ItemEntity drop : drops) {
                            names.append(drop.getStack().getItem()).append("x")
                                 .append(drop.getStack().getCount()).append(" ");
                        }

                        fail("ghost dropped " + newDrops + " stacks: " + names);
                    }

                    finish();
                }
            }
            default -> {
            }
        }
    }

    private static void finish() {
        if (failures == 0) {
            AL.LOGGER.info("{} ALL {} CHECKS PASSED - ghost behaviour faithful to the 1.7.10 original", TAG, passes);
        } else {
            AL.LOGGER.error("{} DONE: {} passed, {} FAILED", TAG, passes, failures);
        }

        server.stop(false);
        phase = 0;
    }
}
