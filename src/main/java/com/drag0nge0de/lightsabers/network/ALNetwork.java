package com.drag0nge0de.lightsabers.network;

import com.drag0nge0de.lightsabers.AL;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ALNetwork {
   public static void register() {
      PayloadTypeRegistry.playC2S().register(ALNetwork.CastPowerPayload.ID, ALNetwork.CastPowerPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(ALNetwork.UnlockPowerPayload.ID, ALNetwork.UnlockPowerPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(ALNetwork.ToggleSaberPayload.ID, ALNetwork.ToggleSaberPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(ALNetwork.SwingSaberPayload.ID, ALNetwork.SwingSaberPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(ALNetwork.ChannelPayload.ID, ALNetwork.ChannelPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(ALNetwork.SelectSlotPayload.ID, ALNetwork.SelectSlotPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(ALNetwork.AssignSlotsPayload.ID, ALNetwork.AssignSlotsPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(ALNetwork.DrainXpPayload.ID, ALNetwork.DrainXpPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(ALNetwork.CloseHolocronPayload.ID, ALNetwork.CloseHolocronPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(ALNetwork.SyncForcePayload.ID, ALNetwork.SyncForcePayload.CODEC);
      PayloadTypeRegistry.playS2C().register(ALNetwork.SyncSelectionPayload.ID, ALNetwork.SyncSelectionPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(ALNetwork.SyncEffectsPayload.ID, ALNetwork.SyncEffectsPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(ALNetwork.OpenForcePowersPayload.ID, ALNetwork.OpenForcePowersPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(ALNetwork.SyncLidPayload.ID, ALNetwork.SyncLidPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(ALNetwork.HolocronStatePayload.ID, ALNetwork.HolocronStatePayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(ALNetwork.SwingSaberPayload.ID,
         (payload, context) -> context.player().getServer().execute(() -> handleSwingSaber(context.player())));
   }

   public static void sendToTracking(ServerWorld world, BlockPos pos, CustomPayload payload) {
      for (ServerPlayerEntity player : PlayerLookup.tracking(world, pos)) {
         ServerPlayNetworking.send(player, payload);
      }
   }

   public static record SyncLidPayload(BlockPos pos, int timer) implements CustomPayload {
      public static final Id<ALNetwork.SyncLidPayload> ID = new Id(AL.id("sync_lid"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.SyncLidPayload> CODEC = PacketCodec.tuple(
         BlockPos.PACKET_CODEC, ALNetwork.SyncLidPayload::pos,
         PacketCodecs.VAR_INT, ALNetwork.SyncLidPayload::timer, ALNetwork.SyncLidPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record ChannelPayload(boolean active, String power) implements CustomPayload {
      public static final Id<ALNetwork.ChannelPayload> ID = new Id(AL.id("channel"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.ChannelPayload> CODEC = PacketCodec.tuple(
         PacketCodecs.BOOL, ALNetwork.ChannelPayload::active,
         PacketCodecs.STRING, ALNetwork.ChannelPayload::power, ALNetwork.ChannelPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record SelectSlotPayload(int slot) implements CustomPayload {
      public static final Id<ALNetwork.SelectSlotPayload> ID = new Id(AL.id("select_slot"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.SelectSlotPayload> CODEC = PacketCodec.tuple(
         PacketCodecs.VAR_INT, ALNetwork.SelectSlotPayload::slot, ALNetwork.SelectSlotPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record AssignSlotsPayload(List<String> powers) implements CustomPayload {
      public static final Id<ALNetwork.AssignSlotsPayload> ID = new Id(AL.id("assign_slots"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.AssignSlotsPayload> CODEC = PacketCodec.tuple(
         PacketCodecs.STRING.collect(PacketCodecs.toList()), ALNetwork.AssignSlotsPayload::powers, ALNetwork.AssignSlotsPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record SyncSelectionPayload(int slot, List<String> powers) implements CustomPayload {
      public static final Id<ALNetwork.SyncSelectionPayload> ID = new Id(AL.id("sync_selection"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.SyncSelectionPayload> CODEC = PacketCodec.tuple(
         PacketCodecs.VAR_INT, ALNetwork.SyncSelectionPayload::slot,
         PacketCodecs.STRING.collect(PacketCodecs.toList()), ALNetwork.SyncSelectionPayload::powers, ALNetwork.SyncSelectionPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record EffectData(String id, int amplifier, int duration, int casterId) {
   }

   public static record SyncEffectsPayload(int entityId, List<EffectData> effects) implements CustomPayload {
      public static final Id<ALNetwork.SyncEffectsPayload> ID = new Id(AL.id("sync_effects"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.SyncEffectsPayload> CODEC = PacketCodec.tuple(
         PacketCodecs.VAR_INT, ALNetwork.SyncEffectsPayload::entityId,
         ALNetwork.EffectListCodecs.CODEC, ALNetwork.SyncEffectsPayload::effects, ALNetwork.SyncEffectsPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static final class EffectListCodecs {
      public static final PacketCodec<RegistryByteBuf, List<EffectData>> CODEC = new PacketCodec<RegistryByteBuf, List<EffectData>>() {
         public List<EffectData> decode(RegistryByteBuf buf) {
            int n = buf.readVarInt();
            List<EffectData> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
               list.add(new EffectData(buf.readString(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
            }
            return list;
         }

         public void encode(RegistryByteBuf buf, List<EffectData> value) {
            buf.writeVarInt(value.size());
            for (EffectData e : value) {
               buf.writeString(e.id());
               buf.writeVarInt(e.amplifier());
               buf.writeVarInt(e.duration());
               buf.writeVarInt(e.casterId());
            }
         }
      };

      private EffectListCodecs() {
      }
   }

   public static record ToggleSaberPayload(boolean mainHand) implements CustomPayload {
      public static final Id<ALNetwork.ToggleSaberPayload> ID = new Id(AL.id("toggle_saber"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.ToggleSaberPayload> CODEC = PacketCodec.tuple(
         PacketCodecs.STRING, payload -> payload.mainHand() ? "main" : "off",
         s -> new ALNetwork.ToggleSaberPayload(s.equals("main"))
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record SwingSaberPayload() implements CustomPayload {
      public static final Id<ALNetwork.SwingSaberPayload> ID = new Id(AL.id("swing_saber"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.SwingSaberPayload> CODEC = PacketCodec.unit(
         new ALNetwork.SwingSaberPayload()
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static volatile int swingSoundsPlayed = 0;

   private static void handleSwingSaber(ServerPlayerEntity player) {
      if (player.getMainHandStack().getItem() instanceof com.drag0nge0de.lightsabers.item.LightsaberItem
            && com.drag0nge0de.lightsabers.item.LightsaberItem.getComponent(player.getMainHandStack()).active()) {
         player.getWorld().playSound(null, player.getBlockPos(),
               com.drag0nge0de.lightsabers.registry.ALSounds.LIGHTSABER_SWING,
               net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
         swingSoundsPlayed++;
         if (Boolean.getBoolean("al.debug.swing")) {
            System.out.println("[AL] swing sound played for " + player.getName().getString()
                  + " (total " + swingSoundsPlayed + ")");
         }
      }
   }

   public static record OpenForcePowersPayload(BlockPos pos) implements CustomPayload {
      public static final Id<ALNetwork.OpenForcePowersPayload> ID = new Id(AL.id("open_force_powers"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.OpenForcePowersPayload> CODEC = PacketCodec.tuple(
         BlockPos.PACKET_CODEC, ALNetwork.OpenForcePowersPayload::pos, ALNetwork.OpenForcePowersPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record HolocronStatePayload(BlockPos pos, boolean open) implements CustomPayload {
      public static final Id<ALNetwork.HolocronStatePayload> ID = new Id(AL.id("holocron_state"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.HolocronStatePayload> CODEC = PacketCodec.tuple(
         BlockPos.PACKET_CODEC, ALNetwork.HolocronStatePayload::pos,
         PacketCodecs.BOOL, ALNetwork.HolocronStatePayload::open, ALNetwork.HolocronStatePayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record CloseHolocronPayload(BlockPos pos) implements CustomPayload {
      public static final Id<ALNetwork.CloseHolocronPayload> ID = new Id(AL.id("close_holocron"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.CloseHolocronPayload> CODEC = PacketCodec.tuple(
         BlockPos.PACKET_CODEC, ALNetwork.CloseHolocronPayload::pos, ALNetwork.CloseHolocronPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record DrainXpPayload(String power) implements CustomPayload {
      public static final Id<ALNetwork.DrainXpPayload> ID = new Id(AL.id("drain_xp"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.DrainXpPayload> CODEC = PacketCodec.tuple(
         PacketCodecs.STRING, ALNetwork.DrainXpPayload::power, ALNetwork.DrainXpPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record CastPowerPayload(String power) implements CustomPayload {
      public static final Id<ALNetwork.CastPowerPayload> ID = new Id(AL.id("force_cast"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.CastPowerPayload> CODEC = PacketCodec.tuple(
         PacketCodecs.STRING, ALNetwork.CastPowerPayload::power, ALNetwork.CastPowerPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static final class StringIntMapCodecs {
      public static final PacketCodec<RegistryByteBuf, java.util.Map<String, Integer>> CODEC = new PacketCodec<RegistryByteBuf, java.util.Map<String, Integer>>() {
         public java.util.Map<String, Integer> decode(RegistryByteBuf buf) {
            int n = buf.readVarInt();
            java.util.Map<String, Integer> map = new java.util.HashMap<>();
            for (int i = 0; i < n; i++) {
               map.put(buf.readString(), buf.readVarInt());
            }
            return map;
         }

         public void encode(RegistryByteBuf buf, java.util.Map<String, Integer> value) {
            buf.writeVarInt(value.size());
            for (java.util.Map.Entry<String, Integer> e : value.entrySet()) {
               buf.writeString(e.getKey());
               buf.writeVarInt(e.getValue());
            }
         }
      };

      private StringIntMapCodecs() {
      }
   }

   public static record SyncForcePayload(int xp, float energy, float maxEnergy, float regen, List<String> unlocked,
         java.util.Map<String, Integer> xpInvested) implements CustomPayload {
      public static final Id<ALNetwork.SyncForcePayload> ID = new Id(AL.id("force_sync"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.SyncForcePayload> CODEC = PacketCodec.tuple(
         PacketCodecs.VAR_INT,
         ALNetwork.SyncForcePayload::xp,
         PacketCodecs.FLOAT,
         ALNetwork.SyncForcePayload::energy,
         PacketCodecs.FLOAT,
         ALNetwork.SyncForcePayload::maxEnergy,
         PacketCodecs.FLOAT,
         ALNetwork.SyncForcePayload::regen,
         PacketCodecs.STRING.collect(PacketCodecs.toList()),
         ALNetwork.SyncForcePayload::unlocked,
         ALNetwork.StringIntMapCodecs.CODEC,
         ALNetwork.SyncForcePayload::xpInvested,
         ALNetwork.SyncForcePayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }

   public static record UnlockPowerPayload(String power) implements CustomPayload {
      public static final Id<ALNetwork.UnlockPowerPayload> ID = new Id(AL.id("force_unlock"));
      public static final PacketCodec<RegistryByteBuf, ALNetwork.UnlockPowerPayload> CODEC = PacketCodec.tuple(
         PacketCodecs.STRING, ALNetwork.UnlockPowerPayload::power, ALNetwork.UnlockPowerPayload::new
      );

      public Id<? extends CustomPayload> getId() {
         return ID;
      }
   }
}
