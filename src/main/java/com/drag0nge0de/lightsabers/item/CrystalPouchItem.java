package com.drag0nge0de.lightsabers.item;

import com.drag0nge0de.lightsabers.screen.CrystalPouchScreenHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CrystalPouchItem extends Item {

    public CrystalPouchItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.text.Text> tooltip,
            net.minecraft.item.tooltip.TooltipType type) {
        com.drag0nge0de.lightsabers.component.CrystalComponent crystal = stack.get(com.drag0nge0de.lightsabers.registry.ALComponents.CRYSTAL);
        if (crystal != null) {
            com.drag0nge0de.lightsabers.component.CrystalColor color = com.drag0nge0de.lightsabers.component.CrystalColor.nearest(crystal.color());
            tooltip.add(net.minecraft.text.Text.translatable("lightsabers.color." + color.name().toLowerCase())
                    .formatted(net.minecraft.util.Formatting.GRAY));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack pouch = user.getStackInHand(hand);
        if (!world.isClient) {

            world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(),
                    SoundCategory.PLAYERS, 0.6F, 1.0F);
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inventory, player) -> new CrystalPouchScreenHandler(syncId, inventory,
                            new PouchInventory(pouch), true),
                    Text.translatable("gui.lightsabers.crystal_pouch")));
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, pouch);
    }
}
