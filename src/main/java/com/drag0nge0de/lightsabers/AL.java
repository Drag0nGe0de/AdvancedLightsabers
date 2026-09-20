package com.drag0nge0de.lightsabers;

import com.drag0nge0de.lightsabers.command.ALTestCommand;
import com.drag0nge0de.lightsabers.command.ForceCommand;
import com.drag0nge0de.lightsabers.force.ForcePowers;
import com.drag0nge0de.lightsabers.force.ForceState;
import com.drag0nge0de.lightsabers.handler.ForceEvents;
import com.drag0nge0de.lightsabers.loot.ALLoot;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALEntities;
import com.drag0nge0de.lightsabers.registry.ALScreens;
import com.drag0nge0de.lightsabers.registry.ALItemGroups;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.registry.ALRecipes;
import com.drag0nge0de.lightsabers.registry.ALSounds;
import com.drag0nge0de.lightsabers.registry.ALWorldgen;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AL implements ModInitializer {
   public static final String MOD_ID = "lightsabers";
   public static final Logger LOGGER = LoggerFactory.getLogger("Advanced Lightsabers");

   public static Identifier id(String path) {
      return Identifier.of("lightsabers", path);
   }

   public void onInitialize() {
      com.drag0nge0de.lightsabers.config.ALConfig.load();
      ALComponents.register();
      com.drag0nge0de.lightsabers.advancement.ALAdvancements.register();
      ALSounds.register();
      ALNetwork.register();
      ForceState.touch();
      ALBlocks.register();
      ALBlockEntities.register();

      ALEntities.register();
      ALItems.register();
      com.drag0nge0de.lightsabers.item.LightsaberItem.register();
      ALItemGroups.register();
      ALScreens.register();
      ALRecipes.register();
      ALWorldgen.register();
      com.drag0nge0de.lightsabers.world.CrystalRelicScanner.register();
      ALLoot.register();
      ForcePowers.register();
      ForceEvents.register();
      ForceCommand.register();
      if (Boolean.getBoolean("al.altest")) {
         ALTestCommand.register();
      }
      if (Boolean.getBoolean("al.shots")) {
         com.drag0nge0de.lightsabers.command.ALShotsServer.register();
         com.drag0nge0de.lightsabers.command.ALBehaviorTest.register();
      }
      ServerLifecycleEvents.SERVER_STARTED.register(AL::verifyRecipeSync);
      LOGGER.info("Advanced Lightsabers (Fabric port) initialized - may the Force be with you");
   }

   private static void verifyRecipeSync(MinecraftServer server) {
      try {
         SynchronizeRecipesS2CPacket packet = new SynchronizeRecipesS2CPacket(server.getRecipeManager().values());
         RegistryByteBuf buf = new RegistryByteBuf(Unpooled.buffer(), server.getRegistryManager());
         SynchronizeRecipesS2CPacket.CODEC.encode(buf, packet);
         int bytes = buf.readableBytes();
         buf.release();
         LOGGER.info("Recipe sync self-check passed ({} recipes, {} bytes) - clients can join", packet.getRecipes().size(), bytes);
      } catch (Throwable var4) {
         LOGGER.error("Recipe sync self-check FAILED - players would be kicked on world join", var4);
         throw var4;
      }
   }
}
