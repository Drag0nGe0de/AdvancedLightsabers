package com.drag0nge0de.lightsabers.command;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.block.LightsaberForgeBlock;
import com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock;
import com.drag0nge0de.lightsabers.block.blockentity.SithStoneCoffinBlockEntity;
import com.drag0nge0de.lightsabers.entity.SithGhostEntity;
import com.drag0nge0de.lightsabers.force.ForceManager;
import com.drag0nge0de.lightsabers.force.ForceEffects;
import com.drag0nge0de.lightsabers.force.ForcePowers;
import com.drag0nge0de.lightsabers.force.ForceState;
import com.drag0nge0de.lightsabers.force.Power;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.FocusingCrystalType;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.item.FocusingCrystalItem;
import com.drag0nge0de.lightsabers.item.PartItem;
import com.drag0nge0de.lightsabers.screen.LightsaberForgeScreenHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.Rarity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Heightmap;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ALTestCommand {
   private static final String BOT_NAME = "ForceBot";
   private static ServerPlayerEntity bot;

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(ALTestCommand::coffinTickHook);
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            CommandManager.literal("altest")
               .requires(source -> source.hasPermissionLevel(2))
               .then(CommandManager.literal("tree").executes(ctx -> {
                  ((ServerCommandSource)ctx.getSource()).sendFeedback(() -> assertTree(), false);
                  return 1;
               }))
               .then(CommandManager.literal("forge").executes(ctx -> {
                  ServerCommandSource src = (ServerCommandSource)ctx.getSource();

                  try {
                     Text result = forgeTest(src);
                     src.sendFeedback(() -> result, false);
                  } catch (CommandSyntaxException e) {
                     src.sendError(Text.literal("altest forge needs a player: " + e.getMessage()));
                  }

                  return 1;
               }))
               .then(CommandManager.literal("station").executes(ctx -> {
                  ServerCommandSource src = (ServerCommandSource)ctx.getSource();

                  try {
                     Text result = stationTest(src);
                     src.sendFeedback(() -> result, false);
                  } catch (CommandSyntaxException e) {
                     src.sendError(Text.literal("altest station needs a player: " + e.getMessage()));
                  }

                  return 1;
               }))
               .then(CommandManager.literal("forgeshape").executes(ctx -> {
                  ServerCommandSource src = (ServerCommandSource)ctx.getSource();
                  src.sendFeedback(() -> forgeShapeTest(src), false);

                  return 1;
               }))
               .then(CommandManager.literal("forgelone").executes(ctx -> {
                  ServerCommandSource src = (ServerCommandSource)ctx.getSource();
                  src.sendFeedback(() -> forgeLoneTest(src), false);

                  return 1;
               }))
               .then(CommandManager.literal("stairs").executes(ctx -> {
                  ServerCommandSource src = (ServerCommandSource)ctx.getSource();
                  src.sendFeedback(() -> stairProbe(src), false);

                  return 1;
               }))
               .then(CommandManager.literal("crystalnames").executes(ctx -> {
                  ServerCommandSource src = (ServerCommandSource)ctx.getSource();
                  src.sendFeedback(() -> crystalNameProbe(src), false);

                  return 1;
               }))
               .then(CommandManager.literal("attrs")
                  .then(CommandManager.argument("wantDamage",
                              com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg())
                     .executes(ctx -> {
                        ServerCommandSource src = (ServerCommandSource)ctx.getSource();
                        ServerPlayerEntity player = src.getPlayer();
                        double want = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "wantDamage");
                        double got = player.getAttributeInstance(
                              net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE).getValue();
                        boolean ok = Math.abs(got - want) < 0.01;
                        src.sendFeedback(() -> Text.literal(
                              (ok ? "PASS: " : "FAIL: ") + "server damage " + got + " want " + want), false);
                        return ok ? 1 : 0;
                     })))
               .then(CommandManager.literal("spacing")
                  .then(CommandManager.argument("want", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                     .executes(ctx -> {
                        ServerCommandSource src = (ServerCommandSource)ctx.getSource();
                        net.minecraft.registry.entry.RegistryEntry<net.minecraft.structure.StructureSet> setEntry =
                              src.getServer().getRegistryManager()
                                 .get(net.minecraft.registry.RegistryKeys.STRUCTURE_SET)
                                 .getEntry(AL.id("jedi_temples"))
                                 .orElse(null);
                        if (setEntry == null) {
                           src.sendError(Text.literal("FAIL: jedi_temples structure set missing"));
                           return 0;
                        }

                        net.minecraft.world.gen.chunk.placement.StructurePlacement placement =
                              setEntry.value().placement();
                        if (!(placement instanceof net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement spread)) {
                           src.sendError(Text.literal("FAIL: jedi_temples placement is not random_spread"));
                           return 0;
                        }

                        spread.getStartChunk(src.getWorld().getSeed(), 0, 0);
                        int got = spread.getSpacing();
                        int want = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "want");
                        boolean ok = got == want;
                        src.sendFeedback(() -> Text.literal(
                              (ok ? "PASS: " : "FAIL: ") + "jedi temple spacing " + got + " want " + want), false);
                        return ok ? 1 : 0;
                     })))
               .then(CommandManager.literal("craftdouble").executes(ctx -> {
                  ServerCommandSource src = (ServerCommandSource)ctx.getSource();
                  src.sendFeedback(() -> craftDoubleProbe(), false);

                  return 1;
               }))
               .then(CommandManager.literal("bot")
                  .then(CommandManager.literal("spawn").executes(ctx -> spawnBot((ServerCommandSource)ctx.getSource())))
                  .then(CommandManager.literal("despawn").executes(ctx -> despawnBot((ServerCommandSource)ctx.getSource())))
                  .then(CommandManager.literal("unlockall").executes(ctx -> unlockAll((ServerCommandSource)ctx.getSource())))
                  .then(CommandManager.literal("cast").executes(ctx -> castAll((ServerCommandSource)ctx.getSource())))
                  .then(CommandManager.literal("channel").executes(ctx -> channelProbe((ServerCommandSource)ctx.getSource())))
                  .then(CommandManager.literal("channelsustain").executes(ctx -> channelSustain((ServerCommandSource)ctx.getSource())))
                  .then(CommandManager.literal("info").executes(ctx -> botInfo((ServerCommandSource)ctx.getSource())))
                  .then(CommandManager.literal("coffin").executes(ctx -> runCoffinTest((ServerCommandSource)ctx.getSource()))))
         ));
   }

   private static Text craftDoubleProbe() {
      List<String> lines = new ArrayList<>();
      com.drag0nge0de.lightsabers.recipe.SaberAssemblyRecipe recipe = new com.drag0nge0de.lightsabers.recipe.SaberAssemblyRecipe();

      ItemStack mando = com.drag0nge0de.lightsabers.item.LightsaberItem.createSaber(
            com.drag0nge0de.lightsabers.hilt.Hilt.MANDALORIAN, 0xFFFFFF, false);
      ItemStack knighted = com.drag0nge0de.lightsabers.item.LightsaberItem.createSaber(
            com.drag0nge0de.lightsabers.hilt.Hilt.KNIGHTED, 0xFF0000, false);

      ItemStack mixed = new ItemStack(ALItems.LIGHTSABER);
      mixed.set(ALComponents.LIGHTSABER, new LightsaberComponent(false, "knighted", "mauler", "mandalorian", "rebel", 0x59B9FF, false, 0));

      ItemStack plain = new ItemStack(ALItems.LIGHTSABER);
      ItemStack active = com.drag0nge0de.lightsabers.item.LightsaberItem.createSaber(
            com.drag0nge0de.lightsabers.hilt.Hilt.MANDALORIAN, 0xFFFFFF, false);
      com.drag0nge0de.lightsabers.item.LightsaberItem.setActive(active, true);

      craftDoubleCase(lines, recipe, "preset + exact copy (middle-click copy)", mando, mando.copy());
      craftDoubleCase(lines, recipe, "mixed-part forge saber + exact copy", mixed, mixed.copy());
      craftDoubleCase(lines, recipe, "two different presets", mando, knighted);
      craftDoubleCase(lines, recipe, "default stacks (no explicit component)", plain, plain.copy());
      craftDoubleCase(lines, recipe, "active saber + copy", active, active.copy());
      craftDoubleCase(lines, recipe, "mixed saber (flipped order)", mixed.copy(), mixed);

      for (String line : lines) {
         if (line.contains("FAIL")) {
            return Text.literal("craftdouble FAILURES: " + String.join(" | ", lines));
         }
      }
      return Text.literal("craftdouble: all " + lines.size() + " cases keep both sabers' full blade data");
   }

   private static void craftDoubleCase(List<String> lines, com.drag0nge0de.lightsabers.recipe.SaberAssemblyRecipe recipe,
         String name, ItemStack upper, ItemStack lower) {
      try {
         List<ItemStack> grid = new ArrayList<>();

         for (int i = 0; i < 9; i++) {
            grid.add(ItemStack.EMPTY);
         }

         grid.set(1, upper);
         grid.set(4, lower);
         net.minecraft.recipe.input.CraftingRecipeInput input = net.minecraft.recipe.input.CraftingRecipeInput.create(3, 3, grid);

         if (!recipe.matches(input, null)) {
            lines.add(name + ": FAIL no recipe match");
            return;
         }

         ItemStack result = recipe.craft(input, null);

         if (!result.isOf(ALItems.DOUBLE_LIGHTSABER)) {
            lines.add(name + ": FAIL wrong result item " + result.getItem());
            return;
         }

         LightsaberComponent u = com.drag0nge0de.lightsabers.item.LightsaberItem.getComponent(upper);
         LightsaberComponent l = com.drag0nge0de.lightsabers.item.LightsaberItem.getComponent(lower);
         LightsaberComponent c = com.drag0nge0de.lightsabers.item.LightsaberItem.getComponent(result);
         boolean okMain = c.emitterHilt().equals(u.emitterHilt()) && c.switchHilt().equals(u.switchHilt())
               && c.gripHilt().equals(u.gripHilt()) && c.pommelHilt().equals(u.pommelHilt())
               && c.color() == u.color() && c.doubleSaber() && !c.active();
         boolean okSecond = c.second().isPresent();

         if (okSecond) {
            LightsaberComponent.Blade b = c.second().get();
            okSecond = b.emitterHilt().equals(l.emitterHilt()) && b.switchHilt().equals(l.switchHilt())
                  && b.gripHilt().equals(l.gripHilt()) && b.pommelHilt().equals(l.pommelHilt())
                  && b.color() == l.color();
         }

         if (okMain && okSecond) {
            lines.add(name + ": PASS");
         } else {
            lines.add(name + ": FAIL main=" + c.emitterHilt() + "/" + c.switchHilt() + "/" + c.gripHilt()
                  + "/" + c.pommelHilt() + " color=" + Integer.toHexString(c.color())
                  + " second=" + c.second().map(Object::toString).orElse("absent"));
         }
      } catch (Exception e) {
         lines.add(name + ": FAIL exception " + e);
      }
   }

   private static Text crystalNameProbe(ServerCommandSource source) {
      List<String> lines = new ArrayList<>();
      lines.add(nameString("create deep blue (common)", com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(CrystalColor.DEEP_BLUE), "Deep Blue (Common)"));
      lines.add(nameString("create magenta (uncommon)", com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(CrystalColor.MAGENTA), "Magenta (Uncommon)"));
      lines.add(nameString("create indigo (rare)", com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(CrystalColor.INDIGO), "Indigo (Rare)"));
      lines.add(nameString("create purple (rare)", com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(CrystalColor.PURPLE), "Purple (Rare)"));
      lines.add(nameString("create arctic blue (epic)", com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(CrystalColor.ARCTIC_BLUE), "Arctic Blue (Epic)"));
      lines.add(nameString("create white (epic)", com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(CrystalColor.WHITE), "White (Epic)"));

      ItemStack legacy = new ItemStack(ALItems.KYBER_CRYSTAL);
      legacy.set(ALComponents.CRYSTAL, new CrystalComponent(CrystalColor.DEEP_BLUE.rgb));
      lines.add(nameString("pre-3.9.18 stack (no RARITY)", legacy, "Deep Blue (Common)"));

      ItemStack tainted = com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(CrystalColor.RED);
      tainted.set(DataComponentTypes.RARITY, net.minecraft.util.Rarity.EPIC);

      try {
         tainted.getItem().inventoryTick(tainted, source.getServer().getOverworld(), bot, 0, false);
      } catch (Exception ignored) {
      }
      lines.add(nameString("stale 3.9.18 RARITY component after repair tick", tainted, "Red (Uncommon)"));

      for (String line : lines) {
         if (line.contains("FAIL")) {
            return Text.literal("crystalnames FAILURES: " + String.join(" | ", lines));
         }
      }
      return Text.literal("crystalnames: all " + lines.size() + " checks agree with the 3.9.20 style (white name, gray color + rarity lore)");
   }

   private static String nameString(String label, ItemStack stack, String expectedLore) {
      List<Text> tooltip = stack.getTooltip(net.minecraft.item.Item.TooltipContext.DEFAULT, null,
            net.minecraft.item.tooltip.TooltipType.BASIC);
      Text nameLine = tooltip.isEmpty() ? stack.getName() : tooltip.get(0);
      String nameColor = nameLine.getStyle().getColor() != null ? nameLine.getStyle().getColor().getName() : "none";
      boolean nameWhite = nameColor.equals("none") || nameColor.equals("white");
      String lore = tooltip.size() > 1 ? tooltip.get(tooltip.size() - 1).getString() : "";
      Text loreLine = tooltip.size() > 1 ? tooltip.get(tooltip.size() - 1) : null;
      String loreColor = loreLine != null && loreLine.getStyle().getColor() != null ? loreLine.getStyle().getColor().getName() : "none";
      Rarity rarity = stack.getOrDefault(DataComponentTypes.RARITY, Rarity.COMMON);
      boolean rarityWhitened = rarity == Rarity.COMMON;

      boolean pass = nameWhite && loreColor.equals("gray") && expectedLore.equals(lore)
            && stack.getName().getString().equals("Kyber Crystal") && rarityWhitened;
      return (pass ? "PASS " : "FAIL ") + label + ": name '" + stack.getName().getString() + "' nameColor=" + nameColor
            + " lore='" + lore + "' loreColor=" + loreColor + " rarity=" + rarity;
   }

   private static Text stairProbe(ServerCommandSource source) {
      ServerWorld world = source.getServer().getOverworld();
      BlockPos center = BlockPos.ofFloored(source.getPosition());
      int r = 48;
      int checked = 0;
      int match = 0;
      List<String> fails = new ArrayList<>();

      for (BlockPos pos : BlockPos.iterate(center.add(-r, -24, -r), center.add(r, 24, r))) {
         BlockState state = world.getBlockState(pos);

         if (!(state.getBlock() instanceof StairsBlock)) {
            continue;
         }

         checked++;
         BlockState recomputed = state.getStateForNeighborUpdate(Direction.NORTH,
               world.getBlockState(pos.north()), world, pos, pos.north());

         if (recomputed.get(StairsBlock.SHAPE) != state.get(StairsBlock.SHAPE)) {
            if (fails.size() < 6) {
               fails.add(pos.toShortString() + " " + state.get(StairsBlock.FACING) + "/"
                     + state.get(StairsBlock.SHAPE) + " want " + recomputed.get(StairsBlock.SHAPE));
            }
         } else {
            match++;
         }
      }

      String head = "altest stairs @" + center.toShortString() + ": " + checked + " stairs, " + match + " shapes match";

      if (fails.isEmpty()) {
         return Text.literal(head + " - all baked shapes agree with vanilla recomputation");
      }

      return Text.literal(head + " FAILURES: " + String.join(" | ", fails));
   }

   private static Text forgeShapeTest(ServerCommandSource source) {
      ServerWorld world = source.getServer().getOverworld();
      BlockPos base = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING,
            world.getSpawnPos().add(8, 0, 0));
      List<String> fails = new ArrayList<>();
      int pass = 0;
      int total = 0;

      for (Block block : new Block[] {ALBlocks.LIGHTSABER_FORGE, ALBlocks.LIGHTSABER_FORGE_DARK}) {
         for (Direction facing : Direction.Type.HORIZONTAL) {
            world.getChunk(base);
            BlockState placed = world.getBlockState(base);

            for (int attempt = 0; attempt < 3; attempt++) {
               world.setBlockState(base, block.getDefaultState().with(LightsaberForgeBlock.FACING, facing));

               try {
                  Thread.sleep(60);
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
               }

               placed = world.getBlockState(base);

               if (placed.isOf(block) && placed.get(LightsaberForgeBlock.FACING) == facing
                     && !LightsaberForgeBlock.isPanel(placed)) {
                  break;
               }
            }

            if (!placed.isOf(block) || placed.get(LightsaberForgeBlock.FACING) != facing) {
               total += 4;
               fails.add(block + " " + facing + " placement failed (chunk not loaded)");
               continue;
            }

            BlockPos panelPos = base.offset(LightsaberForgeBlock.panelDir(placed));
            world.setBlockState(panelPos, block.getDefaultState()
                  .with(LightsaberForgeBlock.FACING, facing).with(LightsaberForgeBlock.PANEL, true));

            for (int half = 0; half < 2; half++) {
               BlockPos pos = half == 0 ? base : panelPos;
               BlockState state = world.getBlockState(pos);
               double minX = switch (facing) {
                  case NORTH -> half == 0 ? 0 : -1;
                  case SOUTH -> half == 0 ? -1 : 0;
                  default -> 0;
               };
               double maxX = switch (facing) {
                  case NORTH -> half == 0 ? 2 : 1;
                  case SOUTH -> half == 0 ? 1 : 2;
                  default -> 1;
               };
               double minZ = switch (facing) {
                  case EAST -> half == 0 ? 0 : -1;
                  case WEST -> half == 0 ? -1 : 0;
                  default -> 0;
               };
               double maxZ = switch (facing) {
                  case EAST -> half == 0 ? 2 : 1;
                  case WEST -> half == 0 ? 1 : 2;
                  default -> 1;
               };

               for (int mode = 0; mode < 2; mode++) {
                  VoxelShape shape = mode == 0
                        ? state.getOutlineShape(world, pos, ShapeContext.absent())
                        : state.getCollisionShape(world, pos, ShapeContext.absent());
                  Box b = shape.getBoundingBox();
                  boolean ok = near(b.minX, minX) && near(b.maxX, maxX) && near(b.minZ, minZ)
                        && near(b.maxZ, maxZ) && near(b.minY, 0) && near(b.maxY, 0.8125);
                  total++;

                  if (ok) {
                     pass++;
                  } else {
                     fails.add((half == 0 ? "main" : "panel") + " " + block + " " + facing + " "
                           + (mode == 0 ? "outline" : "collision")
                           + " box=" + b.minX + "," + b.minY + "," + b.minZ + " to " + b.maxX + ","
                           + b.maxY + "," + b.maxZ);
                  }
               }
            }
         }
      }

      world.setBlockState(base, net.minecraft.block.Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
      world.setBlockState(base.add(1, 0, 0), net.minecraft.block.Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
      world.setBlockState(base.add(-1, 0, 0), net.minecraft.block.Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
      world.setBlockState(base.add(0, 0, 1), net.minecraft.block.Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
      world.setBlockState(base.add(0, 0, -1), net.minecraft.block.Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
      String head = "forgeshape " + pass + "/" + total;

      if (fails.isEmpty()) {
         return Text.literal(head + " both halves span the exact 2-cell footprint in all facings");
      }

      return Text.literal(head + " FAILURES: " + String.join(" | ", fails));
   }

   private static boolean near(double a, double b) {
      return Math.abs(a - b) < 1.0E-9;
   }

   private static Text forgeLoneTest(ServerCommandSource source) {
      ServerWorld world = source.getServer().getOverworld();
      BlockPos base = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING,
            world.getSpawnPos().add(-8, 0, 0));
      List<String> fails = new ArrayList<>();
      int pass = 0;
      int total = 0;

      for (Block block : new Block[] {ALBlocks.LIGHTSABER_FORGE, ALBlocks.LIGHTSABER_FORGE_DARK}) {
         for (Direction facing : Direction.Type.HORIZONTAL) {
            world.getChunk(base);
            world.setBlockState(base, block.getDefaultState().with(LightsaberForgeBlock.FACING, facing),
                  Block.NOTIFY_ALL | Block.FORCE_STATE);
            BlockPos panelPos = base.offset(facing.rotateYClockwise());

            world.setBlockState(panelPos, Blocks.STONE.getDefaultState(), Block.NOTIFY_ALL);
            BlockState afterStone = world.getBlockState(base);
            total++;

            if (afterStone.isOf(block) && !LightsaberForgeBlock.isPanel(afterStone)
                  && afterStone.get(LightsaberForgeBlock.FACING) == facing) {
               pass++;
            } else {
               fails.add("stone " + block + " " + facing + " main=" + afterStone);
            }

            world.setBlockState(panelPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            BlockState afterAir = world.getBlockState(base);
            BlockState panelCell = world.getBlockState(panelPos);
            total++;

            if (afterAir.isOf(block) && !LightsaberForgeBlock.isPanel(afterAir) && panelCell.isAir()) {
               pass++;
            } else {
               fails.add("air " + block + " " + facing + " main=" + afterAir + " panel=" + panelCell);
            }

            world.setBlockState(base, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
         }
      }

      String head = "forgelone " + pass + "/" + total;

      if (fails.isEmpty()) {
         return Text.literal(head + " lone forges never heal or pop from neighbor updates");
      }

      return Text.literal(head + " FAILURES: " + String.join(" | ", fails));
   }

   private static Text forgeTest(ServerCommandSource source) throws CommandSyntaxException {
      ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity p ? p
            : source.getPlayer();
      if (player == null) {

         List<ServerPlayerEntity> online = source.getServer().getPlayerManager().getPlayerList();
         if (online.isEmpty()) {
            return Text.literal("Forge test needs a player or bot online").formatted(Formatting.RED);
         }

         player = online.get(0);
      }

      List<String> results = new ArrayList<>();

      LightsaberForgeScreenHandler handler = new LightsaberForgeScreenHandler(0,
            player.getInventory(), ScreenHandlerContext.EMPTY);
      com.drag0nge0de.lightsabers.hilt.Hilt hilt = com.drag0nge0de.lightsabers.hilt.Hilt.DEFAULT;
      handler.input.setStack(0, PartItem.create("emitter", hilt));
      handler.input.setStack(1, PartItem.create("switch_section", hilt));
      handler.input.setStack(2, PartItem.create("body", hilt));
      handler.input.setStack(3, PartItem.create("pommel", hilt));

      results.add(handler.assembled == null ? "no-crystal guard OK" : "FAIL: assembled without crystal");

      ItemStack kyber = new ItemStack(com.drag0nge0de.lightsabers.registry.ALItems.KYBER_CRYSTAL);
      kyber.set(com.drag0nge0de.lightsabers.registry.ALComponents.CRYSTAL,
            new CrystalComponent(CrystalColor.MEDIUM_BLUE.rgb));
      handler.input.setStack(5, kyber);
      handler.input.setStack(4, new ItemStack(com.drag0nge0de.lightsabers.registry.ALItems.CIRCUITRY));
      handler.input.setStack(6, FocusingCrystalItem.create(FocusingCrystalType.CRACKED));

      boolean ok = false;
      if (handler.assembled != null && !handler.tooShort) {
         ItemStack out = handler.getSlot(8).getStack();
         LightsaberComponent comp = out.get(com.drag0nge0de.lightsabers.registry.ALComponents.LIGHTSABER);
         ok = !out.isEmpty() && comp != null
               && hilt.getId().equals(comp.hilt()) && comp.isHiltUniform()
               && comp.focusing() == FocusingCrystalType.CRACKED.mask();
         boolean assembledOk = ok;
         results.add(assembledOk
               ? "assemble OK (hilt=" + comp.hilt() + ", focusing=" + comp.focusing() + ")"
               : "FAIL: wrong output stack");

         if (assembledOk) {
            handler.getSlot(8).onTakeItem(player, out);
            boolean empty = true;
            for (int i = 0; i < 8; i++) {
               empty &= handler.input.getStack(i).isEmpty();
            }
            boolean inputsEmpty = empty;
            results.add(inputsEmpty ? "take-consumes-inputs OK" : "FAIL: inputs not consumed");
         }
      } else {
         results.add("FAIL: no assembled saber (tooShort=" + handler.tooShort + ")");
      }

      handler.input.setStack(0, PartItem.create("emitter", hilt));
      handler.input.setStack(1, PartItem.create("switch_section",
            com.drag0nge0de.lightsabers.hilt.Hilt.values()[1]));
      handler.input.setStack(2, PartItem.create("body", hilt));
      handler.input.setStack(3, PartItem.create("pommel", hilt));
      handler.input.setStack(5, kyber);
      results.add(handler.assembled == null ? "mixed-hilt guard OK" : "FAIL: mixed hilts assembled");

      boolean allOk = results.stream().noneMatch(r -> r.startsWith("FAIL"));
      return Text.literal(allOk ? "Forge OK: " : "Forge FAILURES: ")
            .append(Text.literal(String.join("; ", results))
                  .formatted(allOk ? Formatting.GREEN : Formatting.RED));
   }

   private static Text stationTest(ServerCommandSource source) throws CommandSyntaxException {
      ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity p ? p
            : source.getPlayer();
      if (player == null) {
         List<ServerPlayerEntity> online = source.getServer().getPlayerManager().getPlayerList();
         if (online.isEmpty()) {
            return Text.literal("Station test needs a player or bot online").formatted(Formatting.RED);
         }

         player = online.get(0);
      }

      List<String> results = new ArrayList<>();
      net.minecraft.server.world.ServerWorld world = player.getServerWorld();
      net.minecraft.util.math.BlockPos pos = player.getBlockPos().add(2, 0, 0);
      net.minecraft.block.BlockState state = com.drag0nge0de.lightsabers.registry.ALBlocks.DISASSEMBLY_STATION.getDefaultState();
      world.setBlockState(pos, state);

      try {
         net.minecraft.block.entity.BlockEntity rawBe = world.getBlockEntity(pos);
         if (!(rawBe instanceof com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity be)) {
            return Text.literal("FAIL: station block entity missing").formatted(Formatting.RED);
         }

         be.setStack(0, com.drag0nge0de.lightsabers.item.LightsaberItem.createSaber(com.drag0nge0de.lightsabers.hilt.Hilt.DEFAULT,
               CrystalColor.MEDIUM_BLUE.rgb, false));
         for (int i = 0; i < 100; i++) {
            com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity.tick(world, pos, state, be);
         }

         results.add(be.progress == 0 ? "no-fuel guard OK" : "FAIL: progressed without fuel");

         be.setStack(1, new ItemStack(net.minecraft.item.Items.REDSTONE, 64));
         boolean disassembled = false;

         for (int i = 0; i < com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity.TICKS_DISASSEMBLY * 3 && !disassembled; i++) {
            com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity.tick(world, pos, state, be);
            disassembled = be.getStack(0).isEmpty();
         }

         if (disassembled) {
            results.add("disassembly cycle OK (" + be.progress + " partial ticks, fuel left "
                  + be.getStack(1).getCount() + ")");

            int parts = 0;
            int other = 0;

            for (int slot = 2; slot < com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity.SIZE; slot++) {
               ItemStack out = be.getStack(slot);
               if (!out.isEmpty()) {
                  if (out.getItem() instanceof com.drag0nge0de.lightsabers.item.PartItem) {
                     parts++;
                  } else {
                     other += out.getCount();
                  }
               }
            }

            results.add(parts == 4 ? "4 hilt parts salvaged OK" : "FAIL: expected 4 parts, got " + parts);
            results.add("bonus rolls: " + other + " extra stack(s) (circuitry 25% / crystal 35%+)");
         } else {
            results.add("FAIL: saber never disassembled");
         }
      } finally {
         world.removeBlock(pos, false);
      }

      boolean allOk = results.stream().noneMatch(r -> r.startsWith("FAIL"));
      return Text.literal(allOk ? "Station OK: " : "Station FAILURES: ")
            .append(Text.literal(String.join("; ", results))
                  .formatted(allOk ? Formatting.GREEN : Formatting.RED));
   }

   private static Text assertTree() {
      List<String> problems = new ArrayList<>();

      for (Power power : Power.POWERS) {
         if (power.parent != null && !Power.POWERS.contains(power.parent)) {
            problems.add(power.getName() + " has missing parent");
         }

         if (power.stats.xpCost < 0) {
            problems.add(power.getName() + " has negative cost");
         }

         if (power.isCastable() && power.stats.useCost <= 0.0F) {
            problems.add(power.getName() + " castable without cost");
         }
      }

      if (Power.byName("forceSensitivity") == null) {
         problems.add("root missing");
      }

      return problems.isEmpty()
         ? Text.literal("Power tree OK: " + Power.POWERS.size() + " powers, " + Power.allCastable().size() + " castable")
            .formatted(Formatting.GREEN)
         : Text.literal("Problems: " + String.join("; ", problems)).formatted(Formatting.RED);
   }

   private static int spawnBot(ServerCommandSource source) {
      if (bot != null && bot.isAlive()) {
         source.sendFeedback(() -> Text.literal("ForceBot already present").formatted(Formatting.YELLOW), false);
         return 0;
      } else {
         MinecraftServer server = source.getServer();
         PlayerManager playerManager = server.getPlayerManager();
         GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes("ForceBot".getBytes()), "ForceBot");
         SyncedClientOptions options = SyncedClientOptions.createDefault();

         try {
            ServerPlayerEntity player = playerManager.createPlayer(profile, options);
            ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
            Field channelField = null;

            for (Field f : ClientConnection.class.getDeclaredFields()) {
               if (f.getType() == Channel.class) {
                  channelField = f;
                  break;
               }
            }

            if (channelField == null) {
               throw new IllegalStateException("channel field not found");
            } else {
               channelField.setAccessible(true);
               channelField.set(connection, new EmbeddedChannel());
               ConnectedClientData clientData = ConnectedClientData.createDefault(profile, false);
               player.networkHandler = new ServerPlayNetworkHandler(server, connection, player, clientData);
               player.setPosition(source.getPosition());
               playerManager.onPlayerConnect(connection, player, clientData);
               bot = player;
               ForceManager.sendSync(player);
               Text feedback = Text.literal("ForceBot spawned at " + player.getBlockPos().toShortString()).formatted(Formatting.GREEN);
               source.sendFeedback(() -> feedback, false);
               return 1;
            }
         } catch (Throwable var12) {
            AL.LOGGER.error("ForceBot spawn failed", var12);
            Text err = Text.literal("Spawn failed: " + var12.getClass().getSimpleName() + ": " + var12.getMessage());
            source.sendError(err);
            return 0;
         }
      }
   }

   private static int coffinTestPhase = 0;
   private static int coffinTestTick = 0;
   private static BlockPos coffinS;
   private static final BlockPos[] COFFIN_SPOTS = new BlockPos[4];
   private static BlockPos depositSpot = null;
   private static SithGhostEntity testGhost = null;
   private static int coffinPass = 0;
   private static int coffinFail = 0;

   private static int runCoffinTest(ServerCommandSource source) {
      if (bot == null) {
         source.sendError(Text.literal("Spawn the bot first: /altest bot spawn"));
         return 0;
      }

      MinecraftServer server = source.getServer();
      ServerWorld world = server.getOverworld();

      for (int cx = 23; cx <= 26; cx++) {
         for (int cz = 23; cz <= 26; cz++) {
            world.getChunk(cx, cz, net.minecraft.world.chunk.ChunkStatus.FULL, true);
            world.setChunkForced(cx, cz, true);
         }
      }

      int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, 400, 400);

      coffinS = new BlockPos(400, y, 400);

      for (BlockPos pos : BlockPos.iterate(376, y - 2, 376, 424, y + 6, 424)) {
         world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
      }

      for (BlockPos pos : BlockPos.iterate(376, y - 2, 376, 424, y - 2, 424)) {
         world.setBlockState(pos, Blocks.STONE.getDefaultState(), Block.FORCE_STATE);
      }

      COFFIN_SPOTS[0] = new BlockPos(420, y - 1, 400);
      COFFIN_SPOTS[1] = new BlockPos(380, y - 1, 400);
      COFFIN_SPOTS[2] = new BlockPos(400, y - 1, 420);
      COFFIN_SPOTS[3] = new BlockPos(400, y - 1, 380);

      placeCoffinArena(world);
      coffinTestPhase = 1;
      coffinTestTick = 0;
      coffinPass = 0;
      coffinFail = 0;
      source.sendFeedback(() -> Text.literal("[AL-COFFIN] test started, sarc=" + coffinS.toShortString()), false);
      return 1;
   }

   private static void placeCoffinArena(ServerWorld world) {
      world.setBlockState(coffinS,
         ALBlocks.SITH_SARCOPHAGUS.getDefaultState().with(net.minecraft.state.property.Properties.HORIZONTAL_FACING, Direction.NORTH),
         Block.FORCE_STATE);

      for (BlockPos spot : COFFIN_SPOTS) {
         world.setBlockState(spot, ALBlocks.SITH_STONE_COFFIN.getDefaultState(), Block.FORCE_STATE);
      }

      bot.refreshPositionAndAngles(coffinS.getX() + 0.5, coffinS.getY() + 1.0, coffinS.getZ() + 0.5, 0.0F, 0.0F);
   }

   private static void coffinCheck(ServerWorld world, String what, boolean ok) {
      if (ok) {
         coffinPass++;
         AL.LOGGER.info("[AL-COFFIN] PASS {}", what);
      } else {
         coffinFail++;
         AL.LOGGER.error("[AL-COFFIN] FAIL {}", what);
      }
   }

   private static void coffinTick(MinecraftServer server) {
      if (coffinTestPhase == 0) {
         return;
      }

      ServerWorld world = server.getOverworld();
      coffinTestTick++;

      switch (coffinTestPhase) {
         case 1 -> {
            if (coffinTestTick >= 60) {
               boolean allOpen = true;
               int ghosts = 0;

               for (BlockPos spot : COFFIN_SPOTS) {
                  boolean open = world.getBlockState(spot).contains(com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock.OPEN)
                     && world.getBlockState(spot).get(com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock.OPEN);
                  allOpen &= open;
               }

               ghosts = world.getEntitiesByClass(SithGhostEntity.class, new Box(coffinS).expand(48), g -> true).size();
               coffinCheck(world, "wake opened all four stone coffins", allOpen);
               coffinCheck(world, "wake spawned four ghosts", ghosts >= 4);

               if (!allOpen || ghosts < 4) {
                  for (BlockPos spot : COFFIN_SPOTS) {
                     AL.LOGGER.info("[AL-COFFIN] p1debug {} open={} link={}",
                        spot.toShortString(),
                        world.getBlockState(spot).contains(SithStoneCoffinBlock.OPEN) && world.getBlockState(spot).get(SithStoneCoffinBlock.OPEN),
                        world.getBlockEntity(spot) instanceof SithStoneCoffinBlockEntity c ? c.isOpen() : -1);
                  }
               }

               bot.refreshPositionAndAngles(440.5, world.getTopY(Heightmap.Type.MOTION_BLOCKING, 440, 440) + 1, 440.5, 0.0F, 0.0F);

               for (SithGhostEntity g : world.getEntitiesByClass(SithGhostEntity.class,
                     new Box(coffinS).expand(48), gg -> true)) {
                  g.refreshPositionAndAngles(g.restX + 0.5, g.restY + 0.5, g.restZ + 0.5, 0.0F, 0.0F);
                  g.setTarget(null);
                  g.taskFinished = 1;
               }

               coffinTestPhase = 2;
               coffinTestTick = 0;
            }
         }
         case 2 -> {
            if (coffinTestTick >= 10) {
               bot.refreshPositionAndAngles(440.5, world.getTopY(Heightmap.Type.MOTION_BLOCKING, 440, 440) + 1, 440.5, 0.0F, 0.0F);
               depositSpot = new BlockPos(400, coffinS.getY(), 422);
               world.setBlockState(depositSpot, ALBlocks.SITH_STONE_COFFIN.getDefaultState(), Block.FORCE_STATE);
               world.setBlockState(depositSpot.up(), Blocks.REDSTONE_BLOCK.getDefaultState(), Block.FORCE_STATE);
               coffinTestPhase = 3;
               coffinTestTick = 0;
            }
         }
         case 3 -> {
            if (coffinTestTick >= 40) {
               boolean armed = false;

               for (SithGhostEntity g : world.getEntitiesByClass(SithGhostEntity.class,
                     new Box(depositSpot).expand(6), gg -> true)) {
                  if (!g.getMainHandStack().isEmpty() && g.getMainHandStack().contains(ALComponents.LIGHTSABER)) {
                     armed = true;
                     testGhost = g;
                  }
               }

               coffinCheck(world, "redstone wake armed a ghost with a saber", armed && testGhost != null);
               world.setBlockState(depositSpot.up(), Blocks.AIR.getDefaultState(), Block.FORCE_STATE);

               if (testGhost != null) {
                  testGhost.refreshPositionAndAngles(depositSpot.getX() + 0.5, depositSpot.getY() + 0.5, depositSpot.getZ() + 0.5, 0.0F, 0.0F);
                  testGhost.setTarget(null);
                  testGhost.taskFinished = 1;
               }

               coffinTestPhase = 4;
               coffinTestTick = 0;
            }
         }
         case 4 -> {
            if (coffinTestTick >= 30) {
               boolean open = world.getBlockState(depositSpot).get(SithStoneCoffinBlock.OPEN);
               ItemStack stored = ItemStack.EMPTY;

               if (world.getBlockEntity(depositSpot) instanceof SithStoneCoffinBlockEntity coffin) {
                  stored = coffin.getEquipment();
               }

               coffinCheck(world, "calmed ghost deposited its saber and closed the coffin",
                  !open && stored.contains(ALComponents.LIGHTSABER));

               if (open || stored.isEmpty()) {
                  AL.LOGGER.info("[AL-COFFIN] p4debug open={} stored={} ghostAlive={} hand={}",
                     open, stored.isEmpty(),
                     testGhost != null && testGhost.isAlive(),
                     testGhost != null ? testGhost.getMainHandStack().getItem() : null);
               }
               coffinTestPhase = 5;
               coffinTestTick = 0;
            }
         }
         case 5 -> {
            if (coffinTestTick >= 10) {
               if (world.getBlockEntity(depositSpot) instanceof SithStoneCoffinBlockEntity coffin) {
                  coffin.punchRetrieve(world, depositSpot, bot);

                  boolean removed = world.getBlockState(depositSpot).isAir();
                  ItemStack coffinItem = ItemStack.EMPTY;

                  for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, new Box(depositSpot).expand(3), i -> true)) {
                     if (item.getStack().contains(ALComponents.COFFIN_EQUIPMENT)
                           && !item.getStack().get(ALComponents.COFFIN_EQUIPMENT).isEmpty()
                           && item.getStack().get(ALComponents.COFFIN_EQUIPMENT).get(0).contains(ALComponents.LIGHTSABER)) {
                        coffinItem = item.getStack();
                     }
                  }

                  coffinCheck(world, "punch retrieval removed the coffin block", removed);
                  coffinCheck(world, "punch retrieval dropped a coffin item holding the saber", !coffinItem.isEmpty());
               } else {
                  coffinCheck(world, "coffin BE present after placement", false);
               }

               coffinTestPhase = 6;
               coffinTestTick = 0;
            }
         }
         case 6 -> {
            if (coffinTestTick >= 10) {
               world.setBlockState(depositSpot, ALBlocks.SITH_STONE_COFFIN.getDefaultState(), Block.FORCE_STATE);
               world.setBlockState(depositSpot.up(), Blocks.REDSTONE_BLOCK.getDefaultState(), Block.FORCE_STATE);
               coffinTestPhase = 7;
               coffinTestTick = 0;
            }
         }
         case 7 -> {
            if (coffinTestTick >= 40) {
               boolean open = world.getBlockState(depositSpot).get(SithStoneCoffinBlock.OPEN);
               boolean armed = false;

               for (SithGhostEntity g : world.getEntitiesByClass(SithGhostEntity.class,
                     new Box(depositSpot).expand(6), gg -> true)) {
                  if (!g.getMainHandStack().isEmpty() && g.getMainHandStack().contains(ALComponents.LIGHTSABER)) {
                     armed = true;
                  }
               }

               coffinCheck(world, "redstone rewake gave the ghost the stored saber", open && armed);
               world.setBlockState(depositSpot.up(), Blocks.AIR.getDefaultState(), Block.FORCE_STATE);

               try {
                  ItemStack probe = new ItemStack(ALBlocks.SITH_STONE_COFFIN.asItem());
                  ItemStack saber = new ItemStack(ALItems.LIGHTSABER);
                  saber.set(ALComponents.LIGHTSABER, new LightsaberComponent(false, "knighted", 16711680, false, 0));
                  probe.set(ALComponents.COFFIN_EQUIPMENT, java.util.List.of(saber));
                  net.minecraft.nbt.NbtCompound nbt = (net.minecraft.nbt.NbtCompound) probe.encodeAllowEmpty(world.getRegistryManager());
                  ItemStack back = ItemStack.fromNbtOrEmpty(world.getRegistryManager(), nbt);
                  AL.LOGGER.info("[AL-COFFIN] roundtrip ok, equipment={} size={}",
                     back.contains(ALComponents.COFFIN_EQUIPMENT), back.get(ALComponents.COFFIN_EQUIPMENT).size());
                  coffinPass++;
               } catch (Throwable t) {
                  coffinFail++;
                  AL.LOGGER.error("[AL-COFFIN] roundtrip FAILED", t);
               }

               AL.LOGGER.info("[AL-COFFIN] done: {} pass, {} fail", coffinPass, coffinFail);
               coffinTestPhase = 0;
            }
         }
      }
   }

   private static void coffinTickHook(MinecraftServer server) {
      coffinTick(server);
   }

   private static int despawnBot(ServerCommandSource source) {
      if (bot != null) {
         bot.getServer().getPlayerManager().remove(bot);
         bot = null;
         source.sendFeedback(() -> Text.literal("ForceBot removed").formatted(Formatting.GREEN), false);
      }

      return 1;
   }

   private static int unlockAll(ServerCommandSource source) {
      if (bot == null) {
         source.sendError(Text.literal("Spawn the bot first: /altest bot spawn"));
         return 0;
      } else {
         ForceManager.setXP(bot, 1000000);
         int count = 0;
         boolean changed = true;

         while (changed) {
            changed = false;

            for (Power power : Power.POWERS) {
               if (ForceManager.unlockPower(bot, power)) {
                  count++;
                  changed = true;
               }
            }
         }

         int finalCount = count;
         source.sendFeedback(
            () -> Text.literal("Unlocked " + finalCount + " powers, energy max " + (int)ForceManager.getMaxEnergy(bot))
                  .formatted(Formatting.GREEN),
            false
         );
         return 1;
      }
   }

   private static int channelProbe(ServerCommandSource source) {
      if (bot == null) {
         source.sendError(Text.literal("Spawn the bot first: /altest bot spawn"));
         return 0;
      }

      ForceManager.setEnergy(bot, ForceManager.getMaxEnergy(bot));
      ForcePowers.resetCastCooldown(bot);
      ForcePowers.startChannel(bot, "lightning1");
      ForcePowers.keepAliveChannel(bot, "lightning1");
      float before = ForceManager.getEnergy(bot);
      com.drag0nge0de.lightsabers.force.ForcePowers.tickChannels(bot);
      float after = ForceManager.getEnergy(bot);
      boolean channeling = ForcePowers.isChanneling(bot);
      boolean effect = com.drag0nge0de.lightsabers.force.ForceEffects.has(bot, com.drag0nge0de.lightsabers.force.ForceEffects.LIGHTNING);
      source.sendFeedback(() -> Text.literal("Channel probe: before=" + before + " after=" + after
            + " channeling=" + channeling + " lightningEffect=" + effect), false);
      return 1;
   }

   private static int channelSustain(ServerCommandSource source) {
      if (bot == null) {
         source.sendError(Text.literal("Spawn the bot first: /altest bot spawn"));
         return 0;
      }

      ForceManager.unlockPowerUngated(bot, Power.byName("fortify1"));
      ForcePowers.stopChannel(bot, false);
      ForcePowers.resetCastCooldown(bot);
      ForceManager.setEnergy(bot, ForceManager.getMaxEnergy(bot));

      ForcePowers.startChannel(bot, "fortify1");
      float before = ForceManager.getEnergy(bot);
      for (int i = 0; i < 100; i++) {
         ForcePowers.tickChannels(bot);
         if (i % 10 == 9) {
            ForcePowers.startChannel(bot, "fortify1");
         }
      }

      boolean sustained = ForcePowers.isChanneling(bot);
      float drained = before - ForceManager.getEnergy(bot);
      boolean effect = ForceEffects.has(bot, ForceEffects.FORTIFY);
      ForcePowers.stopChannel(bot, false);

      ForcePowers.resetCastCooldown(bot);
      ForcePowers.startChannel(bot, "fortify1");
      ForcePowers.getChannel(bot).started = bot.getWorld().getTime() - 100;
      ForcePowers.tickChannels(bot);
      boolean watchdogWorks = !ForcePowers.isChanneling(bot);
      ForcePowers.stopChannel(bot, false);

      ForcePowers.resetCastCooldown(bot);
      ForcePowers.startChannel(bot, "fortify1");
      ForcePowers.getChannel(bot).started = bot.getWorld().getTime() - 100;
      ForcePowers.startChannel(bot, "fortify1");
      ForcePowers.tickChannels(bot);
      boolean refreshSaves = ForcePowers.isChanneling(bot);
      ForcePowers.stopChannel(bot, false);

      boolean pass = sustained && drained > 0 && effect && watchdogWorks && refreshSaves;
      String msg = "channelsustain: held=100t sustained=" + sustained
            + " drained=" + String.format("%.1f", drained) + " fortifyEffect=" + effect
            + " watchdogKillsStale=" + watchdogWorks
            + " keepaliveRevivesStale=" + refreshSaves + (pass ? " PASS" : " FAIL");
      source.sendFeedback(() -> Text.literal(msg).formatted(pass ? Formatting.GREEN : Formatting.RED), false);
      return 1;
   }

   private static int castAll(ServerCommandSource source) {
      if (bot == null) {
         source.sendError(Text.literal("Spawn the bot first: /altest bot spawn"));
         return 0;
      } else {
         int success = 0;
         int attempted = 0;
         List<String> failed = new ArrayList<>();

         ForceManager.setXP(bot, 200000);

         for (Power power : Power.allCastable()) {
            if (power.stats.powerType == com.drag0nge0de.lightsabers.force.PowerType.PER_SECOND) {
               continue;
            }

            attempted++;
            ForceManager.setEnergy(bot, ForceManager.getMaxEnergy(bot));
            if (!ForceManager.hasPower(bot, power)) {
               ForceManager.unlockPowerUngated(bot, power);
            }
            ForcePowers.resetCastCooldown(bot);
            if (!ForcePowers.cast(bot, power.getName())) {
               failed.add(power.getName());
            } else {
               success++;
            }
         }

         ForcePowers.resetCastCooldown(bot);

         int s = success;
         int a = attempted;
         String fails = String.join(", ", failed);
         source.sendFeedback(
            () -> Text.literal("Cast " + s + "/" + a + " castable powers" + (fails.isEmpty() ? " - all OK" : " - failed: " + fails))
                  .formatted(fails.isEmpty() ? Formatting.GREEN : Formatting.YELLOW),
            false
         );
         return 1;
      }
   }

   private static int botInfo(ServerCommandSource source) {
      if (bot == null) {
         source.sendError(Text.literal("No ForceBot"));
         return 0;
      } else {
         ServerPlayerEntity b = bot;
         source.sendFeedback(
            () -> Text.literal(
                  "ForceBot: xp="
                     + ForceManager.getXP(b)
                     + " energy="
                     + (int)ForceManager.getEnergy(b)
                     + "/"
                     + (int)ForceManager.getMaxEnergy(b)
                     + " regen="
                     + ForceManager.getRegenPerSecond(b)
                     + " unlocked="
                     + ((List)b.getAttachedOrCreate(ForceState.UNLOCKED, ArrayList::new)).size()
               ),
            false
         );
         return 1;
      }
   }
}
