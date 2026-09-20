package com.drag0nge0de.lightsabers.block.blockentity;

import java.util.LinkedHashMap;
import java.util.Map;

import com.drag0nge0de.lightsabers.block.DisassemblyStationBlock;
import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.FocusingComponent;
import com.drag0nge0de.lightsabers.component.FocusingCrystalType;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.item.PartItem;
import com.drag0nge0de.lightsabers.registry.ALBlockEntities;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALItems;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class DisassemblyStationBlockEntity extends BlockEntity
        implements SidedInventory, net.minecraft.screen.NamedScreenHandlerFactory {

    public static final int TICKS_DISASSEMBLY = 2400;
    public static final int INPUT = 0;
    public static final int FUEL = 1;
    public static final int SIZE = 17;

    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);

    private final net.minecraft.screen.PropertyDelegate propertyDelegate = new net.minecraft.screen.PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> fuelTicks;
                case 2 -> maxFuelTicks;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> fuelTicks = value;
                case 2 -> maxFuelTicks = value;
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public int fuelTicks;
    public int maxFuelTicks;
    public int progress;

    private static final int[] TOP_SLOTS = {INPUT};
    private static final int[] BOTTOM_SLOTS;
    private static final int[] SIDE_SLOTS = {INPUT, FUEL};

    static {
        BOTTOM_SLOTS = new int[SIZE - 1];
        for (int i = 2; i < SIZE; i++) {
            BOTTOM_SLOTS[i - 2] = i;
        }
        BOTTOM_SLOTS[SIZE - 2] = FUEL;
    }

    public DisassemblyStationBlockEntity(BlockPos pos, BlockState state) {
        super(ALBlockEntities.DISASSEMBLY_STATION, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, DisassemblyStationBlockEntity be) {
        boolean burning = be.fuelTicks > 0;
        boolean dirty = false;

        if (be.fuelTicks > 0) {
            --be.fuelTicks;
        }

        if (!world.isClient) {
            if (be.fuelTicks != 0 || (!be.stacks.get(FUEL).isEmpty() && !be.stacks.get(INPUT).isEmpty())) {
                if (be.fuelTicks == 0 && canDisassemble(be.stacks.get(INPUT))
                        && (be.maxFuelTicks = be.fuelTicks = getFuelValue(be.stacks.get(FUEL))) > 0) {
                    dirty = true;
                    ItemStack fuel = be.stacks.get(FUEL);
                    fuel.decrement(1);
                    if (fuel.isEmpty()) {
                        be.stacks.set(FUEL, ItemStack.EMPTY);
                    }
                }

                if (be.fuelTicks > 0 && canDisassemble(be.stacks.get(INPUT))) {
                    ++be.progress;
                    if (be.progress >= TICKS_DISASSEMBLY) {
                        be.progress = 0;
                        be.disassembleItem();
                        dirty = true;
                        world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                    }
                } else {
                    be.progress = 0;
                }
            }

            if (burning != be.fuelTicks > 0) {
                dirty = true;
                be.markDirty();

                if (state.contains(DisassemblyStationBlock.LIT)
                        && state.get(DisassemblyStationBlock.LIT) != be.isBurning()) {
                    world.setBlockState(pos, state.with(DisassemblyStationBlock.LIT, be.isBurning()));
                }
            }
        }

        if (dirty) {
            be.markDirty();
        }
    }

    public boolean isBurning() {
        return fuelTicks > 0;
    }

    public int getCookProgressScaled(int scale) {
        return progress * scale / TICKS_DISASSEMBLY;
    }

    public int getBurnTimeRemainingScaled(int scale) {
        if (maxFuelTicks == 0) {
            maxFuelTicks = 200;
        }
        return fuelTicks * scale / maxFuelTicks;
    }

    public static boolean canDisassemble(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof LightsaberItem;
    }

    public void disassembleItem() {
        ItemStack in = stacks.get(INPUT);
        if (!canDisassemble(in)) {
            return;
        }
        for (Map.Entry<ItemStack, Float> e : getOutput(in).entrySet()) {
            if (e.getValue() > world.getRandom().nextFloat()) {
                addOutputItem(e.getKey().copy());
            }
        }
        in.decrement(1);
        if (in.isEmpty()) {
            stacks.set(INPUT, ItemStack.EMPTY);
        }
    }

    public void addOutputItem(ItemStack stack) {
        for (int i = 2; i < SIZE; i++) {
            ItemStack cur = stacks.get(i);
            if (cur.isEmpty()) {
                stacks.set(i, stack.copy());
                return;
            }
            if (ItemStack.areItemsAndComponentsEqual(cur, stack)) {
                int j = Math.min(stack.getCount(), stack.getMaxCount() - cur.getCount());
                if (j > 0) {
                    cur.increment(j);
                    stack.decrement(j);
                }
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

        if (world != null) {
            net.minecraft.util.ItemScatterer.spawn(world, pos, new SimpleInventory(stack));
        }
    }

    public static Map<ItemStack, Float> getOutput(ItemStack stack) {
        LightsaberComponent data = LightsaberItem.getComponent(stack);

        if (data.doubleSaber() && data.second().isPresent()) {
            Map<ItemStack, Float> drops = new LinkedHashMap<>(getOutput(data));
            drops.putAll(getOutput(secondBladeComponent(data)));
            return drops;
        }

        return getOutput(data);
    }

    private static LightsaberComponent secondBladeComponent(LightsaberComponent data) {
        LightsaberComponent.Blade blade = data.second().get();
        return new LightsaberComponent(false, blade.emitterHilt(), blade.switchHilt(), blade.gripHilt(),
            blade.pommelHilt(), blade.color(), false, blade.focusing());
    }

    private static Map<ItemStack, Float> getOutput(LightsaberComponent data) {
        Map<ItemStack, Float> drops = new LinkedHashMap<>();
        drops.put(new ItemStack(ALItems.CIRCUITRY), 0.25F);

        String[] hilts = data.hilts();

        if (data.isHiltUniform()) {
            Hilt hilt = Hilt.byName(hilts[2]);
            drops.put(PartItem.create("emitter", hilt), 1.0F);
            drops.put(PartItem.create("switch_section", hilt), 1.0F);
            drops.put(PartItem.create("body", hilt), 1.0F);
            drops.put(PartItem.create("pommel", hilt), 1.0F);
        } else {
            Map<String, Integer> seen = new java.util.HashMap<>();
            String[] parts = {"emitter", "switch_section", "body", "pommel"};

            for (int i = 0; i < 4; i++) {
                int occurrence = seen.getOrDefault(hilts[i], 0);
                seen.put(hilts[i], occurrence + 1);
                drops.put(PartItem.create(parts[i], Hilt.byName(hilts[i])), 0.66F + 0.05F * occurrence);
            }
        }

        for (FocusingCrystalType type : FocusingCrystalType.values()) {
            if ((data.focusing() & type.mask()) != 0) {
                ItemStack crystal = new ItemStack(ALItems.FOCUSING_CRYSTAL);
                crystal.set(ALComponents.FOCUSING, new FocusingComponent(type));
                drops.put(crystal, 0.725F);
            }
        }

        if (data.special() == null || data.special().isEmpty()) {
            CrystalColor color = CrystalColor.nearest(data.color());
            drops.put(crystalStack(color), 0.35F + 0.125F * rarity(color).ordinal());
        }

        return drops;
    }

    private static ItemStack crystalStack(CrystalColor color) {
        return com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(color);
    }

    public static Rarity rarity(CrystalColor color) {
        return switch (color) {
            case INDIGO, PURPLE, CYAN -> Rarity.RARE;
            case ARCTIC_BLUE, WHITE -> Rarity.EPIC;
            default -> Rarity.COMMON;
        };
    }

    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC
    }
    public static int getFuelValue(ItemStack stack) {
        if (stack.isOf(Items.REDSTONE)) {
            return 300;
        }
        if (stack.isOf(Items.REDSTONE_BLOCK)) {
            return 2700;
        }
        return 0;
    }

    public static boolean isItemFuel(ItemStack stack) {
        return getFuelValue(stack) > 0;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return side == Direction.DOWN ? BOTTOM_SLOTS : side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot == INPUT && canDisassemble(stack) || slot == FUEL && isItemFuel(stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir != Direction.DOWN || slot != FUEL;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(stacks, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(stacks, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
        return world != null && world.getBlockEntity(pos) == this
                && player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot == INPUT && canDisassemble(stack) || slot == FUEL && isItemFuel(stack);
    }

    @Override
    public void clear() {
        stacks.clear();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        Inventories.writeNbt(nbt, stacks, lookup);
        nbt.putShort("BurnTime", (short) fuelTicks);
        nbt.putShort("DisassemblyTime", (short) progress);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        Inventories.readNbt(nbt, stacks, lookup);
        fuelTicks = nbt.getShort("BurnTime");
        progress = nbt.getShort("DisassemblyTime");
        maxFuelTicks = getFuelValue(stacks.get(FUEL));
    }

    @Override
    public net.minecraft.screen.ScreenHandler createMenu(int syncId,
            net.minecraft.entity.player.PlayerInventory playerInventory,
            net.minecraft.entity.player.PlayerEntity player) {
        return new com.drag0nge0de.lightsabers.screen.DisassemblyStationScreenHandler(
                syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public net.minecraft.text.Text getDisplayName() {
        return net.minecraft.text.Text.translatable("gui.lightsabers.disassembly_station");
    }
}
