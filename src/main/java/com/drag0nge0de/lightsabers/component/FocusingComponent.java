package com.drag0nge0de.lightsabers.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.RegistryByteBuf;

public record FocusingComponent(FocusingCrystalType type) {

    public static final Codec<FocusingComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FocusingCrystalType.CODEC.fieldOf("type").forGetter(FocusingComponent::type)
    ).apply(instance, FocusingComponent::new));

    public static final PacketCodec<RegistryByteBuf, FocusingComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, component -> component.type().ordinal(),
            ordinal -> new FocusingComponent(FocusingCrystalType.values()[ordinal % FocusingCrystalType.values().length]));
}
