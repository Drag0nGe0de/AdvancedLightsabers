package com.drag0nge0de.lightsabers.screen;

import com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity;
import com.drag0nge0de.lightsabers.registry.ALScreens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class DisassemblyStationScreenHandler extends ScreenHandler {

    public static final int[][] SLOTS = {{16, 18}, {16, 54}};

    private final DisassemblyStationBlockEntity inventory;
    private final PropertyDelegate delegate;

    public DisassemblyStationScreenHandler(int syncId, PlayerInventory playerInventory,
            DisassemblyStationBlockEntity inventory, PropertyDelegate delegate) {
        super(ALScreens.DISASSEMBLY_STATION, syncId);
        this.inventory = inventory;
        this.delegate = delegate;

        addSlot(new ValidityChecked(inventory, DisassemblyStationBlockEntity.INPUT, SLOTS[0][0], SLOTS[0][1]));
        addSlot(new ValidityChecked(inventory, DisassemblyStationBlockEntity.FUEL, SLOTS[1][0], SLOTS[1][1]));
        for (int i = 0; i < 15; i++) {
            addSlot(new Slot(inventory, i + 2, 72 + 18 * (i % 5), 18 + 18 * (i / 5)));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 144));
        }

        addProperties(delegate);
    }

    public int progress() {
        return delegate.get(0);
    }

    public int fuelTicks() {
        return delegate.get(1);
    }

    public int maxFuelTicks() {
        return delegate.get(2);
    }

    public boolean isBurning() {
        return fuelTicks() > 0;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        int outMin = 2;
        int outMax = 16;
        int invStart = 17;
        int invMainEnd = 44;
        int invEnd = 53;

        if (slotIndex >= outMin && slotIndex <= outMax) {

            if (!this.insertItem(stack, invStart, invEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex > outMax) {

            if (DisassemblyStationBlockEntity.canDisassemble(stack)
                    && this.slots.get(DisassemblyStationBlockEntity.INPUT).canInsert(stack)) {
                if (!this.insertItem(stack, DisassemblyStationBlockEntity.INPUT,
                        DisassemblyStationBlockEntity.INPUT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (DisassemblyStationBlockEntity.isItemFuel(stack)
                    && this.slots.get(DisassemblyStationBlockEntity.FUEL).canInsert(stack)) {
                if (!this.insertItem(stack, DisassemblyStationBlockEntity.FUEL,
                        DisassemblyStationBlockEntity.FUEL + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex >= invStart && slotIndex < invMainEnd) {
                if (!this.insertItem(stack, invMainEnd, invEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex >= invMainEnd && !this.insertItem(stack, invStart, invMainEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {

            if (!this.insertItem(stack, invStart, invEnd, true)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTakeItem(player, stack);
        return original;
    }

    private static class ValidityChecked extends Slot {

        public ValidityChecked(DisassemblyStationBlockEntity inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return switch (getIndex()) {
                case DisassemblyStationBlockEntity.INPUT -> DisassemblyStationBlockEntity.canDisassemble(stack);
                case DisassemblyStationBlockEntity.FUEL -> DisassemblyStationBlockEntity.isItemFuel(stack);
                default -> false;
            };
        }
    }
}
