package com.drag0nge0de.lightsabers;

import com.drag0nge0de.lightsabers.client.ForceArcs;
import com.drag0nge0de.lightsabers.client.ForceClientState;
import com.drag0nge0de.lightsabers.client.ForceLoopSounds;
import com.drag0nge0de.lightsabers.client.ForceHud;
import com.drag0nge0de.lightsabers.client.render.BlockEntityRenderers;
import com.drag0nge0de.lightsabers.client.render.HolocronRenderer;
import com.drag0nge0de.lightsabers.client.render.ItemRenderers;
import com.drag0nge0de.lightsabers.client.render.LightsaberStandRenderer;
import com.drag0nge0de.lightsabers.client.render.SithGhostRenderer;
import com.drag0nge0de.lightsabers.client.render.model.SithGhostModel;
import com.drag0nge0de.lightsabers.client.render.ThrownSaberRenderer;
import com.drag0nge0de.lightsabers.client.screen.CrystalPouchScreen;
import com.drag0nge0de.lightsabers.client.screen.DisassemblyStationScreen;
import com.drag0nge0de.lightsabers.client.screen.ForcePowersScreen;
import com.drag0nge0de.lightsabers.client.screen.LightsaberForgeScreen;
import com.drag0nge0de.lightsabers.client.screen.SithSarcophagusScreen;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import com.drag0nge0de.lightsabers.registry.ALScreens;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALEntities;
import com.drag0nge0de.lightsabers.registry.ALItems;
import java.util.HashSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.MathHelper;

public class ALClient implements ClientModInitializer {
   public static final EntityModelLayer SITH_GHOST_LAYER = new EntityModelLayer(AL.id("sith_ghost"), "main");
   private static KeyBinding saberKey;
   private static KeyBinding castKey;
   private static KeyBinding selectKey;
   private static KeyBinding configKey;
   private static int selectHeldTicks;
   private static int useCooldown;
   private static int channelTickCounter;
   private static boolean channelActive;
   private static String channelPower = "";

