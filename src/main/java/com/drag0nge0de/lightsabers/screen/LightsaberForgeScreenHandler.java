package com.drag0nge0de.lightsabers.screen;

import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.FocusingCrystalType;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.hilt.HiltStats;
import com.drag0nge0de.lightsabers.hilt.HiltStatsTable;
import com.drag0nge0de.lightsabers.item.FocusingCrystalItem;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.item.PartItem;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.registry.ALScreens;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class LightsaberForgeScreenHandler extends ScreenHandler {

    public static final int[][] SLOTS = {{20, 17}, {20, 35}, {20, 53}, {20, 71},
            {43, 71}, {66, 71}, {89, 71}, {107, 71}};
    public static final int[] OUTPUT = {136, 87};

    private static final String[] PART_FOR_SLOT = {"emitter", "switch_section", "body", "pommel"};

    public final SimpleInventory input = new SimpleInventory(8) {
        @Override
        public void markDirty() {
            super.markDirty();
            onInputChange();
        }
    };
    public final CraftingResultInventory result = new CraftingResultInventory();

    private final ScreenHandlerContext context;

    public LightsaberComponent assembled;
    public boolean tooShort;
    public boolean special;

    public LightsaberForgeScreenHandler(int syncId, PlayerInventory playerInventory,
            ScreenHandlerContext context) {
        super(ALScreens.LIGHTSABER_FORGE, syncId);
        this.context = context;

        for (int i = 0; i < SLOTS.length; i++) {
            addSlot(new InputSlot(input, i, SLOTS[i][0], SLOTS[i][1]));
        }
        addSlot(new ForgeOutputSlot());

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 114 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 172));
        }

        onInputChange();
    }

    private void onInputChange() {
        ItemStack kyber = input.getStack(5);
        ItemStack[] parts = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            parts[i] = input.getStack(i);
        }

        assembled = null;
        tooShort = false;
        special = false;

        boolean fish = isFish(kyber);
        if (!fish && (kyber.isEmpty() || !kyber.contains(ALComponents.CRYSTAL))) {
            updateResultSlot();
            return;
        }

        if (!(input.getStack(4).getItem() == ALItems.CIRCUITRY)) {
            updateResultSlot();
            return;
        }

        String[] hilts = new String[4];
        int focusing = 0;
        for (int i = 0; i < 4; i++) {
            if (parts[i].isEmpty()) {
                updateResultSlot();
                return;
            }
            hilts[i] = PartItem.getHilt(parts[i]).getId();
        }

        for (int slot : new int[]{6, 7}) {
            ItemStack stack = input.getStack(slot);
            if (!stack.isEmpty() && stack.contains(ALComponents.FOCUSING)) {
                FocusingCrystalType type = stack.get(ALComponents.FOCUSING).type();
                focusing |= type.mask();
            }
        }

        int color = fish
                ? CrystalColor.values()[0].rgb
                : kyber.get(ALComponents.CRYSTAL).color();
        specialId = fish ? fishId(kyber) : "";
        assembled = new LightsaberComponent(false, hilts[0], hilts[1], hilts[2], hilts[3], color, false, focusing, null, specialId);
        tooShort = mixedHeight(hilts) * 0.575F < LightsaberItem.MIN_LENGTH_CM;

        updateResultSlot();
    }

    private String specialId = "";

    public static float mixedHeight(String[] hilts) {
        return HiltStatsTable.stats(hilts[0]).emitterH()
                + HiltStatsTable.stats(hilts[1]).switchH()
                + HiltStatsTable.stats(hilts[2]).bodyH()
                + HiltStatsTable.stats(hilts[3]).pommelH();
    }

    private static boolean isFish(ItemStack stack) {
        return !stack.isEmpty() && (stack.isOf(Items.COD) || stack.isOf(Items.SALMON)
                || stack.isOf(Items.TROPICAL_FISH) || stack.isOf(Items.PUFFERFISH));
    }

    private static String fishId(ItemStack stack) {
        return net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
    }

    private void updateResultSlot() {
        if (assembled != null) {
            ItemStack stack = new ItemStack(ALItems.LIGHTSABER);
            stack.set(ALComponents.LIGHTSABER, assembled);
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));
            if (special) {
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("FISHSTICKS!!"));
                stack.set(DataComponentTypes.RARITY, Rarity.RARE);
            }
            result.setStack(0, stack);
        } else {
            result.setStack(0, ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int outputIndex = 8;
        int invStart = 9;
        int hotbarStart = 36;
        int invEnd = 45;

        if (slotIndex == outputIndex) {
            if (!this.insertItem(stack, invStart, invEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= invStart && slotIndex < invEnd) {
            boolean moved = false;
            for (int i = 0; i < 8; i++) {
                Slot target = this.slots.get(i);
                if (target instanceof InputSlot in && in.canInsert(stack) && !target.hasStack()) {
                    this.insertItem(stack, i, i + 1, false);
                    moved = true;
                    break;
                }
            }

            if (!moved) {
                boolean ok = slotIndex >= hotbarStart
                        ? this.insertItem(stack, invStart, hotbarStart, false)
                        : this.insertItem(stack, hotbarStart, invEnd, false);
                if (!ok) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if (!this.insertItem(stack, invStart, invEnd, false)) {
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

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (!player.getWorld().isClient) {
            for (int i = 0; i < input.size(); i++) {
                ItemStack stack = input.removeStack(i);
                if (!stack.isEmpty()) {
                    player.dropItem(stack, false);
                }
            }
        }
    }

    public static String partFor(int slot) {
        return PART_FOR_SLOT[slot];
    }

    public static boolean canInsertInSlot(ItemStack stack, int slot) {
        Item item = stack.getItem();
        if (slot < 4) {
            return item instanceof PartItem
                    && ((PartItem) item).getPart().equals(PART_FOR_SLOT[slot]);
        }

        if (slot == 4) {
            return item == ALItems.CIRCUITRY;
        }
        if (slot == 5) {

            return stack.contains(ALComponents.CRYSTAL) || isFish(stack);
        }
        return item instanceof FocusingCrystalItem;
    }

    private class InputSlot extends Slot {

        public InputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            if (!canInsertInSlot(stack, getIndex())) {
                return false;
            }

            for (int i = 0; i < input.size(); i++) {
                ItemStack other = input.getStack(i);
                if (!other.isEmpty() && other.getItem() == stack.getItem()
                        && ItemStack.areItemsAndComponentsEqual(other, stack)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class ForgeOutputSlot extends Slot {
        public ForgeOutputSlot() {
            super(result, 0, OUTPUT[0], OUTPUT[1]);
        }

        @Override
        public boolean canTakeItems(PlayerEntity player) {
            return assembled != null && !tooShort;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            for (int i = 0; i < input.size(); i++) {
                input.removeStack(i, 1);
            }
        }
    }
}
