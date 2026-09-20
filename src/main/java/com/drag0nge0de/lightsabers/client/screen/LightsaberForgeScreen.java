package com.drag0nge0de.lightsabers.client.screen;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.client.render.ItemRenderers;
import com.drag0nge0de.lightsabers.client.render.model.HiltModels;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.item.PartItem;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.screen.LightsaberForgeScreenHandler;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LightsaberForgeScreen extends HandledScreen<LightsaberForgeScreenHandler> {

    private static final Identifier TEXTURE = AL.id("textures/gui/container/lightsaber_forge.png");

    private static final Identifier OUTLINE = AL.id("textures/item/focusing_crystal_outline.png");

    public LightsaberForgeScreen(LightsaberForgeScreenHandler handler, PlayerInventory inventory,
            Text title) {
        super(handler, inventory, title);
        backgroundHeight = 196;
        playerInventoryTitleY = 102;
        titleY = 6;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        com.drag0nge0de.lightsabers.client.ALPerf.frameBegin("LightsaberForgeScreen");
        com.drag0nge0de.lightsabers.client.ALPerf.section("preframe");
        super.render(context, mouseX, mouseY, delta);
        com.drag0nge0de.lightsabers.client.ALPerf.section("tooltip");
        drawMouseoverTooltip(context, mouseX, mouseY);
        com.drag0nge0de.lightsabers.client.ALPerf.frameEnd();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        com.drag0nge0de.lightsabers.client.ALPerf.section("preview3d");
        LightsaberComponent assembled = handler.assembled;
        if (assembled != null) {

            context.enableScissor(x + 43, y + 17, x + 156, y + 64);
            float spin = client.player.age + delta;
            MatrixStack matrices = new MatrixStack();
            matrices.translate(x + 110, y + 40, 150);
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y
                    .rotationDegrees((float) Math.sin(spin / 20) * 2.5F));
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z
                    .rotationDegrees((float) Math.sin(spin / 20 + 2) * 2.5F));
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-90));
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(90 + spin));
            matrices.scale(-20, 20, 20);

            LightsaberComponent preview = assembled.withActive(true);
            com.drag0nge0de.lightsabers.client.render.HiltRenderer.renderSaber(matrices,
                    context.getVertexConsumers(), preview, 0xF000F0,
                    net.minecraft.client.render.OverlayTexture.DEFAULT_UV, false);
            context.getVertexConsumers().draw();
            context.disableScissor();

            float cm = LightsaberForgeScreenHandler.mixedHeight(assembled.hilts()) * 0.575F;
            if (handler.tooShort) {
                context.drawTexture(TEXTURE, x + 131, y + 65, 176, 0, 26, 17);
                context.drawTextWithShadow(textRenderer,
                        Text.translatable("gui.lightsabers.lightsaber_forge.too_short"),
                        x + 45, y + 55, 0xD74848);
            } else {
                context.drawTextWithShadow(textRenderer,
                        String.format("%.2f cm", cm), x + 45, y + 55, 0xFFFFFF);
            }
        } else {

            Hilt placed = ghostHilt();
            Hilt shown = placed != null ? placed
                    : Hilt.values()[client.player.age / 20 % Hilt.values().length];
            String hiltId = shown.getId();
            var vertexConsumers = context.getVertexConsumers();
            MatrixStack matrices = context.getMatrices();
            for (int slot = 0; slot < 4; slot++) {
                if (handler.input.getStack(slot).isEmpty()) {
                    int gx = x + LightsaberForgeScreenHandler.SLOTS[slot][0];
                    int gy = y + LightsaberForgeScreenHandler.SLOTS[slot][1];
                    String part = LightsaberForgeScreenHandler.partFor(slot);
                    matrices.push();
                    matrices.translate(gx + 8, gy + 8, 150);
                    matrices.scale(16, -16, 16);
                    ItemRenderers.Part.partGuiPose(matrices, hiltId, part);
                    var vc = vertexConsumers.getBuffer(
                            net.minecraft.client.render.RenderLayer.getEntityTranslucent(
                                    com.drag0nge0de.lightsabers.client.render.HiltRenderer.texture(hiltId, part)));

                    com.drag0nge0de.lightsabers.client.render.HiltRenderer.part(hiltId, part)
                            .render(matrices, vc, 0xF000F0, net.minecraft.client.render.OverlayTexture.DEFAULT_UV, 0x20999999);
                    matrices.pop();
                }
            }
            if (handler.input.getStack(5).isEmpty()) {
                int gx = x + LightsaberForgeScreenHandler.SLOTS[5][0];
                int gy = y + LightsaberForgeScreenHandler.SLOTS[5][1];
                float[] rgb = com.drag0nge0de.lightsabers.client.render.HiltRenderer.rgb(
                        com.drag0nge0de.lightsabers.client.render.model.HiltModels.stats(hiltId).defaultColor());
                matrices.push();
                matrices.translate(gx + 8, gy + 8, 150);
                matrices.scale(16, -16, 16);
                matrices.scale(1.6F, 1.6F, 1.6F);

                com.drag0nge0de.lightsabers.client.render.BlockEntityRenderers.renderCrystal(
                        matrices, vertexConsumers, rgb, 0.25F, 180, true);
                matrices.pop();
            }

            for (int slot : new int[]{6, 7}) {
                if (handler.input.getStack(slot).isEmpty()) {
                    context.drawTexture(OUTLINE, x + LightsaberForgeScreenHandler.SLOTS[slot][0],
                            y + LightsaberForgeScreenHandler.SLOTS[slot][1],
                            0, 0, 16, 16, 16, 16);
                }
            }
            vertexConsumers.draw();
        }
        com.drag0nge0de.lightsabers.client.ALPerf.section("after-preview");
    }

    private Hilt ghostHilt() {
        Hilt hilt = null;
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = handler.input.getStack(slot);
            if (!stack.isEmpty()) {
                Hilt partHilt = PartItem.getHilt(stack);
                if (hilt == null || hilt == partHilt) {
                    hilt = partHilt;
                } else {
                    return null;
                }
            }
        }
        return hilt;
    }
}