   public void onInitializeClient() {

      ColorProviderRegistry.ITEM.register((ItemColorProvider)(stack, tintIndex) -> {
         LightsaberComponent component = (LightsaberComponent)stack.get(ALComponents.LIGHTSABER);
         return component != null && tintIndex >= 1 ? component.color() | 0xFF000000 : 0xFFFFFFFF;
      }, new ItemConvertible[]{ALItems.LIGHTSABER, ALItems.DOUBLE_LIGHTSABER});
      ColorProviderRegistry.ITEM.register((ItemColorProvider)(stack, tintIndex) -> {
         CrystalComponent component = (CrystalComponent)stack.get(ALComponents.CRYSTAL);
         return component != null ? component.color() | 0xFF000000 : 0xFFFFFFFF;
      }, new ItemConvertible[]{ALItems.KYBER_CRYSTAL});
      ColorProviderRegistry.ITEM.register((ItemColorProvider)(stack, tintIndex) -> {
         CrystalComponent component = (CrystalComponent)stack.get(ALComponents.CRYSTAL);
         return component != null && tintIndex == 1 ? component.color() | 0xFF000000 : 0xFFFFFFFF;
      }, new ItemConvertible[]{ALItems.CRYSTAL_POUCH});
      EntityModelLayerRegistry.registerModelLayer(SITH_GHOST_LAYER, SithGhostModel::getTexturedModelData);
      EntityRendererRegistry.register(ALEntities.THROWN_LIGHTSABER, ThrownSaberRenderer::new);
      EntityRendererRegistry.register(ALEntities.SITH_GHOST, SithGhostRenderer::new);
      ItemRenderers.register();
      BlockEntityRendererFactories.register(ALBlockEntities.LIGHTSABER_STAND, LightsaberStandRenderer::new);
      BlockEntityRendererFactories.register(ALBlockEntities.HOLOCRON, HolocronRenderer::new);
      BlockEntityRendererFactories.register(ALBlockEntities.LIGHTSABER_FORGE, BlockEntityRenderers.Forge::new);
      BlockEntityRendererFactories.register(ALBlockEntities.DISASSEMBLY_STATION, BlockEntityRenderers.DisassemblyStation::new);
      BlockEntityRendererFactories.register(ALBlockEntities.CRYSTAL_ORE, BlockEntityRenderers.CrystalOre::new);
      BlockEntityRendererFactories.register(ALBlockEntities.SITH_SARCOPHAGUS, BlockEntityRenderers.SithCoffin::new);
      BlockEntityRendererFactories.register(ALBlockEntities.SITH_STONE_COFFIN, BlockEntityRenderers.SithStoneCoffin::new);
      HandledScreens.register(ALScreens.LIGHTSABER_FORGE, LightsaberForgeScreen::new);
      HandledScreens.register(ALScreens.DISASSEMBLY_STATION, DisassemblyStationScreen::new);
      HandledScreens.register(ALScreens.SITH_SARCOPHAGUS, SithSarcophagusScreen::new);
      HandledScreens.register(ALScreens.CRYSTAL_POUCH, CrystalPouchScreen::new);
      ClientPlayNetworking.registerGlobalReceiver(
         ALNetwork.SyncForcePayload.ID,
         (payload, context) -> ForceClientState.update(payload.xp(), payload.energy(), payload.maxEnergy(), payload.regen(), new HashSet<>(payload.unlocked()), payload.xpInvested())
      );

      ClientPlayNetworking.registerGlobalReceiver(
         ALNetwork.SyncSelectionPayload.ID,
         (payload, context) -> ForceClientState.updateSelection(payload.slot(), payload.powers())
      );

      ClientPlayNetworking.registerGlobalReceiver(
         ALNetwork.SyncEffectsPayload.ID,
         (payload, context) -> {
            java.util.Map<String, int[]> effects = new java.util.HashMap<>();
            for (ALNetwork.EffectData data : payload.effects()) {
               effects.put(data.id(), new int[]{data.amplifier(), data.duration(), data.casterId()});
            }

            if (context.player() != null && payload.entityId() == context.player().getId()) {
               ForceClientState.updateEffects(effects);
            }

            ForceArcs.updateEntity(payload.entityId(), effects);
         }
      );

      ClientPlayNetworking.registerGlobalReceiver(
         ALNetwork.OpenForcePowersPayload.ID,
         (payload, context) -> context.client().send(() -> context.client().setScreen(new ForcePowersScreen(payload.pos())))
      );
      ClientPlayNetworking.registerGlobalReceiver(
         ALNetwork.HolocronStatePayload.ID,
         (payload, context) -> context.client().execute(() -> {
            if (context.client().world != null
                  && context.client().world.getBlockEntity(payload.pos())
                     instanceof com.drag0nge0de.lightsabers.block.blockentity.HolocronBlockEntity holocron) {
               holocron.clientAddUser(payload.open() ? 1 : -1);
            }
         })
      );
      ClientPlayNetworking.registerGlobalReceiver(
         ALNetwork.SyncLidPayload.ID,
         (payload, context) -> context.client().execute(() -> {
            if (context.client().world != null
                  && context.client().world.getBlockEntity(payload.pos())
                     instanceof com.drag0nge0de.lightsabers.block.blockentity.SithSarcophagusBlockEntity sarcophagus) {
               sarcophagus.syncLid(payload.timer());
            }
         })
      );
      com.drag0nge0de.lightsabers.client.ALShotsBot.register();
      this.registerKeybinds();
      net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
         (entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof net.minecraft.client.render.entity.PlayerEntityRenderer playerRenderer) {
               registrationHelper.register(new com.drag0nge0de.lightsabers.client.render.HippedSaberFeature(playerRenderer));
            }
         });
      com.drag0nge0de.lightsabers.config.ALConfig.load();
      ForceHud.register();
      ForceArcs.register();
      com.drag0nge0de.lightsabers.client.DynamicLightsCompat.init();
   }

   private void registerKeybinds() {
      castKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lightsabers.activate_power", Type.KEYSYM, 67, "category.lightsabers"));
      selectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lightsabers.select_power", Type.KEYSYM, 70, "category.lightsabers"));
      saberKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lightsabers.activate_saber", Type.KEYSYM, 82, "category.lightsabers"));
      configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lightsabers.open_config", Type.KEYSYM, 79, "category.lightsabers"));
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         ForceClientState.tickEffects();
         ForceLoopSounds.tick(client);
         channelTickCounter++;
         if (useCooldown > 0) {
            useCooldown--;
         }

         while (saberKey.wasPressed()) {
            ClientPlayNetworking.send(new ALNetwork.ToggleSaberPayload(true));
         }

         while (configKey.wasPressed()) {
            if (client.currentScreen == null) {
               client.setScreen(new com.drag0nge0de.lightsabers.client.screen.LightsaberConfigScreen(null));
            }
         }

         com.drag0nge0de.lightsabers.force.Power selected = ForceClientState.getSelectedPower();
         String selectedName = selected != null ? selected.getName() : "";
         boolean castDown = castKey.isPressed() && client.currentScreen == null;
         boolean selectDown = selectKey.isPressed() && client.currentScreen == null;

         while (selectKey.wasPressed()) {
            if (ForceClientState.hasSensitivity()) {
               int next = ForceClientState.selectedSlot < 2 ? ForceClientState.selectedSlot + 1 : 0;
               ForceClientState.selectedSlot = next;
               ClientPlayNetworking.send(new ALNetwork.SelectSlotPayload(next));
            }
         }

         if (selectDown) {
            selectHeldTicks++;
            if (selectHeldTicks == 5) {
               if (ForceClientState.selectedSlot > 0) {
                  ForceClientState.selectedSlot--;
                  ClientPlayNetworking.send(new ALNetwork.SelectSlotPayload(ForceClientState.selectedSlot));
               } else {
                  ForceClientState.selectedSlot = 2;
                  ClientPlayNetworking.send(new ALNetwork.SelectSlotPayload(2));
               }

               client.setScreen(new com.drag0nge0de.lightsabers.client.screen.ForceSelectScreen());
            }
         } else {
            selectHeldTicks = 0;
         }

         boolean spectator = client.player != null && client.player.isSpectator();
         if (!spectator && castDown && selected != null && client.currentScreen == null) {
            if (selected.stats.powerType == com.drag0nge0de.lightsabers.force.PowerType.PER_USE) {
               while (castKey.wasPressed()) {
                  if (useCooldown <= 0) {
                     useCooldown = 20;
                     ClientPlayNetworking.send(new ALNetwork.CastPowerPayload(selectedName));
                  }
               }
            } else if (selected.stats.powerType == com.drag0nge0de.lightsabers.force.PowerType.PER_SECOND) {
               if (useCooldown <= 0) {
                  if (!channelActive) {
                     channelActive = true;
                     channelPower = selectedName;
                     ClientPlayNetworking.send(new ALNetwork.ChannelPayload(true, selectedName));
                  } else if (channelTickCounter % 10 == 0) {
                     ClientPlayNetworking.send(new ALNetwork.ChannelPayload(true, selectedName));
                  }
               }
            }
         }

         if (channelActive && (!castDown || !selectedName.equals(channelPower))) {
            channelActive = false;
            useCooldown = 20;
            ClientPlayNetworking.send(new ALNetwork.ChannelPayload(false, channelPower));
         }
      });
   }

   private static float lerp(float a, float b, float t) {
      return MathHelper.lerp(t, a, b);
   }

   private static record ForceKey(String baseName, int defaultKey) {
   }
}
