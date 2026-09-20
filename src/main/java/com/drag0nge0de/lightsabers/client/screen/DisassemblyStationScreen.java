package com.drag0nge0de.lightsabers.client.screen;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.screen.DisassemblyStationScreenHandler;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DisassemblyStationScreen extends HandledScreen<DisassemblyStationScreenHandler> {

    private static final Identifier TEXTURE = AL.id("textures/gui/container/disassembly_station.png");

    public DisassemblyStationScreen(DisassemblyStationScreenHandler handler, PlayerInventory inventory,
            Text title) {
        super(handler, inventory, title);
        backgroundHeight = 168;
        playerInventoryTitleY = backgroundHeight - 94;
        titleY = 6;
    }

    @Override
    protected void init() {
        super.init();

        titleX = backgroundWidth / 2 - textRenderer.getWidth(title) / 2;
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(net.minecraft.client.gui.DrawContext context, float delta, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        if (handler.isBurning()) {
            int i = burnScaled(13);

            context.drawTexture(TEXTURE, x + 17, y + 37 + 12 - i, 176, 12 - i, 14, i + 2);

            i = progressScaled(24);

            context.drawTexture(TEXTURE, x + 39, y + 36, 176, 14, i + 1, 16);
        }
    }

    private int burnScaled(int scale) {
        int max = handler.maxFuelTicks();
        if (max == 0) {
            max = 200;
        }
        return handler.fuelTicks() * scale / max;
    }

    private int progressScaled(int scale) {
        return handler.progress() * scale / com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity.TICKS_DISASSEMBLY;
    }
}
