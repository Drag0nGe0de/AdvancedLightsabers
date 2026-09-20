package com.drag0nge0de.lightsabers.screen;

import com.drag0nge0de.lightsabers.item.PouchInventory;
import com.drag0nge0de.lightsabers.registry.ALScreens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class CrystalPouchScreenHandler extends ScreenHandler {

    private static final int SLOTS = PouchInventory.SIZE;

    private final PouchInventory pouch;

    public CrystalPouchScreenHandler(int syncId, PlayerInventory playerInventory,
            Inventory pouch, boolean serverSide) {
        super(ALScreens.CRYSTAL_POUCH, syncId);
        this.pouch = pouch instanceof PouchInventory p ? p : null;

        if (serverSide) {
            pouch.onOpen(playerInventory.player);
        }
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new ValidatedSlot(pouch, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 68 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 126));
        }
    }

    public static CrystalPouchScreenHandler client(int syncId, PlayerInventory playerInventory) {
        return new CrystalPouchScreenHandler(syncId, playerInventory,
                new net.minecraft.inventory.SimpleInventory(SLOTS), false);
    }

    public static CrystalPouchScreenHandler server(int syncId, PlayerInventory playerInventory) {
        return client(syncId, playerInventory);
    }

    @Override
    public boolean canUse(PlayerEntity player) {

        return this.pouch == null || this.pouch.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int pouchEnd = SLOTS;
        int invStart = SLOTS;
        int invMainEnd = SLOTS + 27;
        int invEnd = SLOTS + 36;

        if (slotIndex < pouchEnd) {
            if (!this.insertItem(stack, invStart, invEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < invMainEnd) {

            if (!this.insertItem(stack, 0, pouchEnd, false)
                    && !this.insertItem(stack, invMainEnd, invEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.insertItem(stack, 0, pouchEnd, false)
                    && !this.insertItem(stack, invStart, invMainEnd, false)) {
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

    private static class ValidatedSlot extends Slot {

        public ValidatedSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return PouchInventory.isCrystal(stack);
        }
    }
}
