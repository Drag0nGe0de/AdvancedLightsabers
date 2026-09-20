package com.drag0nge0de.lightsabers.client.screen;

import com.drag0nge0de.lightsabers.screen.CrystalPouchScreenHandler;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CrystalPouchScreen extends HandledScreen<CrystalPouchScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/generic_54.png");
    private static final int ROWS = 2;

    public CrystalPouchScreen(CrystalPouchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 114 + ROWS * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    protected void drawBackground(net.minecraft.client.gui.DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, ROWS * 18 + 17);
        context.drawTexture(TEXTURE, x, y + ROWS * 18 + 17, 0, 126, this.backgroundWidth, 96);
    }

    @Override
    protected void drawForeground(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
        context.drawText(this.textRenderer, net.minecraft.text.Text.translatable("container.inventory"),
                this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040, false);
    }
}
