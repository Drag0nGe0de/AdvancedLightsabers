package com.drag0nge0de.lightsabers.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.RegistryByteBuf;

public record HiltComponent(String hilt) {

    public static final Codec<HiltComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("hilt").forGetter(HiltComponent::hilt)
    ).apply(instance, HiltComponent::new));

    public static final PacketCodec<RegistryByteBuf, HiltComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, HiltComponent::hilt,
            HiltComponent::new);
}
