package com.drag0nge0de.lightsabers.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record CrystalComponent(int color) {
   public static final CrystalComponent DEFAULT = new CrystalComponent(16777215);
   public static final Codec<CrystalComponent> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(Codec.INT.fieldOf("color").forGetter(CrystalComponent::color)).apply(instance, CrystalComponent::new)
   );
   public static final PacketCodec<RegistryByteBuf, CrystalComponent> PACKET_CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT, CrystalComponent::color, CrystalComponent::new
   );
}
