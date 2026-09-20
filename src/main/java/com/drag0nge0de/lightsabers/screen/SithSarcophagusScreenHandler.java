package com.drag0nge0de.lightsabers.screen;

import com.drag0nge0de.lightsabers.block.blockentity.SithSarcophagusBlockEntity;
import com.drag0nge0de.lightsabers.registry.ALScreens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SithSarcophagusScreenHandler extends ScreenHandler {

    private static final int SLOTS = 27;

    private final Inventory inventory;

    public SithSarcophagusScreenHandler(int syncId, PlayerInventory playerInventory,
            SithSarcophagusBlockEntity blockEntity) {
        super(ALScreens.SITH_SARCOPHAGUS, syncId);
        this.inventory = blockEntity;
        layout(blockEntity, playerInventory);
    }

    public SithSarcophagusScreenHandler(int syncId, PlayerInventory playerInventory,
            net.minecraft.inventory.SimpleInventory dummy) {
        super(ALScreens.SITH_SARCOPHAGUS, syncId);
        this.inventory = dummy;
        layout(dummy, playerInventory);
    }

    public static SithSarcophagusScreenHandler server(int syncId, PlayerInventory playerInventory) {
        return new SithSarcophagusScreenHandler(syncId, playerInventory,
                new net.minecraft.inventory.SimpleInventory(new ItemStack[SLOTS]));
    }

    private void layout(Inventory container, PlayerInventory playerInventory) {
        container.onOpen(playerInventory.player);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (inventory instanceof SithSarcophagusBlockEntity be) {
            return be.canPlayerUse(player);
        }
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int containerEnd = SLOTS;
        int invStart = SLOTS;
        int invMainEnd = SLOTS + 27;
        int invEnd = SLOTS + 36;

        if (slotIndex < containerEnd) {
            if (!this.insertItem(stack, invStart, invEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < invMainEnd) {
            if (!this.insertItem(stack, invMainEnd, invEnd, false)
                    && !this.insertItem(stack, 0, containerEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.insertItem(stack, invStart, invMainEnd, false)
                    && !this.insertItem(stack, 0, containerEnd, false)) {
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
}
