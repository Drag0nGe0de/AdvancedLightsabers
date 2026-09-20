package com.drag0nge0de.lightsabers.client;

import com.drag0nge0de.lightsabers.block.LightsaberForgeBlock;
import com.drag0nge0de.lightsabers.block.blockentity.LightsaberStandBlockEntity;
import com.drag0nge0de.lightsabers.component.FocusingCrystalType;
import com.drag0nge0de.lightsabers.component.FocusingComponent;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.client.render.ItemRenderers;
import com.drag0nge0de.lightsabers.client.render.HiltRenderer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class ALShotsBot {

    private static final BlockPos TEMPLE_STAND = new BlockPos(20, 121, 20);

    private int tick = 0;
    private int stage = 0;
    private int stopAt = Integer.getInteger("al.stopat", -1);
    private int craftedSlot = -1;
    private long waitUntil = 0;
    private long stageDeadline = 0;
    private int creativeSlot = -1;
    private int stageTick = 0;
    private boolean joined = false;
    private int porkchopsBefore = -1;
    private float castEnergyBefore = -1.0F;
    private String pendingRelease = null;
    private int drainTicks = 0;
    private String assertions = new String();
    private boolean connectAttempted = false;
    public static volatile boolean iconcal = false;
    public static volatile boolean sarcotest = false;
    public static volatile int[] selectHover = null;

    public static void register() {
        if (!Boolean.getBoolean("al.shots")) {
            return;
        }
        ALShotsBot bot = new ALShotsBot();

        bot.stage = Integer.getInteger("al.scenes", 0);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            String addr = System.getProperty("al.server", "127.0.0.1:25565");
            log("client started, will connect to " + addr
                    + " once the resource overlay clears (early BlockUpdates NPE in BakedModelManager)");
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            bot.joined = true;
            bot.tick = 0;
            bot.stage = Integer.getInteger("al.scenes", 0);
            bot.stageTick = 0;
            log("joined, starting scene driver at scene " + bot.stage);
        });
        ClientTickEvents.END_CLIENT_TICK.register(bot::tick);

        java.util.Properties fileProps = new java.util.Properties();
        java.io.File propFile = new java.io.File("/tmp/al_bot.props");
        if (propFile.exists()) {
            try (java.io.FileInputStream in = new java.io.FileInputStream(propFile)) {
                fileProps.load(in);
            } catch (Exception e) {
                log("prop file unreadable: " + e);
            }
        }
        bot.iconcal = Boolean.getBoolean("al.iconcal")
                || Boolean.parseBoolean(fileProps.getProperty("al.iconcal", "false"));
        bot.sarcotest = Boolean.getBoolean("al.sarcotest")
                || Boolean.parseBoolean(fileProps.getProperty("al.sarcotest", "false"));
        log("registered; props: shots=" + Boolean.getBoolean("al.shots")
                + " scenes=" + Integer.getInteger("al.scenes")
                + " sarcotest=" + bot.sarcotest
                + " iconcal=" + bot.iconcal);
    }

    private static void log(String s) {
        System.out.println("[AL-SHOTS] " + s);
    }

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    private void assertEq(String what, boolean ok) {
        assertions += (ok ? "PASS " : "FAIL ") + what + "\n";
        log((ok ? "PASS: " : "FAIL: ") + what);
    }

    private void tick(MinecraftClient client) {
        if (!joined) {

            if (!connectAttempted && client.world == null && client.getOverlay() == null) {
                connectAttempted = true;
                String addr = System.getProperty("al.server", "127.0.0.1:25565");
                log("overlay cleared, connecting to " + addr);
                client.execute(() -> net.minecraft.client.gui.screen.multiplayer.ConnectScreen.connect(
                        null, client,
                        net.minecraft.client.network.ServerAddress.parse(addr),
                        new net.minecraft.client.network.ServerInfo("al-rig", addr,
                                net.minecraft.client.network.ServerInfo.ServerType.OTHER),
                        true, null));
            }
            return;
        }
        if (client.player == null || client.world == null) {
            return;
        }
        if (client.player.isDead()) {

            client.player.requestRespawn();
            return;
        }
        if (pendingRelease != null) {
            String which = pendingRelease;
            pendingRelease = null;
            setKey(which, false);
        }
        drainCommandQueue();
        tick++;
        stageTick++;
        if (tick % 100 == 0) {
            log("heartbeat tick=" + tick + " stage=" + stage + " stageTick=" + stageTick
                    + " pos=" + client.player.getBlockPos() + " hp=" + client.player.getHealth()
                    + " screen=" + (client.currentScreen != null ? client.currentScreen.getClass().getSimpleName() : "null"));
        }
        if (System.currentTimeMillis() < waitUntil) {
            return;
        }

        switch (stage) {
            case 0 -> {
                if (stageTick >= 60) {
                    client.player.setPitch(0.55F);
                }
                if (stageTick >= 120) {
                    shot("01_platform");
                    next();
                }
            }
            case 1 -> {
                if (stageTick >= 10) {
                    useBlock(new BlockPos(0, 120, 0));
                    next();
                }
            }
            case 2 -> {
                if (stageTick >= 25) {
                    porkchopsBefore = countPorkchops();
                    shiftClickFirst(Items.PORKCHOP);
                    next();
                }
            }
            case 3 -> {
                if (stageTick >= 45) {
                    int after = countPorkchops();
                    assertEq("forge dupe: 64 porkchops stay 64 (got " + after + ")", after == 64);
                    shot("02_forge_after_shiftclick");
                    next();
                }
            }
            case 4 -> {
                if (stageTick >= 10) {
                    forgeAssembly();
                    next();
                }
            }
            case 5 -> {
                if (stageTick >= 20) {
                    ItemStack result = client.player.currentScreenHandler.getStacks().get(8);
                    assertEq("forge result slot has assembled saber", !result.isEmpty()
                            && result.getItem() == ALItems.LIGHTSABER);
                    shot("03_forge_assembled");
                    next();
                }
            }
            case 6 -> {
                if (stageTick >= 10) {
                    takeOutput();
                    next();
                }
            }
            case 7 -> {
                if (stageTick >= 25) {
                    int sabers = countItems(ALItems.LIGHTSABER);
                    assertEq("assembled saber taken into inventory (>=2 sabers, had 1)", sabers >= 2);
                    boolean consumed = true;
                    for (int i = 0; i < 6; i++) {
                        consumed &= client.player.currentScreenHandler.getStacks().get(i).isEmpty();
                    }
                    assertEq("forge inputs consumed after take", consumed);
                    shot("04_forge_output_taken");
                    close();
                    next();
                }
            }
            case 8 -> {
                if (stageTick >= 10) {
                    useBlock(new BlockPos(3, 120, 0));
                    next();
                }
            }
            case 9 -> {
                if (stageTick >= 25) {
                    shot("05_disassembly_station");
                    close();
                    next();
                }
            }
            case 10 -> {
                if (stageTick >= 10) {
                    useBlock(new BlockPos(-3, 120, 0));
                    next();
                }
            }
            case 11 -> {
                if (stageTick >= 25) {
                    shot("06_sarcophagus_loot");
                    close();
                    next();
                }
            }
            case 12 -> {
                if (stageTick >= 10) {
                    selectItem(ALItems.CRYSTAL_POUCH);
                    next();
                }
            }
            case 13 -> {
                if (stageTick >= 5) {
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    next();
                }
            }
            case 14 -> {
                if (stageTick >= 25) {
                    pouchShiftClickTest();
                    shot("07_pouch_after_shiftclick");
                    close();
                    next();
                }
            }
            case 15 -> {
                if (stageTick >= 10) {
                    selectItem(ALItems.CRYSTAL_POUCH);
                    next();
                }
            }
            case 16 -> {
                if (stageTick >= 5) {
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    next();
                }
            }
            case 17 -> {
                if (stageTick >= 25) {
                    dropPouchFromGui();
                    next();
                }
            }
            case 18 -> {
                if (stageTick >= 20) {
                    boolean closed = client.player.currentScreenHandler == client.player.playerScreenHandler;
                    assertEq("pouch GUI closes when the pouch is dropped while open", closed);
                    shot("08_pouch_after_drop");
                    next();
                }
            }
            case 19 -> {
                if (stageTick >= 10) {
                    command("tp @s 20.5 121 24.5 180 0");
                    next();
                }
            }
            case 20 -> {
                if (stageTick >= 30) {
                    selectEmptySlot();
                    client.player.setPitch(0.35F);
                    shot("09_temple_stand_before");
                    next();
                }
            }
            case 21 -> {
                if (stageTick >= 10) {
                    useBlock(TEMPLE_STAND);
                    next();
                }
            }
            case 22 -> {
                if (stageTick >= 30) {
                    boolean empty = client.world.getBlockEntity(TEMPLE_STAND)
                            instanceof LightsaberStandBlockEntity be && be.getDisplayStack().isEmpty();
                    assertEq("temple stand display cleared after taking the saber", empty);
                    shot("10_temple_stand_after");
                    next();
                }
            }
            case 23 -> {
                if (stageTick >= 10) {
                    command("tp @s 0.5 121 4.5 0 0");
                    next();
                }
            }
            case 24 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    client.player.setPitch(0.3F);
                    next();
                }
            }
            case 25 -> {
                if (stageTick >= 15) {
                    shot("11_knighted_firstperson");
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    next();
                }
            }
            case 26 -> {
                if (stageTick >= 15) {
                    client.player.setPitch(0.15F);
                    next();
                }
            }
            case 27 -> {
                if (stageTick >= 15) {
                    shot("12_knighted_thirdperson");
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    next();
                }
            }
            case 28 -> {
                if (stageTick >= 10) {
                    if (!selectItem(ALItems.KYBER_CRYSTAL)) {
                        command("give @s lightsabers:kyber_crystal");
                    }
                    next();
                }
            }
            case 29 -> {

                if (stageTick >= 50) {
                    selectItem(ALItems.KYBER_CRYSTAL);
                    client.player.setPitch(0.25F);
                    next();
                }
            }
            case 30 -> {
                if (stageTick >= 15) {
                    shot("13_crystal_firstperson");
                    next();
                }
            }
            case 31 -> {
                if (stageTick >= 10) {
                    client.player.dropSelectedItem(false);
                    client.player.setPitch(0.55F);
                    next();
                }
            }
            case 32 -> {
                if (stageTick >= 20) {
                    shot("14_crystal_dropped");
                    next();
                }
            }
            case 33 -> {
                if (stageTick >= 10) {
                    client.setScreen(new InventoryScreen(client.player));
                    next();
                }
            }
            case 34 -> {
                if (stageTick >= 25) {
                    shot("15_inventory_icons");
                    client.setScreen(null);
                    next();
                }
            }
            case 35 -> {
                if (stageTick >= 10) {
                    next();
                }
            }
            case 36 -> {
                if (stageTick >= 10) {
                    command("tp @s 4.5 121.2 5.2 180 10");
                    next();
                }
            }
            case 37 -> {
                if (stageTick >= 20) {
                    shot("16_knighted_stand");
                    next();
                }
            }
            case 38 -> {
                if (stageTick >= 30) {
                    shot("17_knighted_stand_late");
                    next();
                }
            }
            case 39 -> {
                if (stageTick >= 30) {
                    shot("18_knighted_stand_a");
                    next();
                }
            }
            case 40 -> {
                if (stageTick >= 30) {
                    shot("19_knighted_stand_b");
                    next();
                }
            }
            case 41 -> {
                if (stageTick >= 10) {
                    command("tp @s -5.5 121 1.5 180 28");
                    next();
                }
            }
            case 42 -> {
                if (stageTick >= 30) {
                    shot("20_dark_forge_showcase");
                    next();
                }
            }
            case 43 -> {
                if (stageTick >= 10) {
                    command("altest station");
                    next();
                }
            }
            case 44 -> {
                if (stageTick >= 60) {
                    shot("21_station_test_chat");
                    next();
                }
            }
            case 45 -> {
                if (stageTick >= 10) {
                    command("tp @s 3.5 121 2.5 180 20");
                    next();
                }
            }
            case 46 -> {
                if (stageTick >= 20) {
                    useBlock(new BlockPos(3, 120, 0));
                    next();
                }
            }
            case 47 -> {
                if (stageTick >= 30) {
                    shot("22_station_loaded");
                    close();
                    next();
                }
            }
            case 48 -> {
                if (stageTick >= 10) {

                    command("weather clear");
                    command("time set noon");
                    command("tp @s 0.5 121 4.5 0 0");
                    next();
                }
            }
            case 49 -> {
                if (stageTick >= 30) {

                    if (selectItem(ALItems.KYBER_CRYSTAL)) {
                        next();
                    } else {
                        command("give @s lightsabers:kyber_crystal");
                        next();
                    }
                }
            }
            case 50 -> {
                if (stageTick >= 50) {
                    selectItem(ALItems.KYBER_CRYSTAL);
                    client.player.setPitch(0.2F);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    next();
                }
            }
            case 51 -> {
                if (stageTick >= 25) {
                    shot("23_crystal_thirdperson");
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    next();
                }
            }
            case 52 -> {
                if (stageTick == 20 || stageTick == 40 || stageTick == 60) {
                    log("FPS baseline world: " + MinecraftClient.getInstance().getCurrentFps());
                }
                if (stageTick >= 70) {
                    next();
                }
            }
            case 53 -> {
                if (stageTick >= 10) {
                    client.setScreen(new com.drag0nge0de.lightsabers.client.screen.ForcePowersScreen(net.minecraft.util.math.BlockPos.ORIGIN));
                    next();
                }
            }
            case 54 -> {
                if (stageTick == 20 || stageTick == 40 || stageTick == 60 || stageTick == 80 || stageTick == 100
                        || stageTick == 130) {
                    log("FPS force-gui: " + MinecraftClient.getInstance().getCurrentFps());
                }
                if (stageTick >= 150) {
                    shot("24_force_gui");
                    next();
                }
            }
            case 55 -> {
                if (stageTick >= 10) {
                    client.setScreen(null);
                    command("tp @s 5.5 121 -0.5 180 12");
                    next();
                }
            }
            case 56 -> {
                if (stageTick >= 30) {
                    useBlock(new BlockPos(5, 120, -2));
                    next();
                }
            }
            case 57 -> {
                if (stageTick >= 30) {
                    assertEq("holocron use opens the Force Powers screen",
                            client.currentScreen instanceof com.drag0nge0de.lightsabers.client.screen.ForcePowersScreen);
                    shot("25_holocron_gui");
                    client.setScreen(null);
                    next();
                }
            }
            case 58 -> {

                if (stageTick >= 10) {
                    ForceClientState.update(25, 80.0F, 160.0F, 4.0F, java.util.Set.of("forceSensitivity"), java.util.Map.of());
                    command("xp add @s 3 levels");
                    command("altest frame");
                    command("tp @s 2.5 120 -2.5 180 10");
                    next();
                }
            }
            case 59 -> {
                if (stageTick >= 30) {
                    int frames = 0;
                    for (var e : client.world.getEntities()) {
                        if (e instanceof net.minecraft.entity.decoration.ItemFrameEntity) {
                            frames++;
                            log("item frame at " + e.getBlockPos() + " facing "
                                    + ((net.minecraft.entity.decoration.ItemFrameEntity) e).getHorizontalFacing());
                        }
                    }
                    log("item frames visible client-side: " + frames);
                    shot("26_item_frame_crystal");
                    next();
                }
            }
            case 60 -> {
                if (stageTick >= 10) {
                    client.setScreen(new InventoryScreen(client.player));
                    next();
                }
            }
            case 61 -> {
                if (stageTick >= 30) {
                    shot("27_inventory_crystal");
                    client.setScreen(null);
                    next();
                }
            }
            case 62 -> {
                if (stageTick >= 10) {
                    useBlock(new BlockPos(0, 120, 0));
                    next();
                }
            }
            case 63 -> {
                if (stageTick == 60 || stageTick == 100 || stageTick == 140) {
                    log("FPS forge-gui: " + MinecraftClient.getInstance().getCurrentFps());
                }
                if (stageTick >= 160) {
                    shot("28_forge_gui");
                    close();
                    next();
                }
            }
            case 64 -> {

                if (stageTick >= 10) {
                    command("altest stand graflex");
                    next();
                }
            }
            case 65 -> {
                if (stageTick >= 20) {
                    command("tp @s 4.5 121.3 5.6 180 8");
                    next();
                }
            }
            case 66 -> {
                if (stageTick >= 30) {
                    shot("29_blade_graflex");
                    next();
                }
            }
            case 67 -> {
                if (stageTick >= 10) {
                    command("altest stand redeemer");
                    next();
                }
            }
            case 68 -> {
                if (stageTick >= 20) {
                    command("tp @s 4.5 121.3 5.6 180 8");
                    next();
                }
            }
            case 69 -> {
                if (stageTick >= 30) {
                    shot("30_blade_redeemer");
                    next();
                }
            }
            case 70 -> {
                if (stageTick >= 10) {
                    command("altest stand prodigal_son");
                    next();
                }
            }
            case 71 -> {
                if (stageTick >= 20) {
                    command("tp @s 4.5 121.3 5.6 180 8");
                    next();
                }
            }
            case 72 -> {
                if (stageTick >= 30) {
                    shot("31_blade_prodigal_son");
                    next();
                }
            }
            case 73 -> {
                if (stageTick >= 10) {
                    command("altest stand juggernaut");
                    next();
                }
            }
            case 74 -> {
                if (stageTick >= 20) {
                    command("tp @s 4.5 121.3 5.6 180 8");
                    next();
                }
            }
            case 75 -> {
                if (stageTick >= 30) {
                    shot("32_blade_juggernaut");
                    next();
                }
            }
            case 76 -> {
                if (stageTick >= 10) {
                    command("altest stand mauler");
                    next();
                }
            }
            case 77 -> {
                if (stageTick >= 20) {
                    command("tp @s 4.5 121.3 5.6 180 8");
                    next();
                }
            }
            case 78 -> {
                if (stageTick >= 30) {
                    shot("33_blade_mauler");
                    next();
                }
            }
            case 79 -> {
                if (stageTick >= 10) {
                    command("altest stand knighted");
                    next();
                }
            }
            case 80 -> {
                if (stageTick >= 20) {
                    command("tp @s 4.5 121.3 5.6 180 8");
                    next();
                }
            }
            case 81 -> {
                if (stageTick >= 30) {
                    shot("34_blade_knighted");
                    next();
                }
            }
            case 82 -> {
                if (stageTick >= 10) {

                    command("gamemode spectator");
                    command("tp @s 5.5 120.9 6.4 190 25");
                    next();
                }
            }
            case 83 -> {
                if (stageTick >= 30) {
                    shot("35_wild_ore");
                    next();
                }
            }
            case 84 -> {

                if (stageTick >= 10) {
                    command("gamemode spectator");
                    command("altest temple");
                    next();
                }
            }
            case 85 -> {
                if (stageTick >= 240) {
                    command("altest floor");
                    next();
                }
            }
            case 86 -> {
                if (stageTick >= 100) {
                    command("altest ore");
                    next();
                }
            }
            case 87 -> {
                if (stageTick >= 80) {
                    shot("36_temple_display");
                    next();
                }
            }
            case 88 -> {
                if (stageTick >= 10) {
                    command("tp @s ~ 8 ~-16 180 18");
                    next();
                }
            }
            case 89 -> {
                if (stageTick >= 160) {
                    shot("37_temple_wide");
                    next();
                }
            }
            case 90 -> {
                if (stageTick >= 10) {
                    command("altest floor");
                    command("tp @s ~ ~ ~ 135 12");
                    next();
                }
            }
            case 91 -> {
                if (stageTick >= 100) {
                    shot("39_temple_interior");
                    next();
                }
            }
            case 92 -> {
                if (stageTick >= 10) {
                    command("tp @s ~ 18 ~ 0 78");
                    next();
                }
            }
            case 93 -> {
                if (stageTick >= 160) {
                    shot("38_temple_overhead");
                    next();
                }
            }
            case 94 -> {
                if (stageTick >= 10) {
                    command("gamemode spectator");
                    command("kill @e[type=lightsabers:sith_ghost]");

                    command("summon lightsabers:sith_ghost -4.5 120.0 -8.5 {NoAI:1b,Rotation:[0f,0f]}");

                    command("summon lightsabers:sith_ghost -2.0 120.0 -9.0");
                    command("tp @s -4.5 119.4 -5.0 180 8");
                    next();
                }
            }
            case 95 -> {
                if (stageTick >= 50) {
                    shot("40_ghost_front");
                    next();
                }
            }
            case 96 -> {
                if (stageTick >= 40) {
                    shot("41_ghost_live");
                    next();
                }
            }
            case 97 -> {

                if (stageTick >= 10) {
                    command("gamemode spectator");
                    command("kill @e[type=lightsabers:sith_ghost]");
                    command("kill @e[type=lightsabers:thrown_lightsaber]");
                    command("gamerule doMobGriefing true");
                    command("difficulty hard");

                    command("fill -6 119 8 6 119 20 polished_andesite");
                    command("fill -6 120 8 6 121 20 air");
                    command("setblock 0 120 12 lightsabers:sith_sarcophagus[facing=south]");
                    command("tp @s 0.5 120.0 20.5 180 5");
                    command("gamemode survival");
                    next();
                }
            }
            case 98 -> {
                if (stageTick >= 70) {
                    shot("42_ghost_rise");
                    command("altest ghostcheck rise");
                    next();
                }
            }
            case 99 -> {
                if (stageTick >= 20) {
                    respawnIfNeeded();
                    command("kill @s");
                    next();
                }
            }
            case 100 -> {

                if (stageTick == 10) {
                    respawnIfNeeded();
                    command("gamemode spectator");
                    command("tp @s 4.5 121.0 15.5 0 15");
                }
                if (stageTick == 80) {
                    shot("43_ghost_return");
                }
                if (stageTick >= 190) {
                    shot("44_ghost_rest_smoke");
                    command("altest ghostcheck deposit");
                    next();
                }
            }
            case 101 -> {
                if (stageTick >= 10) {
                    respawnIfNeeded();
                    command("gamemode survival");
                    command("kill @e[type=lightsabers:sith_ghost]");
                    command("summon lightsabers:sith_ghost 0.5 120.0 12.5 0 0");
                    command("fill -6 120 15 6 121 15 dirt");
                    command("tp @s 0.5 120.0 19.5 180 10");
                    next();
                }
            }
            case 102 -> {
                if (stageTick == 45) {
                    shot("45_ghost_breaking");
                }
                if (stageTick >= 190) {
                    shot("45_ghost_broke_through");
                    command("altest ghostcheck break");
                    next();
                }
            }
            case 103 -> {

                if (stageTick >= 10) {
                    respawnIfNeeded();
                    command("kill @e[type=lightsabers:sith_ghost]");
                    command("kill @e[type=lightsabers:thrown_lightsaber]");
                    command("fill -6 120 15 6 121 15 air");
                    command("fill 0 120 18 0 125 18 dirt");
                    command("tp @s 0.5 126.0 18.5 180 25");
                    command("summon lightsabers:sith_ghost 0.5 120.0 12.5 180 0");
                    next();
                }
            }
            case 104 -> {
                if (stageTick == 50 || stageTick == 80 || stageTick == 110) {
                    shot(stageTick == 50 ? "46_ghost_throw_a" : stageTick == 80 ? "46_ghost_throw_b" : "46_ghost_throw_c");
                    command("altest ghostcheck throw" + stageTick);
                }
                if (stageTick >= 160) {
                    next();
                }
            }
            case 105 -> {
                if (stageTick >= 10) {
                    respawnIfNeeded();
                    command("kill @e[type=lightsabers:thrown_lightsaber]");
                    command("kill @e[type=lightsabers:sith_ghost]");
                    command("fill 0 120 18 0 125 18 air");
                    command("gamemode spectator");
                    command("difficulty easy");
                    command("summon lightsabers:sith_ghost -4.5 120.0 -8.5 {NoAI:1b,Rotation:[0f,0f]}");
                    command("tp @s -4.5 119.4 -5.0 180 8");
                    next();
                }
            }
            case 106 -> {
                if (stageTick >= 50) {
                    shot("47_ghost_final");
                    command("kill @e[type=lightsabers:sith_ghost]");
                    command("gamemode survival");
                    log("scenes complete");
                    next();
                    stage = 107;
                    stageTick = 0;
                }
            }
            case 107 -> {
                if (stageTick >= 10) {
                    command("force xp set 50000");
                    command("force power unlock all");
                    command("force energy set 200");
                    command("force xp query");
                    next();
                }
            }
            case 110 -> {
                if (stageTick >= 60) {
                    command("force cast speed");
                    next();
                }
            }
            case 111 -> {
                if (stageTick >= 40) {
                    assertEq("speed cast applied", mc().player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.SPEED));
                    command("force cast meditation1");
                    command("force cast heal1");
                    shot("53_force_hud");
                    next();
                }
            }
            case 112 -> {
                if (stageTick >= 40) {
                    assertEq("meditation absorption applied", mc().player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.ABSORPTION));
                    command("gamemode survival");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber");
                    next();
                }
            }
            case 113 -> {
                if (stageTick == 20) {
                    command("loot give @s loot lightsabers:chests/sith_tomb");
                    command("loot give @s loot lightsabers:chests/jedi_temple");
                    command("loot give @s loot lightsabers:chests/sith_coffin");
                    next();
                }
            }
            case 114 -> {
                if (stageTick >= 40) {
                    int crystals = 0;
                    for (int i = 0; i < mc().player.getInventory().main.size(); i++) {
                        ItemStack st = mc().player.getInventory().main.get(i);
                        if (st.isOf(ALItems.KYBER_CRYSTAL)) {
                            crystals++;
                        }
                    }
                    assertEq("structure loot tables roll (crystals received)", crystals > 0);
                    shot("54_loot_roll");
                    log("scenes complete");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    next();
                    mc().scheduleStop();
                }
            }
            case 115 -> {
                if (stageTick >= 10) {
                    command("difficulty peaceful");
                    command("kill @e[type=lightsabers:sith_ghost]");
                    command("gamerule keepInventory true");
                    command("altest scene2");
                    next();
                }
            }
            case 116 -> {
                if (stageTick >= 30) {
                    useBlock(new BlockPos(0, 120, 0));
                    next();
                }
            }
            case 117 -> {
                if (stageTick >= 25) {
                    boolean ok = forgeAssembly();

                    if (ok || System.currentTimeMillis() > stageDeadline) {
                        if (!ok) {
                            log("FAIL: forge assembly incomplete");
                        }
                        next();
                    }
                }
            }
            case 118 -> {
                if (stageTick >= 20) {
                    boolean ready = !mc().player.currentScreenHandler.getStacks().get(8).isEmpty();

                    if (ready || System.currentTimeMillis() > stageDeadline) {
                        if (!ready) {
                            log("FAIL: forge output never appeared");
                        } else {
                            takeOutput();
                        }
                        next();
                    }
                }
            }
            case 119 -> {
                if (stageTick >= 25) {
                    int craftedAt = -1;
                    int creativeAt = -1;

                    for (int i = 0; i < client.player.getInventory().size(); i++) {
                        ItemStack s = client.player.getInventory().getStack(i);
                        if (s.getItem() == ALItems.LIGHTSABER) {
                            if (s.contains(DataComponentTypes.CUSTOM_MODEL_DATA)) {
                                craftedAt = i;
                            } else {
                                creativeAt = i;
                            }
                        }
                    }

                    if ((craftedAt >= 0 && creativeAt >= 0) || System.currentTimeMillis() > stageDeadline) {
                        close();
                        log("crafted saber at inv " + craftedAt + ", creative saber at inv " + creativeAt);
                        craftedSlot = craftedAt;
                        creativeSlot = creativeAt;
                        next();
                    }
                }
            }
            case 120 -> {
                if (stageTick >= 15) {
                    client.player.getInventory().selectedSlot = craftedSlot;
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    next();
                }
            }
            case 121 -> {
                if (stageTick >= 20) {
                    ItemStack held = client.player.getMainHandStack();
                    LightsaberComponent lc = held.get(ALComponents.LIGHTSABER);
                    log("CRAFTED saber color=" + (lc != null ? Integer.toHexString(lc.color()) : "?")
                            + " focusing=" + (lc != null ? lc.focusing() : -1)
                            + " active=" + (lc != null && lc.active()));
                    client.player.setPitch(0.35F);
                    shot("50_crafted_firstperson");
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    next();
                }
            }
            case 122 -> {
                if (stageTick >= 20) {
                    client.player.setPitch(0.3F);
                    shot("51_crafted_thirdperson");
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    next();
                }
            }
            case 123 -> {
                if (stageTick >= 15) {
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    client.player.getInventory().selectedSlot = creativeSlot;
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    next();
                }
            }
            case 124 -> {
                if (stageTick >= 20) {
                    ItemStack held = client.player.getMainHandStack();
                    LightsaberComponent lc = held.get(ALComponents.LIGHTSABER);
                    log("CREATIVE saber color=" + (lc != null ? Integer.toHexString(lc.color()) : "?")
                            + " focusing=" + (lc != null ? lc.focusing() : -1)
                            + " active=" + (lc != null && lc.active()));
                    client.player.setPitch(0.35F);
                    shot("52_creative_firstperson");
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    next();
                }
            }
            case 125 -> {
                if (stageTick >= 20) {
                    client.player.setPitch(0.3F);
                    shot("53_creative_thirdperson");
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    next();
                }
            }
            case 126 -> {
                if (stageTick >= 10) {
                    command("fill 0 119 -4 6 120 -4 air");
                    command("tp @s 2.5 120.0 -1.5 0 0");
                    next();
                }
            }
            case 127 -> {
                if (stageTick >= 30) {
                    if (selectItem(ALBlocks.LIGHTSABER_FORGE_DARK.asItem())) {
                        client.player.setPitch(0.6F);
                        useBlock(new BlockPos(2, 119, -3));
                        next();
                    } else if (System.currentTimeMillis() > stageDeadline) {
                        for (int i = 0; i < 9; i++) {
                            log("hotbar " + i + " = " + client.player.getInventory().getStack(i));
                        }
                        log("FAIL: no dark forge item in hotbar");
                        next();
                    }
                }
            }
            case 128 -> {
                if (stageTick >= 40) {
                    BlockState mainState = client.world.getBlockState(new BlockPos(2, 120, -3));
                    boolean mainOk = mainState.isOf(ALBlocks.LIGHTSABER_FORGE_DARK)
                            && !LightsaberForgeBlock.isPanel(mainState);
                    boolean panelOk = false;
                    BlockPos panelCheck = new BlockPos(2, 120, -3);

                    if (mainOk) {
                        panelCheck = new BlockPos(2, 120, -3)
                                .offset(LightsaberForgeBlock.panelDir(mainState));
                        BlockState panelState = client.world.getBlockState(panelCheck);
                        panelOk = panelState.isOf(ALBlocks.LIGHTSABER_FORGE_DARK)
                                && LightsaberForgeBlock.isPanel(panelState);
                    }
                    log("placed forge facing=" + (mainOk ? mainState.get(LightsaberForgeBlock.FACING).toString() : "none")
                            + " panel checked at " + panelCheck);
                    assertEq("placing the forge item creates the main block", mainOk);
                    assertEq("placing the forge item creates the panel block toward the display side", panelOk);
                    shot("55_forge_pair");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    next();
                    mc().scheduleStop();
                }
            }

            case 200 -> {
                if (stageTick >= 15) {
                    command("altest clientunlock");
                    next();
                }
            }
            case 201 -> {
                if (stageTick >= 40) {
                    assertEq("sync: force sensitivity unlocked", com.drag0nge0de.lightsabers.client.ForceClientState.hasSensitivity());
                    assertEq("sync: tree leaves unlocked (push3)", com.drag0nge0de.lightsabers.client.ForceClientState.hasPower("push3"));
                    assertEq("sync: tree leaves unlocked (throw2)", com.drag0nge0de.lightsabers.client.ForceClientState.hasPower("throw2"));
                    log("energy=" + com.drag0nge0de.lightsabers.client.ForceClientState.energy
                            + "/" + com.drag0nge0de.lightsabers.client.ForceClientState.maxEnergy
                            + " xp=" + com.drag0nge0de.lightsabers.client.ForceClientState.xp);
                    assertEq("sync: max energy above sensitivity base", com.drag0nge0de.lightsabers.client.ForceClientState.maxEnergy > 50.0F);
                    next();
                }
            }
            case 202 -> {
                if (stageTick >= 10) {
                    ItemStack k = com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(
                            com.drag0nge0de.lightsabers.component.CrystalColor.INDIGO);
                    java.util.List<Text> tt = k.getTooltip(net.minecraft.item.Item.TooltipContext.DEFAULT, null,
                            net.minecraft.item.tooltip.TooltipType.BASIC);
                    String lore = tt.size() > 1 ? tt.get(tt.size() - 1).getString() : "";
                    String loreColor = tt.size() > 1 && tt.get(tt.size() - 1).getStyle().getColor() != null
                            ? tt.get(tt.size() - 1).getStyle().getColor().getName() : "none";
                    String nameColor = !tt.isEmpty() && tt.get(0).getStyle().getColor() != null
                            ? tt.get(0).getStyle().getColor().getName() : "none";
                    boolean nameWhite = nameColor.equals("none") || nameColor.equals("white");
                    assertEq("kyber name plain white", nameWhite);
                    assertEq("kyber lore is 'Indigo (Rare)'", "Indigo (Rare)".equals(lore));
                    assertEq("kyber lore drawn gray", "gray".equals(loreColor));
                    assertEq("kyber rarity component stays common (white name)",
                            k.getOrDefault(DataComponentTypes.RARITY, net.minecraft.util.Rarity.COMMON) == net.minecraft.util.Rarity.COMMON);
                    next();
                }
            }
            case 203 -> {
                if (stageTick >= 10) {
                    assertDesc("stun1", "stun1", "Stun 2s -> Target");
                    assertDesc("speed", "speed", "Speed x2 5s -> Caster");
                    assertDesc("resist1", "resist1", "Lightsaber DMG / 0.25 7s -> Caster");
                    assertDesc("lightning1", "lightning1", "4 DMG/s -> Target");
                    assertDesc("wound1[0]", "wound1", "2 DMG/s -> Target");
                    assertDesc("wound1[1]", "wound1", "Stun 1.5s -> Target", 1);
                    assertDesc("meditation1[0]", "meditation1", "Attack DMG x1.25 90s -> Caster");
                    assertDesc("meditation1[1]", "meditation1", "+4 Absorption to Caster", 1);
                    assertDesc("throw2", "throw2", "Impact Radius x2");
                    next();
                }
            }
            case 204 -> {
                if (stageTick >= 10) {
                    try {
                        com.drag0nge0de.lightsabers.client.screen.ForceSelectScreen screen =
                                new com.drag0nge0de.lightsabers.client.screen.ForceSelectScreen();
                        screen.init(mc(), mc().getWindow().getScaledWidth(), mc().getWindow().getScaledHeight());
                        java.lang.reflect.Field slotsF = com.drag0nge0de.lightsabers.client.screen.ForceSelectScreen.class
                                .getDeclaredField("slots");
                        slotsF.setAccessible(true);
                        java.util.List<?> listed = (java.util.List<?>) slotsF.get(screen);
                        java.util.Set<String> names = new java.util.HashSet<>();
                        for (Object o : listed) {
                            names.add(((com.drag0nge0de.lightsabers.force.Power) o).getName());
                        }
                        log("select screen lists " + names.size() + ": " + names);
                        assertEq("select screen shows exactly the 13 top-tier castable powers", names.size() == 13);
                        assertEq("select screen has push3", names.contains("push3"));
                        assertEq("select screen has sight3", names.contains("sight3"));
                        assertEq("select screen has stealth", names.contains("stealth"));
                        assertEq("select screen has throw2", names.contains("throw2"));
                        assertEq("select screen has fortify3", names.contains("fortify3"));
                        assertEq("select screen hides lower tiers (heal1)", !names.contains("heal1"));
                        assertEq("select screen hides lower tiers (heal2)", !names.contains("heal2"));
                        assertEq("select screen hides lower tiers (push1)", !names.contains("push1"));
                    } catch (Exception e) {
                        assertEq("select screen reflection failed: " + e, false);
                    }
                    next();
                }
            }
            case 205 -> {
                if (stageTick >= 10) {
                    com.drag0nge0de.lightsabers.client.ForceClientState.setSlotPower(0, "speed");
                    com.drag0nge0de.lightsabers.client.ForceClientState.setSlotPower(1, "fortify1");
                    com.drag0nge0de.lightsabers.client.ForceClientState.setSlotPower(2, "");
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                            new com.drag0nge0de.lightsabers.network.ALNetwork.AssignSlotsPayload(
                            java.util.List.of("speed", "fortify1", "")));
                    log("assigned speed+fortify1 to slots");
                    next();
                }
            }
            case 206 -> {
                if (stageTick >= 25) {
                    assertEq("server echoed slot 0 = speed",
                            com.drag0nge0de.lightsabers.client.ForceClientState.getSlotPower(0) != null
                                    && "speed".equals(com.drag0nge0de.lightsabers.client.ForceClientState.getSlotPower(0).getName()));
                    assertEq("server echoed slot 1 = fortify1",
                            com.drag0nge0de.lightsabers.client.ForceClientState.getSlotPower(1) != null
                                    && "fortify1".equals(com.drag0nge0de.lightsabers.client.ForceClientState.getSlotPower(1).getName()));
                    next();
                }
            }
            case 207 -> {
                if (stageTick >= 10) {
                    pressOnce("selectKey");
                    next();
                }
            }
            case 208 -> {
                if (stageTick >= 20) {
                    assertEq("tap F cycles to slot 1", com.drag0nge0de.lightsabers.client.ForceClientState.selectedSlot == 1);
                    pressOnce("selectKey");
                    next();
                }
            }
            case 209 -> {
                if (stageTick >= 20) {
                    assertEq("tap F cycles to slot 2", com.drag0nge0de.lightsabers.client.ForceClientState.selectedSlot == 2);
                    pressOnce("selectKey");
                    next();
                }
            }
            case 210 -> {
                if (stageTick >= 20) {
                    assertEq("tap F wraps back to slot 0", com.drag0nge0de.lightsabers.client.ForceClientState.selectedSlot == 0);
                    float before = com.drag0nge0de.lightsabers.client.ForceClientState.energy;
                    castEnergyBefore = before;
                    pressOnce("castKey");
                    log("pressed C with speed selected (PER_USE, cost 30)");
                    next();
                }
            }
            case 211 -> {
                if (stageTick >= 20) {
                    float after = com.drag0nge0de.lightsabers.client.ForceClientState.energy;
                    log("energy after cast=" + after + " (regen refills 30 in ~6 ticks with the full tree)");
                    assertEq("C cast speed (speed effect visible client-side)",
                            com.drag0nge0de.lightsabers.client.ForceClientState.hasEffect(com.drag0nge0de.lightsabers.force.ForceEffects.SPEED));
                    pressOnce("selectKey");
                    next();
                }
            }
            case 212 -> {
                if (stageTick >= 20) {
                    assertEq("selected slot 1 (fortify1)", com.drag0nge0de.lightsabers.client.ForceClientState.selectedSlot == 1);
                    setKey("castKey", true);
                    log("holding C to channel fortify1");
                    next();
                }
            }
            case 213 -> {
                if (stageTick >= 45) {
                    assertEq("channel active while C held (fortify effect)",
                            com.drag0nge0de.lightsabers.client.ForceClientState.hasEffect(com.drag0nge0de.lightsabers.force.ForceEffects.FORTIFY));
                    next();
                }
            }
            case 214 -> {
                if (stageTick >= 30) {
                    setKey("castKey", false);
                    log("released C");
                    next();
                }
            }
            case 215 -> {
                if (stageTick >= 30) {
                    assertEq("fortify effect cleared after release",
                            !com.drag0nge0de.lightsabers.client.ForceClientState.hasEffect(com.drag0nge0de.lightsabers.force.ForceEffects.FORTIFY));
                    setKey("selectKey", true);
                    next();
                }
            }
            case 216 -> {
                if (stageTick >= 15) {
                    assertEq("holding F opens the power select screen",
                            mc().currentScreen instanceof com.drag0nge0de.lightsabers.client.screen.ForceSelectScreen);
                    setKey("selectKey", false);
                    shot("61_force_select_screen");
                    next();
                }
            }
            case 217 -> {
                if (stageTick >= 15) {
                    mc().setScreen(null);
                    mc().setScreen(new com.drag0nge0de.lightsabers.client.screen.ForcePowersScreen(net.minecraft.util.math.BlockPos.ORIGIN));
                    next();
                }
            }
            case 218 -> {
                if (stageTick >= 20) {
                    shot("60_force_powers_screen");
                    mc().setScreen(null);
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    next();
                    mc().scheduleStop();
                }
            }

            case 300 -> {
                if (stageTick == 10) {
                    respawnIfNeeded();
                    command("fill -8 119 -12 24 119 30 polished_andesite");
                    command("fill -8 120 -12 24 123 30 air");
                    next();
                }
            }
            case 301 -> {
                if (stageTick >= 20) {
                    command("gamemode survival");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber[lightsabers:lightsaber={active:false,emitter_hilt:\"graflex\",switch_hilt:\"graflex\",hilt:\"graflex\",pommel_hilt:\"graflex\",color:65280,double:false,focusing:4}] 1");
                    command("give @s lightsabers:double_lightsaber[lightsabers:lightsaber={active:false,emitter_hilt:\"knighted\",switch_hilt:\"knighted\",hilt:\"knighted\",pommel_hilt:\"knighted\",color:255,double:true,focusing:4}] 1");
                    command("give @s lightsabers:holocron_jedi 1");
                    command("give @s lightsabers:holocron_sith 1");
                    command("give @s lightsabers:kyber_crystal 1");
                    command("give @s lightsabers:crystal_pouch[lightsabers:crystal={color:16711680}] 1");
                    command("give @s lightsabers:crystal_pouch[lightsabers:crystal={color:16776960}] 1");
                    command("give @s lightsabers:crystal_pouch 1");
                    command("give @s lightsabers:lightsaber_stand 1");
                    command("give @s lightsabers:holocron_jedi 1");
                    command("give @s lightsabers:sith_stone_coffin 1");
                    command("give @s lightsabers:sith_sarcophagus 1");
                    command("give @s lightsabers:sith_sarcophagus 1");
                    command("setblock 8 120 2 lightsabers:lightsaber_stand");
                    command("setblock 0 120 4 lightsabers:holocron_jedi");
                    command("setblock 2 120 4 lightsabers:holocron_sith");
                    next();
                }
            }
            case 302 -> {
                if (stageTick >= 20) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    next();
                }
            }
            case 303 -> {
                if (stageTick >= 30) {
                    shot("70_icon_inventory");
                    mc().setScreen(null);
                    next();
                }
            }
            case 304 -> {
                if (stageTick >= 10) {

                    selectItem(ALItems.LIGHTSABER);
                    next();
                }
            }
            case 305 -> {
                if (stageTick >= 20) {
                    command("tp @s 8.5 120.0 2.5 90 30");
                    useBlock(new BlockPos(8, 120, 2));
                    next();
                }
            }
            case 306 -> {
                if (stageTick >= 30) {
                    command("gamemode spectator");
                    command("tp @s 10.5 121.2 2.5 250 15");
                    next();
                }
            }
            case 307 -> {
                if (stageTick >= 40) {
                    shot("71_stand_placed");
                    command("tp @s 8.5 121.0 6.5 180 25");
                    next();
                }
            }
            case 308 -> {
                if (stageTick >= 30) {
                    shot("71b_stand_side");
                    next();
                }
            }
            case 309 -> {
                if (stageTick >= 10) {
                    command("tp @s 0.5 121.5 9.5 180 35");
                    next();
                }
            }
            case 310 -> {
                if (stageTick >= 40) {
                    shot("72_holocrons_closed");

                    for (var be : new Object[]{mc().world.getBlockEntity(new BlockPos(0, 120, 4)),
                            mc().world.getBlockEntity(new BlockPos(2, 120, 4))}) {
                        if (be instanceof com.drag0nge0de.lightsabers.block.blockentity.HolocronBlockEntity h) {
                            h.clientAddUser(1);
                            log("holocron " + h.getPos() + " sith=" + h.isSith());
                        } else {
                            assertEq("holocron BE exists", false);
                        }
                    }
                    next();
                }
            }
            case 311 -> {
                if (stageTick == 45 || stageTick == 70) {
                    shot(stageTick == 45 ? "73_holocrons_mid_open" : "74_holocrons_open");
                }
                if (stageTick >= 90) {
                    for (var be : new Object[]{mc().world.getBlockEntity(new BlockPos(0, 120, 4)),
                            mc().world.getBlockEntity(new BlockPos(2, 120, 4))}) {
                        if (be instanceof com.drag0nge0de.lightsabers.block.blockentity.HolocronBlockEntity h) {
                            h.clientAddUser(-1);
                        }
                    }
                    next();
                }
            }
            case 312 -> {
                if (stageTick >= 80) {
                    shot("75_holocrons_reclosed");
                    next();
                }
            }
            case 313 -> {
                if (stageTick >= 10) {
                    command("gamemode survival");
                    command("tp @s 0.5 120.0 8.5 0 -8");
                    next();
                }
            }
            case 314 -> {
                if (stageTick >= 20) {

                    var inv = mc().player.getInventory();
                    for (int i = 0; i < inv.main.size(); i++) {
                        var s = inv.main.get(i);
                        if (s.getItem() == ALItems.DOUBLE_LIGHTSABER) {
                            inv.selectedSlot = i;
                            break;
                        }
                    }
                    next();
                }
            }
            case 315 -> {
                if (stageTick >= 30) {
                    shot("76_fp_double");
                    var inv = mc().player.getInventory();
                    for (int i = 0; i < inv.main.size(); i++) {
                        var s = inv.main.get(i);
                        if (s.getItem() == ALItems.LIGHTSABER) {
                            inv.selectedSlot = i;
                            break;
                        }
                    }
                    next();
                }
            }
            case 316 -> {
                if (stageTick >= 30) {
                    shot("77_fp_single");

                    var inv = mc().player.getInventory();
                    for (int i = 0; i < inv.main.size(); i++) {
                        var s = inv.main.get(i);
                        if (s.getItem() == ALItems.LIGHTSABER) {
                            inv.selectedSlot = 5;
                            break;
                        }
                    }
                    mc().options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
                    command("tp @s 0.5 120.0 10.5 180 5");
                    next();
                }
            }
            case 317 -> {
                if (stageTick >= 40) {
                    shot("78_hip_saber");
                    mc().options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);

                    ForceClientState.update(25, 120.0F, 160.0F, 4.0F, java.util.Set.of("forceSensitivity"), java.util.Map.of());
                    next();
                }
            }
            case 318 -> {
                if (stageTick >= 20) {
                    shot("79_hud_energy");

                    command("force power reset");
                    next();
                }
            }
            case 319 -> {
                if (stageTick >= 15) {
                    command("force power unlock @s forceSensitivity");
                    command("force xp set @s 500");
                    next();
                }
            }
            case 320 -> {
                if (stageTick >= 15) {
                    ClientPlayNetworking.send(new com.drag0nge0de.lightsabers.network.ALNetwork.DrainXpPayload("speed1"));
                    drainTicks++;
                    if (drainTicks < 60) {
                        stageTick = 14;
                    } else {
                        next();
                    }
                }
            }
            case 321 -> {
                if (stageTick >= 30) {
                    assertEq("hold-to-drain unlocked speed1", ForceClientState.hasPower("speed1"));
                    log("client xp after drain: " + ForceClientState.xp);
                    shot("80_force_gui_drained");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    next();
                    mc().scheduleStop();
                }
            }
            case 400 -> {

                if (stageTick == 10) {
                    respawnIfNeeded();
                    command("clear @s");
                    command("gamemode survival");
                    command("item replace entity @s hotbar.0 with lightsabers:holocron_jedi");
                    command("item replace entity @s hotbar.1 with lightsabers:holocron_sith");
                    command("item replace entity @s hotbar.2 with lightsabers:kyber_crystal");
                    command("item replace entity @s hotbar.3 with lightsabers:crystal_pouch[lightsabers:crystal={color:16711680}]");
                    command("item replace entity @s hotbar.4 with lightsabers:crystal_pouch");
                    command("item replace entity @s hotbar.5 with lightsabers:lightsaber_stand");
                    command("item replace entity @s hotbar.6 with lightsabers:sith_sarcophagus");
                    command("item replace entity @s hotbar.7 with lightsabers:sith_stone_coffin");
                    command("item replace entity @s hotbar.8 with lightsabers:lightsaber_forge");
                    next();
                }
            }
            case 401 -> {
                if (stageTick >= 60) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    next();
                }
            }
            case 402 -> {
                if (stageTick >= 40) {
                    shot("90_icons_grid");
                    mc().setScreen(null);
                    command("clear @s");
                    command("item replace entity @s hotbar.0 with lightsabers:lightsaber_forge_dark");
                    command("item replace entity @s hotbar.1 with lightsabers:double_lightsaber");
                    command("item replace entity @s hotbar.2 with lightsabers:lightsaber");
                    next();
                }
            }
            case 403 -> {
                if (stageTick >= 50) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    next();
                }
            }
            case 404 -> {
                if (stageTick >= 40) {
                    shot("91_icons_sabers");
                    mc().setScreen(null);
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    next();
                    mc().scheduleStop();
                }
            }
            case 410 -> {

                if (stageTick == 10) {
                    respawnIfNeeded();
                    var inv = mc().player.getInventory();
                    for (int i = 0; i < inv.size(); i++) {
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                    inv.setStack(0, new ItemStack(ALBlocks.HOLOCRON_JEDI.asItem()));
                    inv.setStack(1, new ItemStack(ALBlocks.HOLOCRON_SITH.asItem()));
                    ItemStack kc = new ItemStack(ALItems.KYBER_CRYSTAL);
                    kc.set(ALComponents.CRYSTAL, new com.drag0nge0de.lightsabers.component.CrystalComponent(0x4FA8FF));
                    inv.setStack(2, kc);
                    ItemStack pd = new ItemStack(ALItems.CRYSTAL_POUCH);
                    pd.set(ALComponents.CRYSTAL, new com.drag0nge0de.lightsabers.component.CrystalComponent(0xFF0000));
                    inv.setStack(3, pd);
                    inv.setStack(4, new ItemStack(ALItems.CRYSTAL_POUCH));
                    inv.setStack(5, new ItemStack(ALBlocks.LIGHTSABER_STAND.asItem()));
                    inv.setStack(6, new ItemStack(ALBlocks.SITH_SARCOPHAGUS.asItem(), 4));
                    inv.setStack(7, new ItemStack(ALBlocks.SITH_STONE_COFFIN.asItem()));
                    inv.setStack(8, new ItemStack(ALBlocks.LIGHTSABER_FORGE.asItem()));
                    inv.setStack(9, new ItemStack(ALBlocks.DISASSEMBLY_STATION.asItem()));
                    log("staged 9 icon slots + disassembly client-side");
                    command("time set noon");
                    command("tp @s 99.5 121 -28.5 180 0");
                    next();
                }
            }
            case 411 -> {
                if (stageTick >= 30) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    next();
                }
            }
            case 412 -> {
                if (stageTick >= 30) {

                    try {
                        var inv = mc().player.getInventory();
                        for (int i : new int[]{3, 4, 8}) {
                            ItemStack p = inv.getStack(i);
                            var model = mc().getItemRenderer().getModel(p, mc().world, mc().player, 0);
                            var quads = model.getQuads(null, null, mc().world.random);
                            int sideQuads = 0;
                            for (var d : net.minecraft.util.math.Direction.values()) {
                                sideQuads += model.getQuads(null, d, mc().world.random).size();
                            }
                            log("model slot" + i + " (" + p.getItem() + ")=" + model.getClass().getName()
                                    + " builtin=" + model.isBuiltin() + " quads=" + quads.size()
                                    + " sideQuads=" + sideQuads);
                        }
                    } catch (Throwable t) {
                        log("pouch probe FAILED: " + t);
                    }
                    try {
                        for (String k : new String[]{"SithCoffin", "SithStoneCoffin", "LightsaberStand", "Crystal"}) {
                            float[] b = com.drag0nge0de.lightsabers.client.render.BlockEntityRenderers.tileBounds(k);
                            log(String.format("bounds %s=[%.3f %.3f %.3f .. %.3f %.3f %.3f]",
                                    k, b[0], b[1], b[2], b[3], b[4], b[5]));
                        }
                    } catch (Throwable t) {
                        log("bounds probe FAILED: " + t);
                    }
                    shot("92_iconcal");
                    mc().setScreen(null);
                    next();
                }
            }
            case 413 -> {

                if (stageTick == 10) {
                    var inv = mc().player.getInventory();
                    for (int i = 0; i < inv.size(); i++) {
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                    for (int i = 0; i < 4; i++) {
                        ItemStack s = new ItemStack(ALBlocks.SITH_SARCOPHAGUS.asItem(), i + 1);
                        inv.setStack(i, s);
                    }
                    inv.setStack(8, new ItemStack(ALItems.LIGHTSABER));
                    log("staged sarco cal + saber client-side");
                    next();
                }
            }
            case 414 -> {
                if (stageTick >= 30) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    next();
                }
            }
            case 415 -> {
                if (stageTick >= 30) {
                    shot("94_sarco_cal");
                    mc().setScreen(null);

                    var inv = mc().player.getInventory();
                    for (int i = 0; i < inv.size(); i++) {
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                    ItemStack inactive = new ItemStack(ALItems.LIGHTSABER);
                    inactive.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                            false, "knighted", 0x4FA8FF, false, 0));
                    inv.setStack(6, inactive);
                    inv.setStack(7, new ItemStack(ALItems.KYBER_CRYSTAL));
                    ItemStack active = new ItemStack(ALItems.LIGHTSABER);
                    active.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                            true, "knighted", 0x4FA8FF, false, 0));
                    inv.setStack(8, active);
                    mc().player.getInventory().selectedSlot = 6;
                    next();
                }
            }
            case 416 -> {
                if (stageTick == 1) {

                    var inv = mc().player.getInventory();
                    ItemStack inactive = new ItemStack(ALItems.LIGHTSABER);
                    inactive.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                            false, "graflex", 0x4FA8FF, false, 0));
                    inv.setStack(6, inactive);
                    ItemStack cr = new ItemStack(ALItems.KYBER_CRYSTAL);
                    cr.set(ALComponents.CRYSTAL, new com.drag0nge0de.lightsabers.component.CrystalComponent(0x4FA8FF));
                    inv.setStack(7, cr);
                    ItemStack active = new ItemStack(ALItems.LIGHTSABER);
                    active.set(ALComponents.LIGHTSABER, new com.drag0nge0de.lightsabers.component.LightsaberComponent(
                            true, "graflex", 0x4FA8FF, false, 0));
                    inv.setStack(8, active);
                    inv.selectedSlot = 6;
                    log("fp staged self-contained: inactive(6) crystal(7) active(8)");
                }
                if (stageTick >= 25) {
                    mc().player.setPitch(-55);
                    log("fp calls before inactive shot: " + com.drag0nge0de.lightsabers.client.render.ItemRenderers.Lightsaber.firstPersonCalls);
                    shot("95_fp_inactive");
                    mc().player.getInventory().setStack(6, ItemStack.EMPTY);
                    next();
                }
            }
            case 417 -> {
                if (stageTick >= 25) {
                    mc().player.setPitch(-55);
                    shot("98_fp_empty");
                    mc().player.getInventory().selectedSlot = 7;
                    next();
                }
            }
            case 418 -> {
                if (stageTick >= 25) {
                    mc().player.setPitch(-55);
                    shot("96_fp_crystal");
                    mc().player.getInventory().selectedSlot = 8;
                    next();
                }
            }
            case 419 -> {
                if (stageTick >= 25) {
                    mc().player.setPitch(-55);
                    log("fp calls before active shot: " + com.drag0nge0de.lightsabers.client.render.ItemRenderers.Lightsaber.firstPersonCalls);
                    shot("93_firstperson_saber");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    next();
                    mc().scheduleStop();
                }
            }
            case 420 -> {

                if (stageTick == 10) {
                    respawnIfNeeded();
                    var inv = mc().player.getInventory();
                    for (int i = 0; i < inv.size(); i++) {
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                    inv.setStack(0, new ItemStack(ALBlocks.HOLOCRON_JEDI.asItem()));
                    inv.setStack(1, new ItemStack(ALBlocks.HOLOCRON_SITH.asItem()));
                    ItemStack pd = new ItemStack(ALItems.CRYSTAL_POUCH);
                    pd.set(ALComponents.CRYSTAL, new com.drag0nge0de.lightsabers.component.CrystalComponent(0xFF0000));
                    inv.setStack(2, pd);
                    inv.setStack(3, new ItemStack(ALItems.CRYSTAL_POUCH));
                    inv.setStack(4, new ItemStack(ALBlocks.LIGHTSABER_STAND.asItem()));
                    inv.setStack(5, new ItemStack(ALBlocks.SITH_SARCOPHAGUS.asItem(), 4));
                    inv.setStack(6, new ItemStack(ALBlocks.SITH_STONE_COFFIN.asItem()));
                    inv.setStack(7, new ItemStack(ALBlocks.LIGHTSABER_FORGE.asItem()));
                    inv.setStack(8, new ItemStack(ALBlocks.DISASSEMBLY_STATION.asItem()));
                    log("staged 9 icon slots client-side");
                    command("time set noon");
                    next();
                }
            }
            case 421 -> {
                if (stageTick >= 30) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    next();
                }
            }
            case 422 -> {
                if (stageTick >= 40) {

                    try {
                        for (var group : net.minecraft.registry.Registries.ITEM_GROUP) {
                            if (!com.drag0nge0de.lightsabers.AL.id("main")
                                    .equals(net.minecraft.registry.Registries.ITEM_GROUP.getId(group))) {
                                continue;
                            }
                            group.updateEntries(new net.minecraft.item.ItemGroup.DisplayContext(
                                    mc().player.networkHandler.getEnabledFeatures(), false,
                                    mc().world.getRegistryManager()));
                            for (ItemStack s : group.getDisplayStacks()) {
                                var c = s.get(ALComponents.LIGHTSABER);
                                if (c != null) {
                                    String h = c.emitterHilt();
                                    int f = c.focusing();
                                    if (h.equals("mandalorian") || h.equals("fulcrum")
                                            || h.equals("knighted") || h.equals("rebel")) {
                                        log("preset " + h + " focusing=" + f);
                                        int expected = com.drag0nge0de.lightsabers.hilt.HiltStatsTable.defaultFocusing(h);
                                        assertEq("preset " + h + " focusing==" + expected, f == expected);
                                    }
                                }
                            }
                        }
                    } catch (Throwable t) {
                        log("creative preset probe FAILED: " + t);
                    }
                    shot("3924_icons_grid");
                    mc().setScreen(null);
                    next();
                }
            }
            case 423 -> {

                if (stageTick == 10) {
                    command("difficulty peaceful");
                    command("time set noon");
                    command("fill -2 119 -2 16 119 16 minecraft:smooth_stone");
                    command("setblock 6 120 4 lightsabers:holocron_jedi");
                    command("tp @s 3.5 122.2 9.0 168 32");
                    next();
                }
            }
            case 424 -> {
                if (stageTick >= 40) {
                    shot("3924_holocron_jedi_world");
                    command("setblock 6 120 4 lightsabers:holocron_sith");
                    command("tp @s 6.5 122.2 9.0 168 32");
                    next();
                }
            }
            case 425 -> {
                if (stageTick >= 40) {
                    shot("3924_holocron_sith_world");
                    command("setblock 3 120 2 lightsabers:lightsaber_stand[face=floor]");
                    command("setblock 6 120 2 lightsabers:lightsaber_stand[face=wall,facing=north]");
                    command("setblock 9 120 2 lightsabers:lightsaber_stand[face=ceiling]");
                    command("tp @s 6.5 122.2 7.5 180 38");
                    next();
                }
            }
            case 426 -> {
                if (stageTick >= 40) {
                    shot("3924_stands_hitbox");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    next();
                    mc().scheduleStop();
                }
            }
            case 440 -> {

                if (stageTick == 10) {
                    respawnIfNeeded();
                    command("difficulty peaceful");
                    command("time set noon");
                    command("fill -2 119 -2 16 119 16 minecraft:smooth_stone");
                    var inv = mc().player.getInventory();
                    for (int i = 0; i < inv.size(); i++) {
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                    inv.setStack(0, new ItemStack(ALBlocks.HOLOCRON_SITH.asItem()));
                    ItemStack pd = new ItemStack(ALItems.CRYSTAL_POUCH);
                    pd.set(ALComponents.CRYSTAL, new com.drag0nge0de.lightsabers.component.CrystalComponent(0xFF0000));
                    inv.setStack(1, pd);

                    for (int cmd = 1; cmd <= 3; cmd++) {
                        ItemStack v = new ItemStack(ALItems.CRYSTAL_POUCH);
                        v.set(net.minecraft.component.DataComponentTypes.CUSTOM_MODEL_DATA,
                                new net.minecraft.component.type.CustomModelDataComponent(cmd));
                        inv.setStack(5 + cmd, v);
                    }
                    inv.setStack(2, new ItemStack(ALBlocks.LIGHTSABER_STAND.asItem()));
                    ItemStack mandalorian = com.drag0nge0de.lightsabers.item.LightsaberItem.createSaber(
                            com.drag0nge0de.lightsabers.hilt.Hilt.byName("mandalorian"), 0xFF4040, false);
                    inv.setStack(3, mandalorian);
                    ItemStack plain = com.drag0nge0de.lightsabers.item.LightsaberItem.createSaber(
                            com.drag0nge0de.lightsabers.hilt.Hilt.byName("knighted"), 0x40FF40, false);
                    inv.setStack(4, plain);
                    ItemStack cracked = com.drag0nge0de.lightsabers.item.LightsaberItem.createSaber(
                            com.drag0nge0de.lightsabers.hilt.Hilt.byName("knighted"), 0xFFFFFF, false);
                    cracked.set(ALComponents.LIGHTSABER,
                            com.drag0nge0de.lightsabers.item.LightsaberItem.getComponent(cracked)
                                    .withFocusing(1));
                    inv.setStack(5, cracked);
                    command("setblock 3 120 4 lightsabers:holocron_jedi");
                    command("setblock 6 120 4 lightsabers:holocron_sith");
                    command("setblock 3 120 2 lightsabers:lightsaber_stand[face=floor]");
                    command("setblock 6 120 2 lightsabers:lightsaber_stand[face=wall,facing=north]");
                    command("setblock 9 120 2 lightsabers:lightsaber_stand[face=ceiling]");
                    log("3925 staged: items + world blocks");

                    try {
                        var renderer = mc().getItemRenderer();
                        var model = renderer.getModel(new ItemStack(ALItems.CRYSTAL_POUCH), mc().world, null, 0);
                        var quads = model.getQuads(null, null, net.minecraft.util.math.random.Random.create(1));
                        log("pouch baked model: " + model.getClass().getSimpleName() + " quads=" + quads.size()
                                + " particle=" + model.getParticleSprite().toString());
                        for (int i = 0; i < Math.min(3, quads.size()); i++) {
                            var q = quads.get(i);
                            log("pouch quad " + i + " sprite=" + q.getSprite().toString() + " face=" + q.getFace()
                                    + " tint=" + q.getColorIndex());
                        }
                        var stick = renderer.getModel(new ItemStack(net.minecraft.item.Items.STICK), mc().world, null, 0);
                        log("stick baked model: " + stick.getClass().getSimpleName() + " quads="
                                + stick.getQuads(null, null, net.minecraft.util.math.random.Random.create(1)).size());
                    } catch (Throwable t) {
                        log("model probe FAILED: " + t);
                    }
                    next();
                }
            }
            case 441 -> {
                if (stageTick >= 30) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    next();
                }
            }
            case 442 -> {
                if (stageTick >= 40) {
                    shot("3925_icons");
                    mc().setScreen(null);
                    mc().options.hudHidden = true;
                    command("gamerule sendCommandFeedback false");
                    command("tp @s 3.5 122.2 9.0 168 32");
                    next();
                }
            }
            case 443 -> {
                if (stageTick >= 40) {
                    shot("3925_holocrons_a");
                    command("tp @s 9.5 122.2 9.0 214 32");
                    next();
                }
            }
            case 444 -> {
                if (stageTick >= 40) {
                    shot("3925_holocrons_b");
                    selectEmptySlot();
                    next();
                }
            }
            case 445 -> {
                if (stageTick >= 10) {
                    useBlock(new BlockPos(6, 120, 4));
                    next();
                }
            }
            case 446 -> {
                if (stageTick >= 50) {
                    shot("3925_sith_open");
                    mc().setScreen(null);
                    mc().options.hudHidden = true;
                    command("tp @s 6.5 122.2 7.5 180 38");
                    next();
                }
            }
            case 447 -> {
                if (stageTick >= 40) {
                    shot("3925_stands");
                    command("tp @s 6.5 122.2 7.5 90 38");
                    next();
                }
            }
            case 448 -> {
                if (stageTick >= 40) {
                    shot("3925_stands_b");

                    mc().options.hudHidden = false;
                    selectItem(ALItems.CRYSTAL_POUCH);
                    mc().player.setPitch(0.35F);
                    next();
                }
            }
            case 449 -> {
                if (stageTick >= 30) {
                    shot("3925_pouch_hand");

                    command("clear @s");
                    command("give @s lightsabers:crystal_pouch");
                    next();
                }
            }
            case 450 -> {
                if (stageTick >= 30) {

                    var hotbar0 = mc().player.getInventory().getStack(0);
                    log("hotbar0 before drop: " + hotbar0.getItem());
                    mc().player.getInventory().selectedSlot = 0;
                    command("tp @s 6.5 121.0 7.5 180 74");
                    next();
                }
            }
            case 451 -> {
                if (stageTick >= 20) {
                    mc().player.dropSelectedItem(false);
                    next();
                }
            }
            case 452 -> {
                if (stageTick >= 45) {
                    shot("3925_pouch_ground");

                    command("clear @s");
                    command("altest saber knighted");
                    next();
                }
            }
            case 453 -> {
                if (stageTick >= 30) {
                    boolean active = com.drag0nge0de.lightsabers.item.LightsaberItem
                            .getComponent(mc().player.getMainHandStack()).active();
                    assertEq("altest saber arrives ACTIVE", active);

                    mc().interactionManager.interactItem(mc().player, Hand.MAIN_HAND);
                    next();
                }
            }
            case 454 -> {
                if (stageTick >= 15) {
                    boolean stillActive = com.drag0nge0de.lightsabers.item.LightsaberItem
                            .getComponent(mc().player.getMainHandStack()).active();
                    assertEq("right-click does NOT toggle the saber", stillActive);
                    pressOnce("saberKey");
                    next();
                }
            }
            case 455 -> {
                if (stageTick >= 15) {
                    boolean off = !com.drag0nge0de.lightsabers.item.LightsaberItem
                            .getComponent(mc().player.getMainHandStack()).active();
                    assertEq("R keybind toggles the saber OFF", off);

                    command("altest saber knighted 1");
                    next();
                }
            }
            case 456 -> {
                if (stageTick >= 30) {
                    boolean active = com.drag0nge0de.lightsabers.item.LightsaberItem
                            .getComponent(mc().player.getMainHandStack()).active();
                    assertEq("cracked saber lit", active);
                    mc().player.setPitch(-0.3F);
                    next();
                }
            }
            case 457 -> {
                if (stageTick >= 35) {
                    shot("3925_cracked_fp");

                    try {
                        var m = net.minecraft.client.MinecraftClient.class.getDeclaredMethod("doAttack");
                        m.setAccessible(true);
                        m.invoke(mc());
                        log("invoked doAttack for the swing probe");
                    } catch (Throwable t) {
                        log("doAttack reflection FAILED: " + t);
                    }
                    next();
                }
            }
            case 458 -> {
                if (stageTick >= 25) {
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    next();
                    mc().scheduleStop();
                }
            }
            case 430 -> {

                if (stageTick == 10) {
                    command("time set noon");
                    command("tp @s 6.5 121.8 8.5 180 15");
                    next();
                }
            }
            case 431 -> {
                if (stageTick >= 40) {
                    shot("95_stands_hitbox");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    next();
                    mc().scheduleStop();
                }
            }

            case 470 -> {
                if (stageTick >= 10) {
                    command("difficulty peaceful");
                    next();
                }
            }
            case 471 -> {
                if (stageTick >= 10) {
                    command("fill -10 198 -10 10 198 10 smooth_stone");
                    command("fill -10 199 -10 10 212 10 air");
                    command("time set midnight");
                    command("gamerule doDaylightCycle false");
                    next();
                }
            }
            case 472 -> {
                if (stageTick >= 20) {

                    command("altest saber knighted");
                    next();
                }
            }
            case 473 -> {
                if (stageTick >= 20) {
                    command("tp @s 0.5 199 4.5 0 0");
                    selectItem(ALItems.LIGHTSABER);
                    client.player.setPitch(0.3F);
                    next();
                }
            }
            case 474 -> {
                if (stageTick >= 45) {
                    shot("50_glow_crossguard_fp");
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    next();
                }
            }
            case 475 -> {
                if (stageTick >= 15) {
                    client.player.setPitch(0.15F);
                    next();
                }
            }
            case 476 -> {
                if (stageTick >= 15) {
                    shot("51_glow_crossguard_tp");
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    next();
                }
            }
            case 477 -> {
                if (stageTick >= 10) {
                    command("altest saber graflex");
                    next();
                }
            }
            case 478 -> {
                if (stageTick >= 20) {
                    selectItem(ALItems.LIGHTSABER);
                    client.player.setPitch(0.3F);
                    next();
                }
            }
            case 479 -> {
                if (stageTick >= 25) {
                    shot("52_glow_plain_fp");
                    next();
                }
            }
            case 480 -> {
                if (stageTick >= 10) {
                    command("altest saber knighted 2");
                    next();
                }
            }
            case 481 -> {
                if (stageTick >= 20) {
                    selectItem(ALItems.LIGHTSABER);
                    next();
                }
            }
            case 482 -> {
                if (stageTick >= 25) {
                    shot("53_glow_cracked_fp");
                    next();
                }
            }
            case 483 -> {
                if (stageTick >= 10) {
                    client.setScreen(new InventoryScreen(client.player));
                    next();
                }
            }
            case 484 -> {
                if (stageTick >= 25) {
                    shot("54_icons_regression");
                    client.setScreen(null);
                    next();
                }
            }
            case 485 -> {
                if (stageTick >= 10) {
                    client.player.dropSelectedItem(false);
                    client.player.setPitch(0.55F);
                    next();
                }
            }
            case 486 -> {
                if (stageTick >= 40) {
                    shot("55_glow_dropped");
                    next();
                }
            }
            case 487 -> {
                if (stageTick >= 10) {
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    log("scenes complete");
                    next();
                    mc().scheduleStop();
                }
            }
            case 490 -> {

                if (stageTick == 10) {
                    respawnIfNeeded();
                    command("difficulty peaceful");
                    command("time set noon");
                    command("fill 2 119 2 14 119 14 minecraft:red_wool");
                    command("fill 2 120 2 14 124 14 minecraft:air");
                    command("setblock 8 120 8 lightsabers:lightsaber_stand[face=floor]");
                    var model = com.drag0nge0de.lightsabers.client.render.model.TileModels.cached("LightsaberStand");
                    StringBuilder sb = new StringBuilder("stand tree root=s("
                            + model.xScale + "," + model.yScale + "," + model.zScale + ")");
                    String[][] chains = {{"w0"}, {"w0","ws0"}, {"w0","ws0","wb0"},
                            {"w0","ws0","wb0","n2"}, {"w0","ws0","wb0","n2","n0"},
                            {"w0","ws0","wb0","n2","n8"}, {"w3"}, {"w3","ws3"}, {"w3","ws3","wb3"},
                            {"w3","ws3","wb3","n7"}, {"n3"}, {"n3","n1"}, {"n4"}};
                    for (String[] chain : chains) {
                        var p = model;
                        for (String c : chain) {
                            p = p.getChild(c);
                        }
                        sb.append(" | ").append(chain[chain.length - 1]).append(":p(")
                                .append(p.pivotX).append(",").append(p.pivotY).append(",").append(p.pivotZ)
                                .append(")s(").append(p.xScale).append(",").append(p.yScale)
                                .append(",").append(p.zScale).append(")");
                    }
                    log(sb.toString());
                    var ms = new net.minecraft.client.util.math.MatrixStack();
                    ms.translate(0.5F, 0.5F, 0.5F);
                    ms.scale(1, -1, -1);
                    ms.translate(0, -1, 0);
                    float[] bb = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                            -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
                    model.forEachCuboid(ms, (entry, path, idx, cuboid) -> {
                        var m = entry.getPositionMatrix();
                        for (float cx : new float[]{cuboid.minX, cuboid.maxX}) {
                            for (float cy : new float[]{cuboid.minY, cuboid.maxY}) {
                                for (float cz : new float[]{cuboid.minZ, cuboid.maxZ}) {
                                    var v = m.transformPosition(cx / 16F, cy / 16F, cz / 16F,
                                            new org.joml.Vector3f());
                                    bb[0] = Math.min(bb[0], v.x);
                                    bb[1] = Math.min(bb[1], v.y);
                                    bb[2] = Math.min(bb[2], v.z);
                                    bb[3] = Math.max(bb[3], v.x);
                                    bb[4] = Math.max(bb[4], v.y);
                                    bb[5] = Math.max(bb[5], v.z);
                                }
                            }
                        }
                    });
                    log(String.format("stand frame bbox: x[%.4f,%.4f] y[%.4f,%.4f] z[%.4f,%.4f]",
                            bb[0], bb[3], bb[1], bb[4], bb[2], bb[5]));
                    command("tp @s 8.5 122.6 11.6 180 38");
                    next();
                }
            }
            case 491 -> {
                if (stageTick >= 40) {
                    shot("56_stand_outline_low");
                    next();
                }
            }
            case 492 -> {
                if (stageTick >= 10) {

                    command("tp @s 8.5 126.5 8.5 180 90");
                    mc().options.hudHidden = true;
                    next();
                }
            }
            case 493 -> {
                if (stageTick >= 40) {
                    var p = client.player;
                    log(String.format("cam: pos=%.3f,%.3f,%.3f pitch=%.1f yaw=%.1f",
                            p.getX(), p.getY(), p.getZ(), p.getPitch(), p.getYaw()));
                    shot("59_stand_clean_top");
                    next();
                }
            }
            case 494 -> {
                if (stageTick >= 10) {
                    mc().options.hudHidden = false;
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    log("stand scenes complete");
                    next();
                    mc().scheduleStop();
                }
            }
            case 495 -> {
                if (stageTick >= 10) {
                    next();
                }
            }
            case 500 -> {
                if (stageTick >= 10) {
                    respawnIfNeeded();
                    command("difficulty peaceful");
                    command("time set noon");
                    command("gamerule doDaylightCycle false");
                    command("gamerule keepInventory true");
                    command("fill -6 169 -6 6 169 6 smooth_stone");
                    command("fill -6 170 -6 6 182 6 air");
                    command("tp @s 0.5 170 0.5 0 0");
                    next();
                }
            }
            case 501 -> {
                if (stageTick >= 20) {
                    command("altest saber mandalorian 12");
                    next();
                }
            }
            case 502 -> {
                if (stageTick >= 20) {
                    selectItem(ALItems.LIGHTSABER);
                    client.player.setPitch(-63.0F);
                    next();
                }
            }
            case 503 -> {
                if (stageTick >= 160) {
                    shot("60_day_mando_fp");
                    setProbe(new float[]{0.0F, 0.0F, 0.0F, 0.0F});
                    next();
                }
            }
            case 504 -> {
                if (stageTick >= 20) {
                    shot("61_probe_zero");
                    next();
                }
            }
            case 505 -> {
                if (stageTick >= 10) {
                    next();
                }
            }
            case 506 -> {
                if (stageTick >= 10) {
                    next();
                }
            }
            case 507 -> {
                if (stageTick >= 10) {
                    next();
                }
            }
            case 508 -> {
                if (stageTick >= 10) {
                    next();
                }
            }
            case 509 -> {
                if (stageTick >= 10) {
                    next();
                }
            }
            case 510 -> {
                if (stageTick >= 10) {
                    next();
                }
            }
            case 511 -> {
                if (stageTick >= 15) {
                    shot("68_day_glow_final");
                    next();
                }
            }
            case 512 -> {
                if (stageTick >= 10) {
                    command("time set midnight");
                    next();
                }
            }
            case 513 -> {
                if (stageTick >= 40) {
                    shot("69_night_glow");
                    next();
                }
            }
            case 514 -> {
                if (stageTick >= 10) {
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    client.player.setPitch(0.2F);
                    next();
                }
            }
            case 515 -> {
                if (stageTick >= 20) {
                    shot("70_tp_revert");
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    next();
                }
            }
            case 516 -> {
                if (stageTick >= 10) {
                    client.setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(client.player));
                    next();
                }
            }
            case 517 -> {
                if (stageTick >= 20) {
                    shot("71_icons_inverting");
                    client.setScreen(null);
                    next();
                }
            }
            case 518 -> {
                if (stageTick >= 10) {
                    java.util.HashSet<String> all = new java.util.HashSet<>();
                    for (com.drag0nge0de.lightsabers.force.Power pw : com.drag0nge0de.lightsabers.force.Power.POWERS) {
                        all.add(pw.getName());
                    }
                    com.drag0nge0de.lightsabers.client.ForceClientState.update(500, 160.0F, 160.0F, 4.0F, all, null);
                    com.drag0nge0de.lightsabers.client.ForceClientState.setSlotPower(0, "speed");
                    client.setScreen(new com.drag0nge0de.lightsabers.client.screen.ForceSelectScreen());
                    var win = client.getWindow();
                    ALShotsBot.selectHover = new int[]{win.getScaledWidth() / 2 - 72,
                            win.getScaledHeight() / 2 - 67};
                    log("selectHover = " + java.util.Arrays.toString(ALShotsBot.selectHover));
                    next();
                }
            }
            case 519 -> {
                if (stageTick >= 20) {
                    shot("72_selector_hover");
                    ALShotsBot.selectHover = null;
                    client.setScreen(null);
                    next();
                }
            }
            case 520 -> {
                if (stageTick >= 10) {
                    command("weather clear");
                    command("time set noon");
                    command("fill 24 169 24 36 169 36 smooth_stone");
                    command("fill 24 170 24 36 182 36 air");
                    command("kill @e[type=!minecraft:player]");
                    command("tp @s 30.5 170 30.5 0 0");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber");
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    client.player.setPitch(0.2F);
                    client.player.setYaw(0.0F);
                    setTpProbe(null);
                    setTpCorrection(true);
                    next();
                }
            }
            case 521 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    client.player.setPitch(0.0F);
                    next();
                }
            }
            case 522 -> {
                if (stageTick >= 50) {
                    shot("81_single_front_correction_on");
                    setTpCorrection(false);
                    next();
                }
            }
            case 523 -> {
                if (stageTick >= 25) {
                    shot("82_single_front_correction_off");
                    setTpCorrection(true);
                    next();
                }
            }
            case 524 -> {
                if (stageTick >= 25) {
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    client.player.setPitch(0.2F);
                    next();
                }
            }
            case 525 -> {
                if (stageTick >= 25) {
                    shot("83_single_back_correction_on");
                    client.player.setPitch(0.0F);
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    next();
                }
            }
            case 526 -> {
                if (stageTick >= 25) {
                    command("altest saber mandalorian 0");
                    next();
                }
            }
            case 527 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    shot("84_mando_front_on");
                    setTpProbe(new float[]{0.0F, 0.15F, 0.0F});
                    next();
                }
            }
            case 528 -> {
                if (stageTick >= 25) {
                    shot("85_mando_bias_up");
                    setTpProbe(new float[]{0.0F, -0.15F, 0.0F});
                    next();
                }
            }
            case 529 -> {
                if (stageTick >= 25) {
                    shot("86_mando_bias_down");
                    setTpProbe(null);
                    command("altest saber mauler 0");
                    next();
                }
            }
            case 530 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.DOUBLE_LIGHTSABER);
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    client.player.setPitch(0.0F);
                    shot("87_double_front_on");
                    setTpCorrection(false);
                    next();
                }
            }
            case 531 -> {
                if (stageTick >= 25) {
                    shot("88_double_front_off");
                    setTpCorrection(true);
                    setTpProbe(null);
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    command("clear @s");
                    command("give @s lightsabers:lightsaber");
                    selectItem(ALItems.LIGHTSABER);
                    next();
                }
            }
            case 532 -> {
                if (stageTick >= 20) {
                    shot("89_fp_sanity");
                    command("clear @s");
                    command("altest saber mandalorian 0");
                    command("give @s lightsabers:lightsaber");
                    command("setblock 32 170 32 crafting_table");
                    next();
                }
            }
            case 533 -> {
                if (stageTick >= 30) {
                    useBlock(new BlockPos(32, 170, 32));
                    next();
                }
            }
            case 534 -> {
                if (stageTick >= 20) {
                    var handler = client.player.currentScreenHandler;
                    log("screen handler = " + handler.getClass().getName() + " slots=" + handler.slots.size());
                    log("hotbar0=" + client.player.getInventory().getStack(0).getItem()
                        + " hotbar1=" + client.player.getInventory().getStack(1).getItem());
                    next();
                }
            }
            case 535 -> {
                if (stageTick >= 5) {
                    var handler = client.player.currentScreenHandler;
                    client.interactionManager.clickSlot(handler.syncId, 37, 0, SlotActionType.PICKUP, client.player);
                    client.interactionManager.clickSlot(handler.syncId, 1, 0, SlotActionType.PICKUP, client.player);
                    client.interactionManager.clickSlot(handler.syncId, 38, 0, SlotActionType.PICKUP, client.player);
                    client.interactionManager.clickSlot(handler.syncId, 4, 0, SlotActionType.PICKUP, client.player);
                    log("grid filled with two sabers");
                    shot("92_craft_grid_filled");
                    next();
                }
            }
            case 536 -> {
                if (stageTick >= 15) {
                    var handler = client.player.currentScreenHandler;
                    log("cursor before take = " + handler.getCursorStack());
                    client.interactionManager.clickSlot(handler.syncId, 0, 0, SlotActionType.PICKUP, client.player);
                    var taken = handler.getCursorStack();
                    log("cursor after take = " + taken);
                    boolean isDouble = taken.isOf(ALItems.DOUBLE_LIGHTSABER);
                    var comp = com.drag0nge0de.lightsabers.item.LightsaberItem.getComponent(taken);
                    assertEq("crafted result is a double saber", isDouble);
                    assertEq("crafted double carries a second blade", comp.second().isPresent());
                    assertEq("crafted double grip hilt preserved from upper saber",
                            "mandalorian".equals(comp.hilt()));
                    client.interactionManager.clickSlot(handler.syncId, 39, 0, SlotActionType.PICKUP, client.player);
                    log("result stored in hotbar");
                    next();
                }
            }
            case 537 -> {
                if (stageTick >= 15) {
                    close();
                    selectItem(ALItems.DOUBLE_LIGHTSABER);
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    client.player.setPitch(0.0F);
                    next();
                }
            }
            case 538 -> {
                if (stageTick >= 30) {
                    shot("93_crafted_double_front");
                    command("altest craftdouble");
                    next();
                }
            }
            case 539 -> {
                if (stageTick >= 40) {
                    shot("94_craftdouble_chat");
                    next();
                }
            }
            case 540 -> {
                if (stageTick >= 10) {
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    log("scenes complete (craft section)");
                    next();
                }
            }
            case 541 -> {
                if (stageTick == 10) {
                    close();
                    command("weather clear");
                    command("time set noon");
                    command("fill 24 169 24 36 169 36 smooth_stone");
                    command("fill 24 170 24 36 182 36 air");
                    command("kill @e[type=!minecraft:player]");
                    command("tp @s 30.5 170 30.5 0 0");
                    command("gamemode survival");
                    command("clear @s");
                    command("item replace entity @s container.0 with lightsabers:lightsaber_blade_emitter[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.1 with lightsabers:lightsaber_switch_module[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.2 with lightsabers:lightsaber_grip[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.3 with lightsabers:lightsaber_pommel[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.4 with lightsabers:lightsaber_blade_emitter[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.5 with lightsabers:lightsaber_switch_module[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.6 with lightsabers:lightsaber_grip[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.7 with lightsabers:lightsaber_pommel[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.8 with lightsabers:kyber_crystal");
                    command("item replace entity @s container.9 with lightsabers:kyber_crystal[lightsabers:crystal={color:16711680}]");
                    command("item replace entity @s container.10 with lightsabers:lightsaber_forge");
                    log("staged 4+4 part icons + 2 crystals + forge (server-side)");
                    next();
                }
            }
            case 542 -> {
                if (stageTick >= 20) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    next();
                }
            }
            case 543 -> {
                if (stageTick >= 40) {
                    ItemRenderers.guiPoseProbe = 0;
                    next();
                }
            }
            case 544 -> {
                if (stageTick >= 20) {
                    for (String h : new String[]{"mandalorian", "grafx"}) {
                        for (String p : new String[]{"emitter", "switch_section", "body", "pommel"}) {
                            float[] bb = HiltRenderer.partBounds(h, p);
                            float proj = HiltRenderer.partProjectedExtent(h, p);
                            log("pose " + h + "/" + p + " bb=" + java.util.Arrays.toString(bb)
                                    + " proj=" + proj);
                        }
                    }
                    shot("95_icon_pose_old");
                    ItemRenderers.guiPoseProbe = 1;
                    next();
                }
            }
            case 545 -> {
                if (stageTick >= 20) {
                    shot("96_icon_v1");
                    ItemRenderers.guiPoseProbe = 2;
                    next();
                }
            }
            case 546 -> {
                if (stageTick >= 20) {
                    shot("97_icon_v2");
                    ItemRenderers.guiPoseProbe = 3;
                    next();
                }
            }
            case 547 -> {
                if (stageTick >= 20) {
                    shot("98_icon_v3");
                    ItemRenderers.guiPoseProbe = 4;
                    next();
                }
            }
            case 548 -> {
                if (stageTick >= 20) {
                    shot("99_icon_v4");
                    ItemRenderers.guiPoseProbe = -1;
                    mc().setScreen(null);
                    next();
                }
            }
            case 549 -> {
                if (stageTick >= 20) {
                    command("force structure locate crystalcave");
                    next();
                }
            }
            case 550 -> {
                if (stageTick >= 100) {
                    shot("a1_force_locate_chat");
                    next();
                }
            }
            case 551 -> {
                if (stageTick >= 10) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ChatScreen("/force structure locate "));
                    next();
                }
            }
            case 552 -> {
                if (stageTick >= 40) {
                    shot("a2_force_suggest");
                    mc().setScreen(null);
                    next();
                }
            }
            case 553 -> {
                if (stageTick >= 10) {
                    command("clear @s");
                    command("altest saber mandalorian 0");
                    command("give @s lightsabers:lightsaber");
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    client.player.setPitch(0.0F);
                    setTpProbe(null);
                    setTpCorrection(true);
                    next();
                }
            }
            case 554 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    shot("b1_tp_mando_d0");
                    setTpProbe(new float[]{0.0F, 0.05F, 0.0F});
                    next();
                }
            }
            case 555 -> {
                if (stageTick >= 25) {
                    shot("b2_tp_mando_d05");
                    setTpProbe(new float[]{0.0F, 0.10F, 0.0F});
                    next();
                }
            }
            case 556 -> {
                if (stageTick >= 25) {
                    shot("b3_tp_mando_d10");
                    setTpProbe(new float[]{0.0F, 0.15F, 0.0F});
                    next();
                }
            }
            case 557 -> {
                if (stageTick >= 25) {
                    shot("b4_tp_mando_d15");
                    setTpProbe(new float[]{0.0F, 0.20F, 0.0F});
                    next();
                }
            }
            case 558 -> {
                if (stageTick >= 25) {
                    shot("b5_tp_mando_d20");
                    setTpProbe(new float[]{0.0F, 0.30F, 0.0F});
                    next();
                }
            }
            case 559 -> {
                if (stageTick >= 25) {
                    shot("b6_tp_mando_d30");
                    setTpProbe(null);
                    command("altest saber graflex");
                    command("give @s lightsabers:lightsaber");
                    next();
                }
            }
            case 560 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    shot("b7_tp_graflex_d0");
                    setTpProbe(new float[]{0.0F, 0.10F, 0.0F});
                    next();
                }
            }
            case 561 -> {
                if (stageTick >= 25) {
                    shot("b8_tp_graflex_d10");
                    setTpProbe(new float[]{0.0F, 0.20F, 0.0F});
                    next();
                }
            }
            case 562 -> {
                if (stageTick >= 25) {
                    shot("b9_tp_graflex_d20");
                    setTpProbe(null);
                    next();
                }
            }
            case 563 -> {
                if (stageTick >= 25) {
                    command("time set noon");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    log("scenes complete (probe section)");
                    next();
                }
            }
            case 570 -> {
                if (stageTick >= 10) {
                    command("clear @s");
                    command("give @s lightsabers:lightsaber");
                    command("time set noon");
                    command("weather clear");
                    command("tp @s 30.5 170 30.5 0 0");
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    client.player.setPitch(0.0F);
                    setTpProbe(new float[]{0.0F, -0.45F, 0.0F});
                    setTpCorrection(true);
                    next();
                }
            }
            case 571 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    shot("c1_tuck_off");
                    setTpProbe(null);
                    next();
                }
            }
            case 572 -> {
                if (stageTick >= 25) {
                    shot("c2_tuck_on");
                    setTpProbe(new float[]{0.0F, -0.25F, 0.0F});
                    next();
                }
            }
            case 573 -> {
                if (stageTick >= 25) {
                    shot("c3_tuck_plus");
                    setTpProbe(null);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    client.player.setPitch(0.2F);
                    next();
                }
            }
            case 574 -> {
                if (stageTick >= 25) {
                    shot("c4_tuck_back");
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    client.player.setPitch(0.0F);
                    next();
                }
            }
            case 575 -> {
                if (stageTick >= 25) {
                    shot("c5_fp_sanity");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    log("scenes complete");
                    next();
                    mc().scheduleStop();
                }
            }
            case 580 -> {
                if (stageTick >= 10) {
                    close();
                    command("gamerule sendCommandFeedback false");
                    command("gamerule logAdminCommands false");
                    command("weather clear");
                    command("time set noon");
                    command("fill -4 169 -4 64 169 64 smooth_stone");
                    command("fill -4 170 -4 64 182 64 air");
                    command("kill @e[type=!minecraft:player]");
                    command("tp @s 30.5 170 30.5 0 0");
                    command("gamemode survival");
                    command("clear @s");
                    command("give @s lightsabers:double_lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"mandalorian\",switch_hilt:\"mandalorian\",hilt:\"mandalorian\","
                            + "pommel_hilt:\"mandalorian\",color:65280,double:true,focusing:4,"
                            + "second_blade:{hilt:\"mandalorian\",color:65280}}] 1");
                    next();
                }
            }
            case 581 -> {
                if (stageTick >= 20) {
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    client.player.setPitch(-75.0F);
                    client.player.setYaw(0.0F);
                    selectItem(ALItems.DOUBLE_LIGHTSABER);
                    setTpProbe(null);
                    setTpCorrection(true);
                    client.options.forwardKey.setPressed(true);
                    log("keyForward pressed (walk swing for TD visibility)");
                    next();
                }
            }
            case 582 -> {
                if (stageTick >= 45) {
                    shot("f2_td_p0");
                    setTpProbe(new float[]{0.0F, 0.24F, 0.0F});
                    next();
                }
            }
            case 583 -> {
                if (stageTick >= 25) {
                    shot("f2_td_p24");
                    setTpProbe(new float[]{0.0F, 0.48F, 0.0F});
                    next();
                }
            }
            case 584 -> {
                if (stageTick >= 25) {
                    shot("f2_td_p48");
                    setTpProbe(new float[]{0.0F, 0.72F, 0.0F});
                    next();
                }
            }
            case 585 -> {
                if (stageTick >= 25) {
                    shot("f2_td_p72");
                    setTpProbe(new float[]{0.0F, -0.24F, 0.0F});
                    next();
                }
            }
            case 586 -> {
                if (stageTick >= 25) {
                    shot("f2_td_m24");
                    setTpProbe(new float[]{0.0F, -0.48F, 0.0F});
                    next();
                }
            }
            case 587 -> {
                if (stageTick >= 25) {
                    shot("f2_td_m48");
                    setTpProbe(null);
                    client.options.forwardKey.setPressed(false);
                    log("keyForward released");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"mandalorian\",switch_hilt:\"mandalorian\",hilt:\"mandalorian\","
                            + "pommel_hilt:\"mandalorian\",color:65280,double:false,focusing:4}] 1");
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    client.player.setPitch(-38.0F);
                    next();
                }
            }
            case 588 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    shot("f1_fs_p0");
                    setTpProbe(new float[]{0.0F, 0.24F, 0.0F});
                    next();
                }
            }
            case 589 -> {
                if (stageTick >= 25) {
                    shot("f1_fs_p24");
                    setTpProbe(new float[]{0.0F, 0.48F, 0.0F});
                    next();
                }
            }
            case 590 -> {
                if (stageTick >= 25) {
                    shot("f1_fs_p48");
                    setTpProbe(new float[]{0.0F, 0.72F, 0.0F});
                    next();
                }
            }
            case 591 -> {
                if (stageTick >= 25) {
                    shot("f1_fs_p72");
                    setTpProbe(new float[]{0.0F, -0.24F, 0.0F});
                    next();
                }
            }
            case 592 -> {
                if (stageTick >= 25) {
                    shot("f1_fs_m24");
                    setTpProbe(null);
                    command("clear @s");
                    command("give @s lightsabers:lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"graflex\",switch_hilt:\"graflex\",hilt:\"graflex\","
                            + "pommel_hilt:\"graflex\",color:65280,double:false,focusing:4}] 1");
                    next();
                }
            }
            case 593 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    shot("f3_gf_p0");
                    setTpProbe(new float[]{0.0F, 0.48F, 0.0F});
                    next();
                }
            }
            case 594 -> {
                if (stageTick >= 25) {
                    shot("f3_gf_p48");
                    setTpProbe(null);
                    command("item replace entity @s container.0 with lightsabers:lightsaber_blade_emitter[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.1 with lightsabers:lightsaber_switch_module[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.2 with lightsabers:lightsaber_grip[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.3 with lightsabers:lightsaber_pommel[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.4 with lightsabers:lightsaber_blade_emitter[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.5 with lightsabers:lightsaber_switch_module[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.6 with lightsabers:lightsaber_grip[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.7 with lightsabers:lightsaber_pommel[lightsabers:hilt={hilt:\"grafx\"}]");
                    log("staged 4+4 part icons for angle probe");
                    next();
                }
            }
            case 595 -> {
                if (stageTick >= 20) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    ItemRenderers.guiPoseProbe = 8;
                    next();
                }
            }
            case 596 -> {
                if (stageTick >= 30) {
                    shot("f4_icon_v8");
                    ItemRenderers.guiPoseProbe = 9;
                    next();
                }
            }
            case 597 -> {
                if (stageTick >= 25) {
                    shot("f4_icon_v9");
                    ItemRenderers.guiPoseProbe = -1;
                    next();
                }
            }
            case 598 -> {
                if (stageTick >= 25) {
                    shot("f4_icon_cur");
                    mc().setScreen(null);
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    client.player.setPitch(0.0F);
                    next();
                }
            }
            case 599 -> {
                if (stageTick >= 25) {
                    shot("f5_fp_sanity");
                    command("clear @s");
                    command("give @s lightsabers:double_lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"mandalorian\",switch_hilt:\"mandalorian\",hilt:\"mandalorian\","
                            + "pommel_hilt:\"mandalorian\",color:65280,double:true,focusing:4,"
                            + "second_blade:{hilt:\"mandalorian\",color:65280}}] 1");
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    client.player.setPitch(-38.0F);
                    next();
                }
            }
            case 600 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.DOUBLE_LIGHTSABER);
                    shot("f6_dbl_front_p0");
                    setTpProbe(new float[]{0.0F, 0.24F, 0.0F});
                    next();
                }
            }
            case 601 -> {
                if (stageTick >= 25) {
                    shot("f6_dbl_front_p24");
                    setTpProbe(null);
                    next();
                }
            }
            case 602 -> {
                if (stageTick >= 25) {
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    log("scenes complete (grip+icon probe section v3)");
                    next();
                    mc().scheduleStop();
                }
            }
            case 610 -> {
                if (stageTick >= 10) {
                    close();
                    command("gamerule sendCommandFeedback false");
                    command("gamerule logAdminCommands false");
                    command("weather clear");
                    command("time set noon");
                    command("kill @e[type=!minecraft:player]");
                    command("tp @s 30.5 170 30.5 0 0");
                    command("gamemode survival");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber");
                    log("v3.9.36 TP/icon verify suite: inactive single mando given");
                    next();
                }
            }
            case 611 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    client.player.setPitch(0.0F);
                    client.player.setYaw(0.0F);
                    setTpProbe(null);
                    next();
                }
            }
            case 612 -> {
                if (stageTick >= 40) {
                    shot("g1_single_front");
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    client.player.setPitch(0.2F);
                    next();
                }
            }
            case 613 -> {
                if (stageTick >= 30) {
                    shot("g2_single_back");
                    client.player.setPitch(-72.0F);
                    next();
                }
            }
            case 614 -> {
                if (stageTick >= 30) {
                    shot("g3_single_topdown");
                    command("clear @s");
                    command("give @s lightsabers:double_lightsaber");
                    next();
                }
            }
            case 615 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.DOUBLE_LIGHTSABER);
                    client.player.setPitch(-72.0F);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    next();
                }
            }
            case 616 -> {
                if (stageTick >= 40) {
                    shot("g4_double_topdown");
                    client.player.setPitch(0.0F);
                    client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
                    next();
                }
            }
            case 617 -> {
                if (stageTick >= 30) {
                    shot("g5_double_front");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"mandalorian\",switch_hilt:\"mandalorian\",hilt:\"mandalorian\","
                            + "pommel_hilt:\"mandalorian\",color:65280,double:false,focusing:4}] 1");
                    next();
                }
            }
            case 618 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    shot("g6_single_active_front");
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    client.player.setPitch(0.0F);
                    next();
                }
            }
            case 619 -> {
                if (stageTick >= 30) {
                    shot("g7_fp_sanity");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber");
                    command("item replace entity @s container.1 with lightsabers:lightsaber_blade_emitter[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.2 with lightsabers:lightsaber_switch_module[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.3 with lightsabers:lightsaber_grip[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.4 with lightsabers:lightsaber_pommel[lightsabers:hilt={hilt:\"mandalorian\"}]");
                    command("item replace entity @s container.5 with lightsabers:lightsaber_blade_emitter[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.6 with lightsabers:lightsaber_switch_module[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.7 with lightsabers:lightsaber_grip[lightsabers:hilt={hilt:\"grafx\"}]");
                    command("item replace entity @s container.8 with lightsabers:lightsaber_pommel[lightsabers:hilt={hilt:\"grafx\"}]");
                    log("staged 8 part icons (mando+grafx)");
                    next();
                }
            }
            case 620 -> {
                if (stageTick >= 20) {
                    mc().setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc().player));
                    ItemRenderers.guiPoseProbe = -1;
                    next();
                }
            }
            case 621 -> {
                if (stageTick >= 35) {
                    shot("g8_icons_new");
                    ItemRenderers.guiPoseProbe = 0;
                    next();
                }
            }
            case 622 -> {
                if (stageTick >= 35) {
                    shot("g9_icons_old_v3935");
                    ItemRenderers.guiPoseProbe = -1;
                    mc().setScreen(null);
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    client.player.setPitch(0.0F);
                    next();
                }
            }
            case 623 -> {
                if (stageTick >= 25) {
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    log("scenes complete (v3.9.36 legacy TP + part icons)");
                    next();
                    mc().scheduleStop();
                }
            }
            case 624 -> {
                if (stageTick >= 10) {
                    close();
                    command("gamerule sendCommandFeedback false");
                    command("gamerule logAdminCommands false");
                    command("weather clear");
                    command("time set noon");
                    command("kill @e[type=!minecraft:player]");
                    command("tp @s 30.5 170 30.5 180 0");
                    command("gamemode survival");
                    command("clear @s");
                    command("give @s lightsabers:double_lightsaber");
                    log("front top-down double (both staff halves visible)");
                    next();
                }
            }
            case 625 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.DOUBLE_LIGHTSABER);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    client.player.setPitch(-70.0F);
                    setTpProbe(null);
                    next();
                }
            }
            case 626 -> {
                if (stageTick >= 40) {
                    shot("g10_double_front_topdown");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber");
                    next();
                }
            }
            case 627 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    client.player.setPitch(-70.0F);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    next();
                }
            }
            case 628 -> {
                if (stageTick >= 40) {
                    shot("g11_single_front_topdown");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    log("scenes complete (front top-down section)");
                    next();
                    mc().scheduleStop();
                }
            }
            case 630 -> {
                if (stageTick >= 10) {
                    close();
                    command("gamerule sendCommandFeedback false");
                    command("gamerule logAdminCommands false");
                    command("gamerule announceAdvancements false");
                    command("weather clear");
                    command("time set noon");
                    command("fill 24 169 24 38 169 38 smooth_stone");
                    command("fill 24 170 24 38 182 38 air");
                    command("kill @e[type=!minecraft:player]");
                    command("tp @s 30.5 170 30.5 180 0");
                    client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
                    command("gamemode survival");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"mandalorian\",switch_hilt:\"mandalorian\",hilt:\"mandalorian\","
                            + "pommel_hilt:\"mandalorian\",color:65280,double:false,focusing:0}] 1");
                    log("h-series: TP guard + swing tests");
                    next();
                }
            }
            case 631 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    client.player.setPitch(0.0F);
                    next();
                }
            }
            case 632 -> {
                if (stageTick == 30) {
                    shot("h1_tp_guard_standing");
                    client.player.swingHand(Hand.MAIN_HAND);
                    log("single swing triggered, capture at swing 0.5");
                } else if (stageTick >= 33) {
                    var p = client.player;
                    log("capture h2: handSwinging=" + p.handSwinging + " swingProgress="
                            + p.getHandSwingProgress(0.5F) + " swingTicks=" + p.handSwingTicks);
                    shot("h2_tp_single_swing_mid");
                    command("clear @s");
                    command("give @s lightsabers:double_lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"mandalorian\",switch_hilt:\"mandalorian\",hilt:\"mandalorian\","
                            + "pommel_hilt:\"mandalorian\",color:65280,double:true,focusing:0}] 1");
                    next();
                }
            }
            case 633 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.DOUBLE_LIGHTSABER);
                    client.player.setPitch(0.0F);
                    next();
                }
            }
            case 634 -> {
                if (stageTick == 30) {
                    shot("h3_tp_double_guard");
                    client.player.swingHand(Hand.MAIN_HAND);
                } else if (stageTick >= 33) {
                    shot("h4_tp_double_swing_mid");
                    mc().setScreen(new com.drag0nge0de.lightsabers.client.screen.LightsaberConfigScreen(null));
                    next();
                }
            }
            case 635 -> {
                if (stageTick >= 25) {
                    shot("h5_config_screen");
                    mc().setScreen(null);
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    client.player.setPitch(0.0F);
                    command("clear @s");
                    command("give @s lightsabers:double_lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"mandalorian\",switch_hilt:\"mandalorian\",hilt:\"mandalorian\","
                            + "pommel_hilt:\"mandalorian\",color:65280,double:true,focusing:0}] 1");
                    next();
                }
            }
            case 636 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.DOUBLE_LIGHTSABER);
                    com.drag0nge0de.lightsabers.client.render.ItemRenderers.fpAnimApplied = 0;
                    next();
                }
            }
            case 637 -> {
                if (stageTick == 20) {
                    client.player.swingHand(Hand.MAIN_HAND);
                } else if (stageTick >= 23) {
                    shot("h6_fp_double_swing_mid");
                    assertEq("h6 FP mainhand double animates (applied="
                            + com.drag0nge0de.lightsabers.client.render.ItemRenderers.fpAnimApplied + ")",
                            com.drag0nge0de.lightsabers.client.render.ItemRenderers.fpAnimApplied > 0);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    command("clear @s");
                    command("give @s lightsabers:lightsaber");
                    next();
                }
            }
            case 638 -> {
                if (stageTick >= 10) {
                    close();
                    command("gamerule sendCommandFeedback false");
                    command("clear @s");
                    command("give @s lightsabers:lightsaber");
                    log("attr test: expect damage=" + System.getProperty("al.expectdmg", "8.0")
                            + " speed=" + System.getProperty("al.expectspeed", "2.0"));
                    next();
                }
            }
            case 639 -> {
                if (stageTick >= 30) {
                    selectItem(ALItems.LIGHTSABER);
                    next();
                }
            }
            case 640 -> {
                if (stageTick >= 40) {
                    double dmg = client.player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE).getValue();
                    double spd = client.player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED).getValue();
                    double wantDmg = Double.parseDouble(System.getProperty("al.expectdmg", "8.0"));
                    double wantSpd = Double.parseDouble(System.getProperty("al.expectspeed", "2.0"));
                    assertEq("saber attack speed -> " + spd + " want " + wantSpd, Math.abs(spd - wantSpd) < 0.01);
                    command("gamerule sendCommandFeedback true");
                    command("altest attrs " + wantDmg);
                    next();
                }
            }
            case 641 -> {
                if (stageTick >= 20) {
                    command("altest spacing " + System.getProperty("al.expectspacing", "64"));
                    command("force structure locate crystalcave");
                    next();
                }
            }
            case 642 -> {
                if (stageTick >= 120) {
                    shot("h7_locate_result");
                    log("h-series done, entering offhand animation k-series");
                    next();
                }
            }
            case 643 -> {
                if (stageTick >= 10) {
                    close();
                    command("clear @s");
                    command("item replace entity @s weapon.offhand with lightsabers:double_lightsaber"
                            + "[lightsabers:lightsaber={active:true,emitter_hilt:\"mandalorian\","
                            + "switch_hilt:\"mandalorian\",hilt:\"mandalorian\",pommel_hilt:\"mandalorian\","
                            + "color:65280,double:true,focusing:0}] 1");
                    command("gamemode survival");
                    command("fill 24 169 24 38 169 38 smooth_stone");
                    command("fill 24 170 24 38 182 38 air");
                    command("tp @s 30.5 170 30.5 180 0");
                    client.getTutorialManager().setStep(net.minecraft.client.tutorial.TutorialStep.NONE);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    client.player.setPitch(-10.0F);
                    log("k1: offhand double saber only, swing mainhand (empty), expect no animation on it");
                    next();
                }
            }
            case 644 -> {
                if (stageTick == 10) {
                    com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimApplied = 0;
                    com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimSkippedWhileSwinging = 0;
                    com.drag0nge0de.lightsabers.client.render.ItemRenderers.fpAnimApplied = 0;
                } else if (stageTick == 20 || stageTick == 30 || stageTick == 40) {
                    client.player.swingHand(Hand.MAIN_HAND);
                } else if (stageTick >= 50) {
                    shot("k1_offhand_double_swing_static");
                    assertEq("k1 offhand saber never animates (applied="
                            + com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimApplied + ")",
                            com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimApplied == 0);
                    assertEq("k1 offhand saber rendered during swing (skipped="
                            + com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimSkippedWhileSwinging + ")",
                            com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimSkippedWhileSwinging > 0);
                    next();
                }
            }
            case 645 -> {
                if (stageTick >= 10) {
                    command("clear @s");
                    command("give @s lightsabers:double_lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"mandalorian\",switch_hilt:\"mandalorian\",hilt:\"mandalorian\","
                            + "pommel_hilt:\"mandalorian\",color:65280,double:true,focusing:0}] 1");
                    log("k2: mainhand double saber only, swing, expect animation applied");
                    next();
                }
            }
            case 646 -> {
                if (stageTick == 10) {
                    selectItem(ALItems.DOUBLE_LIGHTSABER);
                    com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimApplied = 0;
                    com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimSkippedWhileSwinging = 0;
                } else if (stageTick == 20 || stageTick == 30 || stageTick == 40) {
                    client.player.swingHand(Hand.MAIN_HAND);
                } else if (stageTick >= 50) {
                    shot("k2_mainhand_double_swing_spin");
                    assertEq("k2 mainhand saber animates (applied="
                            + com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimApplied + ")",
                            com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimApplied > 0);
                    assertEq("k2 nothing skipped while swinging (skipped="
                            + com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimSkippedWhileSwinging + ")",
                            com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimSkippedWhileSwinging == 0);
                    next();
                }
            }
            case 647 -> {
                if (stageTick >= 10) {
                    command("clear @s");
                    command("give @s lightsabers:lightsaber[lightsabers:lightsaber={active:true,"
                            + "emitter_hilt:\"mandalorian\",switch_hilt:\"mandalorian\",hilt:\"mandalorian\","
                            + "pommel_hilt:\"mandalorian\",color:16711680,double:false,focusing:0}] 1");
                    command("item replace entity @s weapon.offhand with lightsabers:double_lightsaber"
                            + "[lightsabers:lightsaber={active:true,emitter_hilt:\"mandalorian\","
                            + "switch_hilt:\"mandalorian\",hilt:\"mandalorian\",pommel_hilt:\"mandalorian\","
                            + "color:65280,double:true,focusing:0}] 1");
                    log("k3: mainhand single + offhand double, swing, only mainhand animates");
                    next();
                }
            }
            case 648 -> {
                if (stageTick == 10) {
                    selectItem(ALItems.LIGHTSABER);
                    com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimApplied = 0;
                    com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimSkippedWhileSwinging = 0;
                } else if (stageTick == 20 || stageTick == 30 || stageTick == 40) {
                    client.player.swingHand(Hand.MAIN_HAND);
                } else if (stageTick >= 50) {
                    shot("k3_dual_wield_swing_mainhand_only");
                    assertEq("k3 mainhand saber animates (applied="
                            + com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimApplied + ")",
                            com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimApplied > 0);
                    assertEq("k3 offhand double saber stays static (skipped="
                            + com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimSkippedWhileSwinging + ")",
                            com.drag0nge0de.lightsabers.client.render.ItemRenderers.tpAnimSkippedWhileSwinging > 0);
                    next();
                }
            }
            case 649 -> {
                if (stageTick >= 10) {
                    command("clear @s");
                    command("item replace entity @s weapon.offhand with lightsabers:double_lightsaber"
                            + "[lightsabers:lightsaber={active:true,emitter_hilt:\"mandalorian\","
                            + "switch_hilt:\"mandalorian\",hilt:\"mandalorian\",pommel_hilt:\"mandalorian\","
                            + "color:65280,double:true,focusing:0}] 1");
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                    client.player.setPitch(0.0F);
                    log("k4: FP offhand double saber only, swing mainhand (empty), expect no animation");
                    next();
                }
            }
            case 650 -> {
                if (stageTick == 10) {
                    com.drag0nge0de.lightsabers.client.render.ItemRenderers.fpAnimApplied = 0;
                } else if (stageTick == 20 || stageTick == 30 || stageTick == 40) {
                    client.player.swingHand(Hand.MAIN_HAND);
                } else if (stageTick >= 50) {
                    shot("k4_fp_offhand_swing_static");
                    assertEq("k4 FP offhand saber never animates (applied="
                            + com.drag0nge0de.lightsabers.client.render.ItemRenderers.fpAnimApplied + ")",
                            com.drag0nge0de.lightsabers.client.render.ItemRenderers.fpAnimApplied == 0);
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    next();
                }
            }
            case 651 -> {
                if (stageTick >= 30) {
                    shot("k5_final");
                    log("ASSERTIONS:\n" + assertions + "DONE");
                    log("scenes complete (v3.10.1 offhand animation gate)");
                    next();
                    mc().scheduleStop();
                }
            }
            default -> {
            }
        }
    }

    private void next() {
        stage++;
        stageTick = 0;
        waitUntil = System.currentTimeMillis() + 300;
        stageDeadline = System.currentTimeMillis() + 25000;
        if (stopAt > 0 && stage >= stopAt) {
            log("stopat " + stopAt + " reached, stopping client");
            mc().scheduleStop();
        }
    }

    private void respawnIfNeeded() {
        var client = mc();
        if (client.player != null && client.player.isDead()) {
            client.player.requestRespawn();
            log("respawn requested");
        }
    }

    private final java.util.ArrayDeque<String> commandQueue = new java.util.ArrayDeque<>();

    private void command(String cmd) {

        commandQueue.add(cmd);
        log("queued: /" + cmd);
    }

    private void drainCommandQueue() {
        var client = mc();
        if (!commandQueue.isEmpty() && client.player != null) {
            String cmd = commandQueue.poll();
            client.player.networkHandler.sendChatCommand(cmd);
        }
    }

    private void useBlock(BlockPos pos) {
        var client = mc();
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).subtract(0, 0.5, 0),
                Direction.UP, pos, false);
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
        client.player.swingHand(Hand.MAIN_HAND);
        log("used block " + pos);
    }

    private void close() {
        var client = mc();
        if (client.player != null && client.player.currentScreenHandler != null
                && client.player.currentScreenHandler != client.player.playerScreenHandler) {
            client.player.closeHandledScreen();
            log("closed screen");
        }
    }

    private boolean selectItem(net.minecraft.item.Item item) {
        var client = mc();
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() == item) {
                client.player.getInventory().selectedSlot = i;
                log("selected " + Text.translatable(item.getTranslationKey()).getString()
                        + " (hotbar " + i + ")");
                return true;
            }
        }
        log("ITEM NOT IN HOTBAR: " + item);
        return false;
    }

    private void selectEmptySlot() {
        var client = mc();
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isEmpty()) {
                client.player.getInventory().selectedSlot = i;
                log("selected empty hotbar slot " + i);
                return;
            }
        }
        log("NO EMPTY HOTBAR SLOT");
    }

    private void shiftClickFirst(net.minecraft.item.Item item) {
        var client = mc();
        var handler = client.player.currentScreenHandler;
        for (int i = 9; i < handler.slots.size(); i++) {
            if (handler.slots.get(i).getStack().isOf(item)) {
                client.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE,
                        client.player);
                log("shift-clicked " + item + " from handler slot " + i);
                return;
            }
        }
        log("shiftClickFirst: no " + item + " found");
    }

    private boolean forgeAssembly() {
        var check = mc().player.currentScreenHandler;
        boolean allFilled = true;

        for (int i = 0; i < 6; i++) {
            if (check.getStacks().get(i).isEmpty()) {
                allFilled = false;
            }
        }

        if (allFilled) {
            return true;
        }

        shiftClickForgeItem(ALItems.CIRCUITRY);
        shiftClickForgeItem(ALItems.KYBER_CRYSTAL);
        shiftClickForgeItem(ALItems.EMITTER);
        shiftClickForgeItem(ALItems.SWITCH_MODULE);
        shiftClickForgeItem(ALItems.GRIP);
        shiftClickForgeItem(ALItems.POMMEL);
        var handler = mc().player.currentScreenHandler;
        boolean filled = true;

        for (int i = 0; i < 6; i++) {
            log("forge slot " + i + " = " + handler.getStacks().get(i).getItem());

            if (handler.getStacks().get(i).isEmpty()) {
                filled = false;
            }
        }

        return filled;
    }

    private void shiftClickForgeItem(net.minecraft.item.Item item) {
        var client = mc();
        var handler = client.player.currentScreenHandler;

        for (int i = 9; i < handler.slots.size(); i++) {
            ItemStack s = handler.slots.get(i).getStack();

            if (s.getItem() == item) {
                client.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE,
                        client.player);
                log("shift-clicked " + item + " into the forge");
                return;
            }
        }

        log("no " + item + " in player slots");
    }

    private void placeIntoForge(net.minecraft.item.Item item, int forgeSlot) {
        var client = mc();
        var handler = client.player.currentScreenHandler;
        for (int i = 9; i < handler.slots.size(); i++) {
            ItemStack s = handler.slots.get(i).getStack();
            if (s.getItem() == item) {
                client.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(handler.syncId, forgeSlot, 0, SlotActionType.PICKUP, client.player);
                log("placed " + item + " into forge slot " + forgeSlot);
                return;
            }
        }
        log("no " + item + " in player slots");
    }

    private void takeOutput() {
        var client = mc();
        var handler = client.player.currentScreenHandler;
        client.interactionManager.clickSlot(handler.syncId, 8, 0, SlotActionType.QUICK_MOVE, client.player);
        log("shift-clicked forge output");
    }

    private void pouchShiftClickTest() {
        var client = mc();
        var handler = client.player.currentScreenHandler;
        shiftClickFirst(s -> s.contains(ALComponents.FOCUSING)
                && s.get(ALComponents.FOCUSING).type() == FocusingCrystalType.CRACKED);
        shiftClickFirst(s -> s.isOf(ALItems.KYBER_CRYSTAL));
        boolean crackedIn = false;
        boolean kyberIn = false;
        for (int i = 0; i < 18; i++) {
            ItemStack s = handler.slots.get(i).getStack();
            if (s.contains(ALComponents.FOCUSING)
                    && s.get(ALComponents.FOCUSING).type() == FocusingCrystalType.CRACKED) {
                crackedIn = true;
            }
            if (s.isOf(ALItems.KYBER_CRYSTAL)) {
                kyberIn = true;
            }
        }
        assertEq("cracked focusing crystal shift-clicks into the pouch", crackedIn);
        assertEq("kyber crystal shift-clicks into the pouch", kyberIn);
    }

    private void shiftClickFirst(java.util.function.Predicate<ItemStack> pred) {
        var client = mc();
        var handler = client.player.currentScreenHandler;
        for (int i = 18; i < handler.slots.size(); i++) {
            if (pred.test(handler.slots.get(i).getStack())) {
                client.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE,
                        client.player);
                log("shift-clicked matching stack from handler slot " + i);
                return;
            }
        }
        log("shiftClickFirst(pred): nothing matched");
    }

    private void dropPouchFromGui() {
        var client = mc();
        var handler = client.player.currentScreenHandler;
        for (int i = 45; i < handler.slots.size(); i++) {
            if (handler.slots.get(i).getStack().getItem() == ALItems.CRYSTAL_POUCH) {
                client.interactionManager.clickSlot(handler.syncId, i, 1, SlotActionType.THROW,
                        client.player);
                log("threw pouch from handler slot " + i);
                return;
            }
        }
        log("no pouch in hotbar section");
    }

    private int countPorkchops() {
        return countItems(Items.PORKCHOP);
    }

    private int countItems(net.minecraft.item.Item item) {
        var client = mc();
        int n = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack s = client.player.getInventory().getStack(i);
            if (s.getItem() == item) {
                n += s.getCount();
            }
        }
        return n;
    }

    private void shot(String name) {
        var client = mc();
        try {
            new java.io.File(client.runDirectory, "screenshots/shots").mkdirs();
            ScreenshotRecorder.saveScreenshot(client.runDirectory, "shots/" + name + ".png",
                    client.getFramebuffer(), t -> log("saved " + name));
        } catch (Exception e) {
            log("screenshot " + name + " FAILED: " + e);
        }
    }

    private void assertDesc(String label, String powerName, String expected) {
        assertDesc(label, powerName, expected, -1);
    }

    private void assertDesc(String label, String powerName, String expected, int index) {
        com.drag0nge0de.lightsabers.force.Power power = com.drag0nge0de.lightsabers.force.Power.byName(powerName);
        if (power == null) {
            assertEq("desc " + label + ": power missing", false);
            return;
        }

        java.util.List<Text> lines = com.drag0nge0de.lightsabers.force.PowerDescriptions.lines(power);
        if (index >= 0) {
            String got = index < lines.size() ? lines.get(index).getString() : "<missing>";
            assertEq("desc " + label + " = '" + expected + "'", expected.equals(got));
        } else {
            boolean found = false;
            for (Text line : lines) {
                if (expected.equals(line.getString())) {
                    found = true;
                }
            }
            assertEq("desc " + label + " contains '" + expected + "'", found);
        }
    }

    private static void setProbe(float[] p) {
        try {
            Class<?> cls = Class.forName("com.drag0nge0de.lightsabers.client.render.ItemRenderers");
            java.lang.reflect.Field f = cls.getField("fpProbe");
            f.set(null, p);
            log("fpProbe = " + (p == null ? "null" : java.util.Arrays.toString(p)));
        } catch (Exception e) {
            log("fpProbe set FAILED: " + e);
        }
    }

    private static void setTpProbe(float[] p) {
        try {
            Class<?> cls = Class.forName("com.drag0nge0de.lightsabers.client.render.ItemRenderers");
            java.lang.reflect.Field f = cls.getField("tpProbe");
            f.set(null, p);
            log("tpProbe = " + (p == null ? "null" : java.util.Arrays.toString(p)));
        } catch (Exception e) {
            log("tpProbe set FAILED: " + e);
        }
    }

    private static void setTpCorrection(boolean on) {
        try {
            Class<?> cls = Class.forName("com.drag0nge0de.lightsabers.client.render.ItemRenderers");
            java.lang.reflect.Field f = cls.getField("tpGripCorrection");
            f.set(null, on);
            log("tpGripCorrection = " + on);
        } catch (Exception e) {
            log("tpGripCorrection set FAILED: " + e);
        }
    }

    private static Object keyBinding(String which) throws Exception {
        java.lang.reflect.Field f = Class.forName("com.drag0nge0de.lightsabers.ALClient").getDeclaredField(which);
        f.setAccessible(true);
        return f.get(null);
    }

    private static void setKeyField(Object binding, String name, Object value) throws Exception {
        java.lang.Class<?>[] classes = {binding.getClass().getSuperclass(), binding.getClass()};
        for (java.lang.Class<?> c : classes) {
            if (c == null || c == Object.class) {
                continue;
            }
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (f.getName().equals(name)) {
                    f.setAccessible(true);
                    f.set(binding, value);
                    return;
                }
            }
        }

        java.lang.Class<?> want = value instanceof Boolean ? boolean.class
                : value instanceof Integer ? int.class : null;
        if (want != null) {
            for (java.lang.reflect.Field f : binding.getClass().getDeclaredFields()) {
                if (f.getType() == want) {
                    f.setAccessible(true);
                    f.set(binding, value);
                    return;
                }
            }
        }

        throw new IllegalStateException("KeyBinding field " + name + " not found");
    }

    private void pressOnce(String which) {
        try {
            Object binding = keyBinding(which);
            setKeyField(binding, "timesPressed", 1);
            setKeyField(binding, "pressed", true);
            pendingRelease = which;
            log("simulated key tap: " + which);
        } catch (Exception e) {
            assertEq("keybind simulation failed for " + which + ": " + e, false);
        }
    }

    private void setKey(String which, boolean down) {
        try {
            Object binding = keyBinding(which);
            setKeyField(binding, "pressed", down);
            if (!down) {
                setKeyField(binding, "timesPressed", 0);
            }
            log("key " + which + " " + (down ? "held" : "released"));
        } catch (Exception e) {
            assertEq("keybind simulation failed for " + which + ": " + e, false);
        }
    }
}
