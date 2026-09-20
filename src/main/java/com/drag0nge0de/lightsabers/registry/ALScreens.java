package com.drag0nge0de.lightsabers.registry;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.screen.CrystalPouchScreenHandler;
import com.drag0nge0de.lightsabers.screen.DisassemblyStationScreenHandler;
import com.drag0nge0de.lightsabers.screen.LightsaberForgeScreenHandler;
import com.drag0nge0de.lightsabers.screen.SithSarcophagusScreenHandler;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class ALScreens {

    public static final ScreenHandlerType<LightsaberForgeScreenHandler> LIGHTSABER_FORGE =
            register("lightsaber_forge",
                    (syncId, inventory) -> new LightsaberForgeScreenHandler(syncId, inventory,
                            net.minecraft.screen.ScreenHandlerContext.EMPTY));

    public static final ScreenHandlerType<DisassemblyStationScreenHandler> DISASSEMBLY_STATION =
            register("disassembly_station",
                    (syncId, inventory) -> new DisassemblyStationScreenHandler(syncId, inventory,
                            new com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity(
                                    net.minecraft.util.math.BlockPos.ORIGIN,
                                    com.drag0nge0de.lightsabers.registry.ALBlocks.DISASSEMBLY_STATION.getDefaultState()),
                            new net.minecraft.screen.ArrayPropertyDelegate(3)));

    public static final ScreenHandlerType<SithSarcophagusScreenHandler> SITH_SARCOPHAGUS =
            register("sith_sarcophagus", (syncId, inventory) ->
                    SithSarcophagusScreenHandler.server(syncId, inventory));

    public static final ScreenHandlerType<CrystalPouchScreenHandler> CRYSTAL_POUCH =
            register("crystal_pouch", (syncId, inventory) ->
                    CrystalPouchScreenHandler.client(syncId, inventory));

    private static <T extends net.minecraft.screen.ScreenHandler> ScreenHandlerType<T> register(
            String name, ScreenHandlerType.Factory<T> factory) {
        ScreenHandlerType<T> type = new ScreenHandlerType<T>(factory,
                net.minecraft.screen.ScreenHandlerType.GENERIC_9X1.getRequiredFeatures());
        return Registry.register(Registries.SCREEN_HANDLER, AL.id(name), type);
    }

    public static void register() {
    }
}
