package com.drag0nge0de.lightsabers.item;

import java.util.ArrayList;
import java.util.List;

import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALItemTags;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PouchInventory implements Inventory {

    public static final int SIZE = 18;

    private final ItemStack pouch;

    public PouchInventory(ItemStack pouch) {
        this.pouch = pouch;
    }

    public static boolean isCrystal(ItemStack stack) {
        return !stack.isEmpty() && stack.isIn(ALItemTags.CRYSTALS);
    }

    private List<ItemStack> read() {
        List<ItemStack> stored = pouch.get(ALComponents.POUCH_CONTENTS);
        List<ItemStack> list = new ArrayList<>();
        if (stored != null) {
            for (int i = 0; i < SIZE; i++) {
                list.add(i < stored.size() ? stored.get(i) : ItemStack.EMPTY);
            }
        } else {
            for (int i = 0; i < SIZE; i++) {
                list.add(ItemStack.EMPTY);
            }
        }
        return list;
    }

    private void write(List<ItemStack> list) {
        pouch.set(ALComponents.POUCH_CONTENTS, List.copyOf(list));
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : read()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return read().get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        List<ItemStack> list = read();
        ItemStack stack = list.get(slot);
        if (stack.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack taken;
        if (amount >= stack.getCount()) {
            taken = stack.copy();
            list.set(slot, ItemStack.EMPTY);
        } else {
            taken = stack.split(amount);
        }
        write(list);
        return taken;
    }

    @Override
    public ItemStack removeStack(int slot) {
        List<ItemStack> list = read();
        ItemStack taken = list.get(slot);
        list.set(slot, ItemStack.EMPTY);
        write(list);
        return taken;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        List<ItemStack> list = read();
        if (stack.getCount() > getMaxCountPerStack()) {
            stack = stack.copyWithCount(getMaxCountPerStack());
        }
        list.set(slot, stack);
        write(list);
    }

    @Override
    public void markDirty() {

    }

    public ItemStack insert(ItemStack stack) {
        List<ItemStack> list = read();
        int count = stack.getCount();

        for (int i = 0; i < SIZE && count > 0; i++) {
            ItemStack cur = list.get(i);

            if (!cur.isEmpty() && ItemStack.areItemsAndComponentsEqual(cur, stack)) {
                int can = Math.min(count, cur.getMaxCount() - cur.getCount());

                if (can > 0) {
                    cur.increment(can);
                    count -= can;
                }
            }
        }

        for (int i = 0; i < SIZE && count > 0; i++) {
            if (list.get(i).isEmpty()) {
                int take = Math.min(count, stack.getMaxCount());
                list.set(i, stack.copyWithCount(take));
                count -= take;
            }
        }

        if (count < stack.getCount()) {
            write(list);
        }

        return stack.copyWithCount(count);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player.getMainHandStack() == pouch || player.getOffHandStack() == pouch;
    }

    @Override
    public void clear() {
        List<ItemStack> list = read();
        for (int i = 0; i < SIZE; i++) {
            list.set(i, ItemStack.EMPTY);
        }
        write(list);
    }
}
