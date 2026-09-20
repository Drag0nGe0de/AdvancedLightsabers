package com.drag0nge0de.lightsabers.command;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import com.drag0nge0de.lightsabers.force.ForceManager;
import com.drag0nge0de.lightsabers.force.ForcePowers;
import com.drag0nge0de.lightsabers.force.Power;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ForceCommand {

   private static final int MAXOUT_XP = 100000;

   private static final SuggestionProvider<ServerCommandSource> POWER_SUGGESTIONS = (context, builder) -> {
      for (Power power : Power.POWERS) {
         builder.suggest(power.getName());
      }
      return builder.buildFuture();
   };

   public static void register() {
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            CommandManager.literal("force")
               .requires(source -> source.hasPermissionLevel(2))
               .then(CommandManager.literal("xp")
                  .then(CommandManager.literal("add")
                     .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> addXp(ctx, sourcePlayer(ctx), IntegerArgumentType.getInteger(ctx, "amount"))))
                     .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                           .executes(ctx -> {
                              int n = 0;
                              for (ServerPlayerEntity p : EntityArgumentType.getPlayers(ctx, "targets")) {
                                 addXp(ctx, p, IntegerArgumentType.getInteger(ctx, "amount"));
                                 n++;
                              }
                              return n;
                           }))))
                  .then(CommandManager.literal("set")
                     .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> setXp(ctx, sourcePlayer(ctx), IntegerArgumentType.getInteger(ctx, "amount"))))
                     .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                           .executes(ctx -> {
                              int n = 0;
                              for (ServerPlayerEntity p : EntityArgumentType.getPlayers(ctx, "targets")) {
                                 setXp(ctx, p, IntegerArgumentType.getInteger(ctx, "amount"));
                                 n++;
                              }
                              return n;
                           }))))
                  .then(CommandManager.literal("query")
                     .executes(ctx -> queryXp(ctx, sourcePlayer(ctx)))
                     .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> queryXp(ctx, EntityArgumentType.getPlayer(ctx, "target"))))))
               .then(CommandManager.literal("energy")
                  .then(CommandManager.literal("add")
                     .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                           ServerPlayerEntity p = sourcePlayer(ctx);
                           ForceManager.addEnergy(p, IntegerArgumentType.getInteger(ctx, "amount"));
                           return 1;
                        }))
                     .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                           .executes(ctx -> {
                              for (ServerPlayerEntity p : EntityArgumentType.getPlayers(ctx, "targets")) {
                                 ForceManager.addEnergy(p, IntegerArgumentType.getInteger(ctx, "amount"));
                              }
                              return 1;
                           }))))
                  .then(CommandManager.literal("set")
                     .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                           ServerPlayerEntity p = sourcePlayer(ctx);
                           ForceManager.setEnergy(p, IntegerArgumentType.getInteger(ctx, "amount"));
                           return 1;
                        }))
                     .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                           .executes(ctx -> {
                              for (ServerPlayerEntity p : EntityArgumentType.getPlayers(ctx, "targets")) {
                                 ForceManager.setEnergy(p, IntegerArgumentType.getInteger(ctx, "amount"));
                              }
                              return 1;
                           })))))
               .then(CommandManager.literal("power")
                  .then(CommandManager.literal("unlock")
                     .then(CommandManager.literal("all")
                        .executes(ctx -> {
                           ServerPlayerEntity p = sourcePlayer(ctx);
                           return unlockAll(ctx, p);
                        })
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                           .executes(ctx -> {
                              int n = 0;
                              for (ServerPlayerEntity p : EntityArgumentType.getPlayers(ctx, "targets")) {
                                 unlockAll(ctx, p);
                                 n++;
                              }
                              return n;
                           })))
                     .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("power", StringArgumentType.word())
                           .suggests(POWER_SUGGESTIONS)
                           .executes(ctx -> {
                              Power power = requirePower(ctx, StringArgumentType.getString(ctx, "power"));
                              if (power == null) return 0;
                              int n = 0;
                              for (ServerPlayerEntity p : EntityArgumentType.getPlayers(ctx, "targets")) {
                                 if (ForceManager.unlockPower(p, power)) n++;
                              }
                              return n;
                           }))))
                  .then(CommandManager.literal("remove")
                     .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("power", StringArgumentType.word())
                           .suggests(POWER_SUGGESTIONS)
                           .executes(ctx -> {
                              Power power = requirePower(ctx, StringArgumentType.getString(ctx, "power"));
                              if (power == null) return 0;
                              int n = 0;
                              for (ServerPlayerEntity p : EntityArgumentType.getPlayers(ctx, "targets")) {
                                 if (ForceManager.removePower(p, power)) n++;
                              }
                              return n;
                           }))))
                  .then(CommandManager.literal("reset")
                     .executes(ctx -> {
                        ServerPlayerEntity p = sourcePlayer(ctx);
                        ForceManager.reset(p);
                        feedback(ctx, "command.lightsabers.reset", p.getGameProfile().getName());
                        return 1;
                     })
                     .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .executes(ctx -> {
                           for (ServerPlayerEntity p : EntityArgumentType.getPlayers(ctx, "targets")) {
                              ForceManager.reset(p);
                              feedback(ctx, "command.lightsabers.reset", p.getGameProfile().getName());
                           }
                           return 1;
                        }))))
               .then(CommandManager.literal("maxout")
                  .executes(ctx -> {
                     ServerPlayerEntity p = sourcePlayer(ctx);
                     maxOut(ctx, p);
                     return 1;
                  })
                  .then(CommandManager.argument("targets", EntityArgumentType.players())
                     .executes(ctx -> {
                        for (ServerPlayerEntity p : EntityArgumentType.getPlayers(ctx, "targets")) {
                           maxOut(ctx, p);
                        }
                        return 1;
                     })))
               .then(CommandManager.literal("cast")
                  .then(CommandManager.argument("power", StringArgumentType.word())
                     .suggests(POWER_SUGGESTIONS)
                     .executes(ctx -> castPower(ctx, sourcePlayer(ctx), StringArgumentType.getString(ctx, "power")))
                     .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> castPower(ctx, EntityArgumentType.getPlayer(ctx, "target"),
                              StringArgumentType.getString(ctx, "power"))))))
               .then(CommandManager.literal("base")
                  .then(CommandManager.literal("reset")
                     .executes(ctx -> baseReset(ctx, sourcePlayer(ctx)))
                     .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> baseReset(ctx, EntityArgumentType.getPlayer(ctx, "target"))))))
               .then(CommandManager.literal("structure")
                  .then(CommandManager.literal("locate")
                     .then(CommandManager.argument("structure", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                           builder.suggest("temple");
                           builder.suggest("tomb");
                           builder.suggest("crystalcave");
                           builder.suggest("all");
                           return builder.buildFuture();
                        })
                        .executes(ctx -> locateStructure(ctx, StringArgumentType.getString(ctx, "structure"), 1))
                        .then(CommandManager.argument("index", IntegerArgumentType.integer(1, 64))
                           .executes(ctx -> locateStructure(ctx, StringArgumentType.getString(ctx, "structure"),
                                 IntegerArgumentType.getInteger(ctx, "index"))))))
                  .then(CommandManager.literal("generate")
                     .then(CommandManager.argument("structure", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                           builder.suggest("temple");
                           builder.suggest("tomb");
                           return builder.buildFuture();
                        })
                        .executes(ctx -> generateStructure(ctx, StringArgumentType.getString(ctx, "structure"))))))
         ));
   }

   private static int baseReset(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
      int base = ForceManager.getBasePower(player);
      feedback(ctx, "command.lightsabers.base_reset", player.getGameProfile().getName(), base);
      return 1;
   }

   private static int generateStructure(CommandContext<ServerCommandSource> ctx, String name) {
      ServerCommandSource source = ctx.getSource();
      ServerWorld world = source.getWorld();
      if (world.getRegistryKey() != net.minecraft.world.World.OVERWORLD) {
         ctx.getSource().sendError(Text.literal("Structures only generate in the Overworld").formatted(Formatting.RED));
         return 0;
      }

      String lower = name.toLowerCase(java.util.Locale.ROOT);
      net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.gen.structure.Structure> entry;
      String label;
      if (lower.equals("temple") || lower.equals("jeditemple")) {
         entry = structureEntry(ctx, "lightsabers:jedi_temple");
         label = "jedi temple";
      } else if (lower.equals("tomb") || lower.equals("sithtomb")) {
         entry = structureEntry(ctx, "lightsabers:sith_tomb");
         label = "sith tomb";
      } else {
         ctx.getSource().sendError(Text.literal("Unknown structure: " + name).formatted(Formatting.RED));
         return 0;
      }

      if (entry == null) {
         ctx.getSource().sendError(Text.literal("Structure not found in this dimension").formatted(Formatting.RED));
         return 0;
      }

      net.minecraft.world.gen.chunk.ChunkGenerator generator = world.getChunkManager().getChunkGenerator();
      net.minecraft.util.math.BlockPos originPos = net.minecraft.util.math.BlockPos.ofFloored(source.getPosition());
      net.minecraft.util.math.ChunkPos chunkPos = new net.minecraft.util.math.ChunkPos(originPos);
      net.minecraft.world.gen.noise.NoiseConfig noiseConfig =
            ((net.minecraft.server.world.ServerChunkManager) world.getChunkManager()).getNoiseConfig();

      net.minecraft.structure.StructureTemplateManager templates = source.getServer().getStructureTemplateManager();
      net.minecraft.world.gen.structure.Structure structure = entry.value();

      net.minecraft.structure.StructureStart start = structure.createStructureStart(
            world.getRegistryManager(), generator, generator.getBiomeSource(), noiseConfig,
            templates, world.getSeed(), chunkPos, 0, world, biome -> true);

      if (start == null || !start.hasChildren()) {
         ctx.getSource().sendError(Text.literal("Could not assemble a " + label + " here").formatted(Formatting.RED));
         return 0;
      }

      start.place(world, world.getStructureAccessor(), generator,
            net.minecraft.util.math.random.Random.create(world.getSeed()),
            new net.minecraft.util.math.BlockBox(-30000000, world.getBottomY(), -30000000,
                  30000000, world.getTopY(), 30000000),
            chunkPos);

      net.minecraft.util.math.BlockPos origin = start.getBoundingBox().getCenter();
      net.minecraft.util.math.BlockPos finalOrigin = origin;
      ctx.getSource().sendFeedback(() -> Text.literal("Generated a " + label + " at ")
            .append(coordLink(finalOrigin.getX(), finalOrigin.getY(), finalOrigin.getZ()))
            .append(Text.literal(".")), true);
      return 1;
   }

   private static int locateStructure(CommandContext<ServerCommandSource> ctx, String name, int index) {
      ServerWorld world = ctx.getSource().getWorld();
      net.minecraft.util.math.BlockPos center = net.minecraft.util.math.BlockPos.ofFloored(ctx.getSource().getPosition());
      String lower = name.toLowerCase(java.util.Locale.ROOT);

      if (lower.equals("crystalcave") || lower.equals("crystalcaves") || lower.equals("cave")) {
         return locateCrystalCave(ctx, world, center, 100, index);
      }
      int radius = 100;

      List<net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.gen.structure.Structure>> wanted = new ArrayList<>();

      if (lower.equals("all")) {
         wanted.add(structureEntry(ctx, "lightsabers:jedi_temple"));
         wanted.add(structureEntry(ctx, "lightsabers:sith_tomb"));
      } else if (lower.equals("temple")) {
         wanted.add(structureEntry(ctx, "lightsabers:jedi_temple"));
      } else if (lower.equals("tomb")) {
         wanted.add(structureEntry(ctx, "lightsabers:sith_tomb"));
      } else {
         ctx.getSource().sendError(Text.literal("Unknown structure: " + name).formatted(Formatting.RED));
         return 0;
      }

      wanted.removeIf(java.util.Objects::isNull);
      if (wanted.isEmpty()) {
         ctx.getSource().sendError(Text.literal("Structure not found in this dimension").formatted(Formatting.RED));
         return 0;
      }

      net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator calc = world.getChunkManager().getStructurePlacementCalculator();
      List<net.minecraft.util.math.BlockPos> candidates = new ArrayList<>();
      for (net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.gen.structure.Structure> entry : wanted) {
         for (net.minecraft.world.gen.chunk.placement.StructurePlacement placement : calc.getPlacements(entry)) {
            if (placement instanceof net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement spread) {
               int spacing = spread.getSpacing();
               long seed = calc.getStructureSeed();

               int maxRing = radius;
               for (int j = -maxRing; j <= maxRing; j++) {
                  for (int k = -maxRing; k <= maxRing; k++) {
                     net.minecraft.util.math.ChunkPos cell = spread.getStartChunk(seed,
                           (center.getX() >> 4) + spacing * j, (center.getZ() >> 4) + spacing * k);
                     candidates.add(spread.getLocatePos(cell));
                  }
               }
            }
         }
      }

      net.minecraft.util.math.BlockPos centerCopy = center;
      candidates.sort(java.util.Comparator.comparingDouble(pos -> pos.getSquaredDistance(centerCopy)));

      net.minecraft.world.gen.StructureAccessor accessor = world.getStructureAccessor();
      int found = 0;
      int resolved = 0;

      for (net.minecraft.util.math.BlockPos pos : candidates) {
         if (found >= index) {
            break;
         }
         if (++resolved > 2048) {
            break;
         }

         net.minecraft.util.math.ChunkPos chunkPos = new net.minecraft.util.math.ChunkPos(pos);
         for (net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.gen.structure.Structure> entry : wanted) {
            boolean present = false;
            for (net.minecraft.world.gen.chunk.placement.StructurePlacement placement : calc.getPlacements(entry)) {
               net.minecraft.world.StructurePresence presence = accessor.getStructurePresence(chunkPos, entry.value(), placement, false);
               if (presence == net.minecraft.world.StructurePresence.START_NOT_PRESENT) {
                  continue;
               }
               if (presence == net.minecraft.world.StructurePresence.START_PRESENT) {
                  present = true;
                  break;
               }

               net.minecraft.world.chunk.Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z, net.minecraft.world.chunk.ChunkStatus.STRUCTURE_STARTS);
               net.minecraft.structure.StructureStart start = accessor.getStructureStart(
                     net.minecraft.util.math.ChunkSectionPos.from(chunk), entry.value(), chunk);
               if (start != null && start.hasChildren()) {
                  present = true;
               }
               break;
            }

            if (present) {
               found++;
               if (found == index) {
                  String foundName = structureLabel(world, entry);
                  net.minecraft.util.math.BlockPos foundPos = pos;
                  ctx.getSource().sendFeedback(() -> Text.literal("Located " + foundName
                        + (index > 1 ? " #" + index : "") + " at ")
                        .append(coordLink(foundPos.getX(), foundPos.getY(), foundPos.getZ()))
                        .append(Text.literal(".")), true);
                  return 1;
               }
            }
         }
      }

      ctx.getSource().sendError(Text.literal("Found only " + found + " " + (wanted.size() > 1 ? "target" : wantedLabel(lower))
            + " site(s) within 100 chunks - can't report #" + index).formatted(Formatting.RED));
      return 0;
   }

   private static String wantedLabel(String lower) {
      return lower.equals("all") ? "structure" : lower;
   }

   private static String structureLabel(ServerWorld world, net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.gen.structure.Structure> entry) {
      net.minecraft.util.Identifier id = world.getRegistryManager()
            .get(net.minecraft.registry.RegistryKeys.STRUCTURE).getId(entry.value());
      return id != null ? id.getPath().replace('_', ' ') : "structure";
   }

   private static net.minecraft.text.Text coordLink(int x, int y, int z) {
      return net.minecraft.text.Text.literal("[" + x + ", " + y + ", " + z + "]")
            .formatted(Formatting.GREEN)
            .styled(style -> style
                  .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND,
                        "/tp @s " + x + " ~ " + z))
                  .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT,
                        net.minecraft.text.Text.literal("Teleport to " + x + " ~ " + z))));
   }

   private static int locateCrystalCave(CommandContext<ServerCommandSource> ctx, ServerWorld world,
         net.minecraft.util.math.BlockPos center, int radius, int index) {
      if (world.getRegistryKey() != net.minecraft.world.World.OVERWORLD) {
         ctx.getSource().sendError(Text.literal("Crystal caves only generate in the Overworld").formatted(Formatting.RED));
         return 0;
      }

      long seed = world.getSeed();
      int ccx = center.getX() >> 4;
      int ccz = center.getZ() >> 4;
      int candidates = 0;
      int confirmed = 0;
      int loaded = 0;
      final int maxLoaded = 64 + 16 * index;

      for (int r = 0; r <= radius; r++) {
         for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
               if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                  continue;
               }

               int cx = ccx + dx;
               int cz = ccz + dz;
               if (!com.drag0nge0de.lightsabers.world.CrystalCaveFeature.isCrystalCaveChunk(seed, cx, cz)) {
                  continue;
               }

               candidates++;
               if (loaded >= maxLoaded) {
                  ctx.getSource().sendError(Text.literal("Found only " + confirmed + " crystal cave(s) within "
                        + radius + " chunks - can't report #" + index).formatted(Formatting.RED));
                  return 0;
               }

               loaded++;
               world.getChunk(cx, cz);
               net.minecraft.util.math.BlockPos ore = findCrystalOre(world, cx, cz);
               if (ore != null) {
                  confirmed++;
                  if (confirmed == index) {
                     net.minecraft.util.math.BlockPos finalOre = ore;
                     ctx.getSource().sendFeedback(() -> Text.literal("Located crystal cave"
                           + (index > 1 ? " #" + index : "") + " at ")
                           .append(coordLink(finalOre.getX(), finalOre.getY(), finalOre.getZ()))
                           .append(Text.literal(".")), true);
                     return 1;
                  }
               }
            }
         }
      }

      ctx.getSource().sendError(Text.literal("Found only " + confirmed + " crystal cave(s) within " + radius
            + " chunks - can't report #" + index).formatted(Formatting.RED));
      return 0;
   }

   private static net.minecraft.util.math.BlockPos findCrystalOre(ServerWorld world, int cx, int cz) {
      net.minecraft.util.math.BlockPos best = null;
      for (net.minecraft.util.math.BlockPos pos : net.minecraft.util.math.BlockPos.iterate(
            cx << 4, world.getBottomY(), cz << 4, (cx << 4) + 15, world.getTopY() - 1, (cz << 4) + 15)) {
         if (world.getBlockState(pos).isOf(com.drag0nge0de.lightsabers.registry.ALBlocks.CRYSTAL_ORE)
               && (best == null || pos.getY() > best.getY())) {
            best = pos.toImmutable();
         }
      }
      return best;
   }

   private static net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.gen.structure.Structure> structureEntry(
      CommandContext<ServerCommandSource> ctx, String id) {
      return ctx.getSource().getServer().getRegistryManager()
         .get(net.minecraft.registry.RegistryKeys.STRUCTURE)
         .getEntry(net.minecraft.util.Identifier.of(id))
         .orElse(null);
   }

   private static ServerPlayerEntity sourcePlayer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
      return ctx.getSource().getPlayerOrThrow();
   }

   private static void feedback(CommandContext<ServerCommandSource> ctx, String key, Object... args) {
      ctx.getSource().sendFeedback(() -> Text.translatable(key, args), true);
   }

   private static int addXp(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, int amount) {
      ForceManager.addXP(player, amount);
      feedback(ctx, "command.lightsabers.xp_added", amount, player.getGameProfile().getName());
      return 1;
   }

   private static int setXp(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, int amount) {
      ForceManager.setXP(player, amount);
      feedback(ctx, "command.lightsabers.xp_set", player.getGameProfile().getName(), amount);
      return 1;
   }

   private static int queryXp(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
      ctx.getSource().sendFeedback(() -> Text.translatable("command.lightsabers.xp_query",
            player.getGameProfile().getName(), ForceManager.getXP(player)), false);
      return 1;
   }

   private static int unlockAll(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
      int n = 0;
      for (Power power : Power.POWERS) {
         if (ForceManager.unlockPowerUngated(player, power)) n++;
      }
      return n;
   }

   private static void maxOut(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
      ForceManager.setXP(player, MAXOUT_XP);
      unlockAll(ctx, player);
      ForceManager.setEnergy(player, ForceManager.getMaxEnergy(player));
      feedback(ctx, "command.lightsabers.maxout", player.getGameProfile().getName(), MAXOUT_XP);
   }

   private static Power requirePower(CommandContext<ServerCommandSource> ctx, String name) {
      Power power = Power.byName(name);
      if (power == null) {
         ctx.getSource().sendError(Text.literal("Unknown power: " + name).formatted(Formatting.RED));
      }
      return power;
   }

   private static int castPower(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, String powerName) {
      Power power = requirePower(ctx, powerName);
      if (power == null) {
         return 0;
      }
      if (!ForceManager.hasPower(player, power)) {
         ForceManager.unlockPower(player, power);
      }
      boolean success = ForcePowers.cast(player, powerName);
      ctx.getSource().sendFeedback(() -> Text.literal("Cast " + powerName + ": "
            + (success ? "success" : "failed"))
            .formatted(success ? Formatting.GREEN : Formatting.RED), true);
      return success ? 1 : 0;
   }
}
