package com.drag0nge0de.lightsabers.component;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.RegistryByteBuf;

public record LightsaberComponent(boolean active, String emitterHilt, String switchHilt, String gripHilt,
        String pommelHilt, int color, boolean doubleSaber, int focusing, java.util.Optional<Blade> second, String special) {

    public record Blade(String emitterHilt, String switchHilt, String gripHilt, String pommelHilt, int color, int focusing) {

        public static final Codec<Blade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("emitter_hilt", "").forGetter(Blade::emitterHilt),
                Codec.STRING.optionalFieldOf("switch_hilt", "").forGetter(Blade::switchHilt),
                Codec.STRING.fieldOf("hilt").forGetter(Blade::gripHilt),
                Codec.STRING.optionalFieldOf("pommel_hilt", "").forGetter(Blade::pommelHilt),
                Codec.INT.fieldOf("color").forGetter(Blade::color),
                Codec.INT.optionalFieldOf("focusing", 0).forGetter(Blade::focusing)
        ).apply(instance, Blade::new));

        public static final PacketCodec<RegistryByteBuf, Blade> PACKET_CODEC = new PacketCodec<>() {
            public Blade decode(RegistryByteBuf buf) {
                String emitter = buf.readString();
                String switchH = buf.readString();
                String grip = buf.readString();
                String pommel = buf.readString();
                int color = buf.readVarInt();
                int focusing = buf.readVarInt();
                return new Blade(emitter, switchH, grip, pommel, color, focusing);
            }

            public void encode(RegistryByteBuf buf, Blade value) {
                buf.writeString(value.emitterHilt());
                buf.writeString(value.switchHilt());
                buf.writeString(value.gripHilt());
                buf.writeString(value.pommelHilt());
                buf.writeVarInt(value.color());
                buf.writeVarInt(value.focusing());
            }
        };

        public static Blade of(LightsaberComponent component) {
            return new Blade(component.emitterHilt, component.switchHilt, component.gripHilt,
                component.pommelHilt, component.color, component.focusing);
        }

        public Blade {
            if (emitterHilt == null || emitterHilt.isEmpty()) {
                emitterHilt = gripHilt;
            }
            if (switchHilt == null || switchHilt.isEmpty()) {
                switchHilt = gripHilt;
            }
            if (pommelHilt == null || pommelHilt.isEmpty()) {
                pommelHilt = gripHilt;
            }
        }

        public String[] hilts() {
            return new String[]{emitterHilt, switchHilt, gripHilt, pommelHilt};
        }
    }

    public LightsaberComponent(boolean active, String emitterHilt, String switchHilt, String gripHilt,
            String pommelHilt, int color, boolean doubleSaber, int focusing) {
        this(active, emitterHilt, switchHilt, gripHilt, pommelHilt, color, doubleSaber, focusing, java.util.Optional.empty(), "");
    }

    public static final LightsaberComponent DEFAULT = new LightsaberComponent(false, "graflex", 0xFFFFFF, false, 0);

    public static final Codec<LightsaberComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("active").forGetter(LightsaberComponent::active),
            Codec.STRING.optionalFieldOf("emitter_hilt", "").forGetter(LightsaberComponent::emitterHilt),
            Codec.STRING.optionalFieldOf("switch_hilt", "").forGetter(LightsaberComponent::switchHilt),
            Codec.STRING.fieldOf("hilt").forGetter(LightsaberComponent::gripHilt),
            Codec.STRING.optionalFieldOf("pommel_hilt", "").forGetter(LightsaberComponent::pommelHilt),
            Codec.INT.fieldOf("color").forGetter(LightsaberComponent::color),
            Codec.BOOL.fieldOf("double").forGetter(LightsaberComponent::doubleSaber),
            Codec.INT.optionalFieldOf("focusing", 0).forGetter(LightsaberComponent::focusing),
            Blade.CODEC.optionalFieldOf("second_blade").forGetter(LightsaberComponent::second),
            Codec.STRING.optionalFieldOf("special", "").forGetter(LightsaberComponent::special)
    ).apply(instance, LightsaberComponent::new));

    public static final PacketCodec<RegistryByteBuf, LightsaberComponent> PACKET_CODEC = new PacketCodec<>() {
        public LightsaberComponent decode(RegistryByteBuf buf) {
            boolean active = buf.readBoolean();
            String emitter = buf.readString();
            String switchH = buf.readString();
            String grip = buf.readString();
            String pommel = buf.readString();
            int color = buf.readVarInt();
            boolean doubleSaber = buf.readBoolean();
            int focusing = buf.readVarInt();
            java.util.Optional<Blade> second = buf.readBoolean()
               ? java.util.Optional.of(Blade.PACKET_CODEC.decode(buf))
               : java.util.Optional.empty();
            String special = buf.readString();
            return new LightsaberComponent(active, emitter, switchH, grip, pommel, color, doubleSaber, focusing, second, special);
        }

        public void encode(RegistryByteBuf buf, LightsaberComponent value) {
            buf.writeBoolean(value.active());
            buf.writeString(value.emitterHilt());
            buf.writeString(value.switchHilt());
            buf.writeString(value.gripHilt());
            buf.writeString(value.pommelHilt());
            buf.writeVarInt(value.color());
            buf.writeBoolean(value.doubleSaber());
            buf.writeVarInt(value.focusing());
            buf.writeBoolean(value.second().isPresent());

            if (value.second().isPresent()) {
                Blade.PACKET_CODEC.encode(buf, value.second().get());
            }

            buf.writeString(value.special() == null ? "" : value.special());
        }
    };

    public LightsaberComponent(boolean active, String hilt, int color, boolean doubleSaber, int focusing) {
        this(active, hilt, hilt, hilt, hilt, color, doubleSaber, focusing, java.util.Optional.empty(), "");
    }

    public LightsaberComponent(boolean active, String hilt, int color, boolean doubleSaber) {
        this(active, hilt, hilt, hilt, hilt, color, doubleSaber, 0, java.util.Optional.empty(), "");
    }

    public LightsaberComponent {
        if (emitterHilt == null || emitterHilt.isEmpty()) {
            emitterHilt = gripHilt;
        }
        if (switchHilt == null || switchHilt.isEmpty()) {
            switchHilt = gripHilt;
        }
        if (pommelHilt == null || pommelHilt.isEmpty()) {
            pommelHilt = gripHilt;
        }
        if (special == null) {
            special = "";
        }
        second = second == null ? java.util.Optional.empty() : second;
    }

    public String hilt() {
        return gripHilt;
    }

    public String[] hilts() {
        return new String[]{emitterHilt, switchHilt, gripHilt, pommelHilt};
    }

    public boolean isHiltUniform() {
        return emitterHilt.equals(switchHilt) && switchHilt.equals(gripHilt) && gripHilt.equals(pommelHilt);
    }

    public LightsaberComponent withActive(boolean active) {
        return new LightsaberComponent(active, emitterHilt, switchHilt, gripHilt, pommelHilt, color, doubleSaber, focusing, second, special);
    }

    public LightsaberComponent withColor(int newColor) {
        return new LightsaberComponent(active, emitterHilt, switchHilt, gripHilt, pommelHilt, newColor, doubleSaber, focusing, second, special);
    }

    public LightsaberComponent withFocusing(int mask) {
        return new LightsaberComponent(active, emitterHilt, switchHilt, gripHilt, pommelHilt, color, doubleSaber, mask, second, special);
    }

    public LightsaberComponent withSecond(Blade newSecond) {
        return new LightsaberComponent(active, emitterHilt, switchHilt, gripHilt, pommelHilt, color, doubleSaber, focusing,
            java.util.Optional.ofNullable(newSecond), special);
    }

    public boolean hasFocusing(int maskBit) {
        return (focusing & (1 << maskBit)) != 0;
    }
}
