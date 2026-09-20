package com.drag0nge0de.lightsabers.component;

import com.mojang.serialization.Codec;

import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;

public enum FocusingCrystalType implements StringIdentifiable {
    COMPRESSED("compressed"),
    CRACKED("cracked"),
    INVERTING("inverting"),
    FINE_CUT("fine_cut"),
    PRISMATIC("prismatic");

    public static final Codec<FocusingCrystalType> CODEC = StringIdentifiable.createCodec(FocusingCrystalType::values);

    private final String id;

    FocusingCrystalType(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return id;
    }

    public int mask() {
        return 1 << ordinal();
    }
}
