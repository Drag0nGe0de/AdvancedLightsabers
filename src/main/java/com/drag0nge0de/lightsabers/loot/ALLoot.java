package com.drag0nge0de.lightsabers.loot;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.item.PartItem;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALItems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.SetComponentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class ALLoot {

    private ALLoot() {
    }

    public static void register() {
        Registry.register(Registries.LOOT_FUNCTION_TYPE, AL.id("random_saber"),
                RandomSaberLootFunction.TYPE);
        Registry.register(Registries.LOOT_FUNCTION_TYPE, AL.id("random_part"),
                RandomPartLootFunction.TYPE);
        Registry.register(Registries.LOOT_FUNCTION_TYPE, AL.id("random_focusing"),
                RandomFocusingLootFunction.TYPE);
        Registry.register(Registries.LOOT_FUNCTION_TYPE, AL.id("crystal_color"),
                CrystalColorLootFunction.TYPE);

        LootTableEvents.MODIFY.register((key, tableBuilder, source) -> {
            if (!source.isBuiltin()) {
                return;
            }
            Identifier id = key.getValue();
            if (id.equals(Identifier.ofVanilla("chests/simple_dungeon"))) {
                tableBuilder.pool(pool(60)
                        .with(part("emitter", 2))
                        .with(part("emitter", 2))
                        .with(part("emitter", 2))
                        .with(crystal("kyber_crystal", 1, CrystalColor.DEEP_BLUE))
                        .with(crystal("kyber_crystal", 1, CrystalColor.MEDIUM_BLUE))
                        .with(crystal("kyber_crystal", 1, CrystalColor.LIGHT_BLUE))
                        .with(crystal("kyber_crystal", 1, CrystalColor.MAGENTA))
                        .with(crystal("kyber_crystal", 1, CrystalColor.PINK)));
            } else if (id.equals(Identifier.ofVanilla("chests/village/village_blacksmith"))) {
                tableBuilder.pool(pool(40)
                        .with(part("emitter", 2))
                        .with(part("emitter", 2))
                        .with(part("emitter", 2))
                        .with(crystal("kyber_crystal", 1, CrystalColor.RED)));
            } else if (id.equals(Identifier.ofVanilla("chests/desert_pyramid"))) {
                tableBuilder.pool(pool(50)
                        .with(crystal("kyber_crystal", 1, CrystalColor.AMBER))
                        .with(crystal("kyber_crystal", 1, CrystalColor.YELLOW))
                        .with(crystal("kyber_crystal", 1, CrystalColor.GOLD))
                        .with(crystal("kyber_crystal", 1, CrystalColor.BLOOD_ORANGE)));
            } else if (id.equals(Identifier.ofVanilla("chests/jungle_temple"))) {
                tableBuilder.pool(pool(50)
                        .with(crystal("kyber_crystal", 1, CrystalColor.LIME_GREEN))
                        .with(crystal("kyber_crystal", 1, CrystalColor.GREEN))
                        .with(crystal("kyber_crystal", 1, CrystalColor.MINT_GREEN)));
            } else if (id.equals(Identifier.ofVanilla("chests/stronghold_library"))) {
                tableBuilder.pool(pool(50)
                        .with(crystal("kyber_crystal", 2, CrystalColor.INDIGO))
                        .with(crystal("kyber_crystal", 2, CrystalColor.PURPLE))
                        .with(crystal("kyber_crystal", 2, CrystalColor.CYAN)));
            } else if (id.equals(Identifier.ofVanilla("chests/abandoned_mineshaft"))) {
                tableBuilder.pool(pool(70)
                        .with(crystal("kyber_crystal", 3, CrystalColor.ARCTIC_BLUE))
                        .with(crystal("kyber_crystal", 3, CrystalColor.WHITE)));
            }
        });
    }

    private static LootPool.Builder pool(int emptyWeight) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1.0F))
                .with(EmptyEntry.builder().weight(emptyWeight));
    }

    private static LootPoolEntry.Builder<?> part(String name, int weight) {
        return ItemEntry.builder(partItem(name))
                .weight(weight)
                .apply(() -> new RandomPartLootFunction(name));
    }

    private static net.minecraft.item.Item partItem(String name) {
        return switch (name) {
            case "switch_section" -> ALItems.SWITCH_MODULE;
            case "body" -> ALItems.GRIP;
            default -> ALItems.EMITTER;
        };
    }

    private static LootPoolEntry.Builder<?> crystal(
            String item, int weight, CrystalColor color) {
        ItemStack def = new ItemStack(Registries.ITEM.get(AL.id(item)));
        return ItemEntry.builder(def.getItem())
                .weight(weight)
                .apply(SetComponentsLootFunction.builder(
                        ALComponents.CRYSTAL, new CrystalComponent(color.rgb)));
    }

    public record RandomSaberLootFunction(boolean allowDouble, float doubleChance,
            int firstColor, int secondColor, float secondChance) implements LootFunction {

        public static final MapCodec<RandomSaberLootFunction> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Codec.BOOL.optionalFieldOf("allow_double", true).forGetter(RandomSaberLootFunction::allowDouble),
                        Codec.FLOAT.optionalFieldOf("double_chance", 0.0F).forGetter(RandomSaberLootFunction::doubleChance),
                        Codec.INT.optionalFieldOf("first_color", CrystalColor.RED.rgb).forGetter(RandomSaberLootFunction::firstColor),
                        Codec.INT.optionalFieldOf("second_color", CrystalColor.PURPLE.rgb).forGetter(RandomSaberLootFunction::secondColor),
                        Codec.FLOAT.optionalFieldOf("second_chance", 0.2F).forGetter(RandomSaberLootFunction::secondChance))
                        .apply(instance, RandomSaberLootFunction::new));

        public static final LootFunctionType<RandomSaberLootFunction> TYPE =
                new LootFunctionType<>(CODEC);

        @Override
        public ItemStack apply(ItemStack stack, LootContext context) {
            Random random = context.getRandom();
            Hilt hilt = Hilt.values()[random.nextInt(Hilt.values().length)];
            int color = random.nextFloat() < this.secondChance
                    ? this.secondColor
                    : this.firstColor;
            boolean doubled = this.allowDouble && random.nextFloat() < this.doubleChance;
            ItemStack saber = new ItemStack(doubled ? ALItems.DOUBLE_LIGHTSABER : ALItems.LIGHTSABER);

            int focusing = 0;
            if (random.nextInt(10) == 0) {
                com.drag0nge0de.lightsabers.component.FocusingCrystalType[] types =
                        com.drag0nge0de.lightsabers.component.FocusingCrystalType.values();
                int first = random.nextInt(types.length);
                focusing |= 1 << first;
                if (random.nextInt(20) == 0) {
                    int second = random.nextInt(types.length - 1);
                    if (second >= first) second++;
                    focusing |= 1 << second;
                }
            }
            saber.set(ALComponents.LIGHTSABER, new LightsaberComponent(false, hilt.getId(), color, doubled, focusing));
            saber.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));
            return saber;
        }

        @Override
        public LootFunctionType<? extends LootFunction> getType() {
            return TYPE;
        }
    }

    public record RandomFocusingLootFunction() implements LootFunction {

        public static final MapCodec<RandomFocusingLootFunction> CODEC = MapCodec.unit(
                RandomFocusingLootFunction::new);

        public static final LootFunctionType<RandomFocusingLootFunction> TYPE =
                new LootFunctionType<>(CODEC);

        @Override
        public ItemStack apply(ItemStack stack, LootContext context) {
            com.drag0nge0de.lightsabers.component.FocusingCrystalType[] types =
                    com.drag0nge0de.lightsabers.component.FocusingCrystalType.values();
            com.drag0nge0de.lightsabers.component.FocusingCrystalType type =
                    types[context.getRandom().nextInt(types.length)];
            com.drag0nge0de.lightsabers.item.FocusingCrystalItem.applyType(stack, type);
            return stack;
        }

        @Override
        public LootFunctionType<? extends LootFunction> getType() {
            return TYPE;
        }
    }

    public record RandomPartLootFunction(String part) implements LootFunction {

        public static final MapCodec<RandomPartLootFunction> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Codec.STRING.fieldOf("part").forGetter(RandomPartLootFunction::part))
                        .apply(instance, RandomPartLootFunction::new));

        public static final LootFunctionType<RandomPartLootFunction> TYPE =
                new LootFunctionType<>(CODEC);

        @Override
        public ItemStack apply(ItemStack stack, LootContext context) {
            Random random = context.getRandom();
            Hilt hilt = Hilt.values()[random.nextInt(Hilt.values().length)];
            return PartItem.create(this.part, hilt);
        }

        @Override
        public LootFunctionType<? extends LootFunction> getType() {
            return TYPE;
        }
    }
}
