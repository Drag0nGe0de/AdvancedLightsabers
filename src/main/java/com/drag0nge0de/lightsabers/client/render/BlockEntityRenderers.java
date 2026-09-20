package com.drag0nge0de.lightsabers.client.render;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.block.DisassemblyStationBlock;
import com.drag0nge0de.lightsabers.block.LightsaberForgeBlock;
import com.drag0nge0de.lightsabers.block.CrystalOreBlock;
import com.drag0nge0de.lightsabers.block.SithSarcophagusBlock;
import com.drag0nge0de.lightsabers.block.SithStoneCoffinBlock;
import com.drag0nge0de.lightsabers.block.blockentity.CrystalOreBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.DisassemblyStationBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.LightsaberForgeBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.SithSarcophagusBlockEntity;
import com.drag0nge0de.lightsabers.block.blockentity.SithStoneCoffinBlockEntity;
import com.drag0nge0de.lightsabers.client.render.model.TileModels;
import com.drag0nge0de.lightsabers.client.render.HiltRenderer;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.Random;

public final class BlockEntityRenderers {

    private static final Identifier FORGE_TEXTURE = AL.id("textures/models/lightsaber_forge_light.png");

    private static final Identifier FORGE_DARK_TEXTURE = AL.id("textures/models/lightsaber_forge_dark.png");
    private static final Identifier STATION_TEXTURE = AL.id("textures/models/disassembly_station.png");
    private static final Identifier COFFIN_TEXTURE = AL.id("textures/models/sith_coffin.png");
    private static final Identifier STONE_COFFIN_TEXTURE = AL.id("textures/models/sith_stone_coffin.png");
    private static final Identifier STAND_TEXTURE = AL.id("textures/models/lightsaber_stand.png");

    private BlockEntityRenderers() {
    }

    public static class Forge implements BlockEntityRenderer<LightsaberForgeBlockEntity> {

        public Forge(BlockEntityRendererFactory.Context context) {
        }

        @Override
        public void render(LightsaberForgeBlockEntity forge, float tickDelta, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            Direction facing = forge.getCachedState().contains(LightsaberForgeBlock.FACING)
                    ? forge.getCachedState().get(LightsaberForgeBlock.FACING)
                    : Direction.NORTH;
            matrices.push();

            matrices.translate(0.5F, 1.5F, 0.5F);
            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(facing.asRotation() + 180));

            renderModel(matrices, vcp, light, false, false,
                  forge.getCachedState().isOf(com.drag0nge0de.lightsabers.registry.ALBlocks.LIGHTSABER_FORGE_DARK)
                        ? FORGE_DARK_TEXTURE : FORGE_TEXTURE);
            matrices.pop();
        }
    }

    public static void renderModel(MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        renderModel(matrices, vcp, light, false, false);
    }

    public static void renderModel(MatrixStack matrices, VertexConsumerProvider vcp, int light,
            boolean centered, boolean noCull) {
        renderModel(matrices, vcp, light, centered, noCull, FORGE_TEXTURE);
    }

    public static void renderModel(MatrixStack matrices, VertexConsumerProvider vcp, int light,
            boolean centered, boolean noCull, Identifier texture) {
        VertexConsumer vc = vcp.getBuffer(noCull
                ? RenderLayer.getEntityCutoutNoCull(texture)
                : RenderLayer.getEntitySolid(texture));
        ModelPart model = TileModels.cached("LightsaberForge");
        TileModels.Part part = TileModels.get("LightsaberForge");
        matrices.push();
        if (centered) {

            matrices.translate(-0.5F, -0.8031F, 0.0F);
        }

        for (int i = 0; i < part.ops().length; i++) {
            TileModels.opPart(model, part, i).render(matrices, vc, light, OverlayTexture.DEFAULT_UV);
        }
        matrices.pop();
    }

    public static class DisassemblyStation implements BlockEntityRenderer<DisassemblyStationBlockEntity> {

        public DisassemblyStation(BlockEntityRendererFactory.Context context) {
        }

        @Override
        public void render(DisassemblyStationBlockEntity station, float tickDelta, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            Direction facing = station.getCachedState().contains(DisassemblyStationBlock.FACING)
                    ? station.getCachedState().get(DisassemblyStationBlock.FACING)
                    : Direction.NORTH;
            matrices.push();
            matrices.translate(0.5F, 1.5F, 0.5F);
            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(facing.asRotation() + 180));
            renderStationModel(matrices, vcp, light, false, false);
            matrices.pop();
        }
    }

    public static void renderStationModel(MatrixStack matrices, VertexConsumerProvider vcp, int light,
            boolean centered, boolean noCull) {
        VertexConsumer vc = vcp.getBuffer(noCull
                ? RenderLayer.getEntityCutoutNoCull(STATION_TEXTURE)
                : RenderLayer.getEntitySolid(STATION_TEXTURE));
        ModelPart model = TileModels.cached("DisassemblyStation");
        TileModels.Part part = TileModels.get("DisassemblyStation");
        matrices.push();
        if (centered) {

            matrices.translate(0.0965F, -0.5937F, 0.0828F);
        }

        for (int i = 0; i < part.ops().length; i++) {
            TileModels.opPart(model, part, i).render(matrices, vc, light, OverlayTexture.DEFAULT_UV);
        }
        matrices.pop();
    }

    public static class SithCoffin implements BlockEntityRenderer<SithSarcophagusBlockEntity> {

        public SithCoffin(BlockEntityRendererFactory.Context context) {
        }

        @Override
        public void render(SithSarcophagusBlockEntity sarcophagus, float tickDelta, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            Direction facing = sarcophagus.getCachedState().contains(SithSarcophagusBlock.FACING)
                    ? sarcophagus.getCachedState().get(SithSarcophagusBlock.FACING)
                    : Direction.NORTH;
            matrices.push();
            matrices.translate(0.5F, 1.5F, 0.5F);
            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(facing.asRotation() + 180));

            renderSarcophagusModel(matrices, vcp, light,
                    MathHelper.clamp(sarcophagus.getLidOpenTimer(tickDelta), 0.0F, 1.0F));
            matrices.pop();
        }
    }

    public static void renderSarcophagusModel(MatrixStack matrices, VertexConsumerProvider vcp,
            int light, float lidFraction) {
        ModelPart model = TileModels.cached("SithCoffin");
        TileModels.Part part = TileModels.get("SithCoffin");
        float f = MathHelper.clamp(lidFraction, 0.0F, 1.0F);
        float f1 = f > 0.5F ? (1 - f) * 2 : 1;
        float f2 = 1 - f1;
        ModelPart lid = model.getChild("n8");
        lid.pivotX = -9.0F + 16 * f;
        lid.pivotY = 11.5F - 3 * f2;
        lid.pivotZ = 8.0F;
        lid.roll = f2;

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntitySolid(COFFIN_TEXTURE));
        for (int i = 0; i < part.ops().length; i++) {
            TileModels.opPart(model, part, i).render(matrices, vc, light, OverlayTexture.DEFAULT_UV);
        }
    }

    public static class SithStoneCoffin implements BlockEntityRenderer<SithStoneCoffinBlockEntity> {

        public SithStoneCoffin(BlockEntityRendererFactory.Context context) {
        }

        @Override
        public void render(SithStoneCoffinBlockEntity coffin, float tickDelta, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            Direction facing = coffin.getCachedState().contains(SithStoneCoffinBlock.FACING)
                    ? coffin.getCachedState().get(SithStoneCoffinBlock.FACING)
                    : Direction.NORTH;
            matrices.push();
            matrices.translate(0.5F, 1.5F, 0.5F);
            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(facing.asRotation() + 180));

            if (coffin.getCachedState().contains(SithStoneCoffinBlock.TOP)
                  && coffin.getCachedState().get(SithStoneCoffinBlock.TOP)) {
               matrices.pop();
               return;
            }

            renderStoneCoffinModel(matrices, vcp, light, !coffin.isOpen());
            matrices.pop();
        }
    }

    public static void renderStoneCoffinModel(MatrixStack matrices, VertexConsumerProvider vcp,
            int light, boolean uprightVisible) {
        ModelPart model = TileModels.cached("SithStoneCoffin");
        TileModels.Part part = TileModels.get("SithStoneCoffin");

        ModelPart upright = model.getChild("n0").getChild("n1");
        upright.visible = uprightVisible;
        upright.hidden = !uprightVisible;

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntitySolid(STONE_COFFIN_TEXTURE));
        for (int i = 0; i < part.ops().length; i++) {
            TileModels.opPart(model, part, i).render(matrices, vc, light, OverlayTexture.DEFAULT_UV);
        }
        upright.visible = true;
        upright.hidden = false;
    }

    public static void renderStandParts(MatrixStack matrices, VertexConsumerProvider vcp,
            int light, net.minecraft.item.ItemStack displayStack) {
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntitySolid(STAND_TEXTURE));
        ModelPart model = TileModels.cached("LightsaberStand");
        TileModels.Part part = TileModels.get("LightsaberStand");
        for (int i = 0; i < part.ops().length; i++) {
            TileModels.renderOpPart(matrices, model, part, i, vc, light, OverlayTexture.DEFAULT_UV);
        }

        if (displayStack != null && !displayStack.isEmpty()
                && displayStack.getItem() instanceof com.drag0nge0de.lightsabers.item.LightsaberItem) {
            com.drag0nge0de.lightsabers.component.LightsaberComponent c =
                    com.drag0nge0de.lightsabers.item.LightsaberItem.getComponent(displayStack);
            matrices.push();
            matrices.translate(0, 1.36F, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
            matrices.scale(0.15F, 0.15F, 0.15F);
            HiltRenderer.renderHilt(matrices, vcp, c.hilt(), Math.max(light, 0xF000F0),
                    OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
    }

    public static float[] tileBounds(String key) {
        float[] out = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        MatrixStack ms = new MatrixStack();
        TileModels.cached(key).forEachCuboid(ms, (entry, path, idx, cuboid) ->
                accumulate(entry, cuboid, out));
        return out;
    }

    private static void accumulate(MatrixStack.Entry entry,
            net.minecraft.client.model.ModelPart.Cuboid cuboid, float[] out) {
        Matrix4f m = entry.getPositionMatrix();
        for (int cx = 0; cx < 2; cx++) {
            for (int cy = 0; cy < 2; cy++) {
                for (int cz = 0; cz < 2; cz++) {
                    float x = (cx == 0 ? cuboid.minX : cuboid.maxX) / 16.0F;
                    float y = (cy == 0 ? cuboid.minY : cuboid.maxY) / 16.0F;
                    float z = (cz == 0 ? cuboid.minZ : cuboid.maxZ) / 16.0F;
                    org.joml.Vector3f v = m.transformPosition(x, y, z, new org.joml.Vector3f());
                    out[0] = Math.min(out[0], v.x);
                    out[1] = Math.min(out[1], v.y);
                    out[2] = Math.min(out[2], v.z);
                    out[3] = Math.max(out[3], v.x);
                    out[4] = Math.max(out[4], v.y);
                    out[5] = Math.max(out[5], v.z);
                }
            }
        }
    }

    public static class CrystalOre implements BlockEntityRenderer<CrystalOreBlockEntity> {

        public CrystalOre(BlockEntityRendererFactory.Context context) {
        }

        @Override
        public void render(CrystalOreBlockEntity ore, float tickDelta, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {

            matrices.translate(0.5F, 1.5F, 0.5F);
            matrices.scale(1, -1, -1);
            int attachment = ore.getCachedState().contains(CrystalOreBlock.ATTACHMENT)
                    ? ore.getCachedState().get(CrystalOreBlock.ATTACHMENT) : 0;
            if (attachment == 6) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                matrices.translate(0, 2, 0);
            } else if (attachment != 5) {
                float yaw = switch (attachment) {
                    case 2 -> 180.0F;
                    case 3 -> 90.0F;
                    case 4 -> 270.0F;
                    default -> 0.0F;
                };
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
                matrices.translate(1, 1, 0);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
            }
            Random rand = new Random(ore.getPos().getX() + ore.getPos().getY() + ore.getPos().getZ());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rand.nextInt(360)));
            matrices.translate(0, rand.nextInt(10) / 40.0F, 0);
            int rgb = ore.getColor() != -1 ? ore.getColor() : ItemRenderers.Crystal.DEFAULT_INT;
            renderCrystalSpikes(matrices, vcp, HiltRenderer.rgb(rgb), 0.6F);
        }
    }

    public static void renderCrystal(MatrixStack matrices, VertexConsumerProvider vcp,
            float[] rgb, float alpha, float rotYDegrees) {
        renderCrystal(matrices, vcp, rgb, alpha, rotYDegrees, false);
    }

    public static void renderCrystal(MatrixStack matrices, VertexConsumerProvider vcp,
            float[] rgb, float alpha, float rotYDegrees, boolean centered) {
        matrices.push();
        if (centered) {
            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

            matrices.translate(0.0163F, -1.3389F, 0.0191F);
        } else {
            matrices.translate(0.5F, 1.5F, 0.5F);
            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotYDegrees));
        }
        renderCrystalSpikes(matrices, vcp, rgb, alpha);
        matrices.pop();
    }

    private static void renderCrystalSpikes(MatrixStack matrices, VertexConsumerProvider vcp,
            float[] rgb, float alpha) {

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(HiltRenderer.WHITE));

        float[][][] spikes = {
                {{-0.8F, 24.3F, -0.9F}, {1, 3, 1}, {0.349F, 0.3316F, -0.0698F}},
                {{0.5F, 24.0F, -1.5F}, {2, 3, 2}, {0.2094F, -0.8203F, 0.1222F}},
                {{0.0F, 24.3F, 1.0F}, {1, 4, 2}, {-0.2443F, 1.1694F, 0.3491F}},
                {{-1.0F, 24.3F, 0.0F}, {2, 4, 2}, {-0.1745F, 0.0F, -0.1745F}},
                {{0.0F, 24.3F, 0.5F}, {1, 3, 1}, {-0.5236F, -0.4014F, 0.1222F}},
                {{0.0F, 24.3F, 0.0F}, {2, 6, 2}, {0.0873F, 0.0F, 0.1047F}},
        };
        for (float[][] spike : spikes) {
            matrices.push();
            matrices.translate(spike[0][0] / 16.0F, spike[0][1] / 16.0F, spike[0][2] / 16.0F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(spike[2][0]));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(spike[2][1]));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(spike[2][2]));

            matrices.scale(0.0625F, 0.0625F, 0.0625F);
            MatrixStack.Entry entry = matrices.peek();
            org.joml.Matrix4f m2 = entry.getPositionMatrix();
            float x = spike[1][0] / 2, y = spike[1][1], z = spike[1][2] / 2;
            face(m2, entry, vc, rgb, alpha, x, -y, z, -x, -y, z, -x, 0, z, x, 0, z, 0, 1, 0);
            face(m2, entry, vc, rgb, alpha, x, -y, -z, x, 0, -z, -x, 0, -z, -x, -y, -z, 0, -1, 0);
            face(m2, entry, vc, rgb, alpha, x, -y, z, x, 0, z, x, 0, -z, x, -y, -z, 1, 0, 0);
            face(m2, entry, vc, rgb, alpha, -x, -y, -z, -x, 0, -z, -x, 0, z, -x, -y, z, -1, 0, 0);

            face(m2, entry, vc, rgb, alpha, -x, 0, -z, x, 0, -z, x, 0, z, -x, 0, z, 0, 1, 0);
            face(m2, entry, vc, rgb, alpha, -x, -y, -z, x, -y, -z, x, -y, z, -x, -y, z, 0, -1, 0);
            matrices.pop();
        }
    }

    private static void face(org.joml.Matrix4f mat, MatrixStack.Entry entry, VertexConsumer vc,
            float[] rgb, float a,
            float ax, float ay, float az, float bx, float by, float bz,
            float cx, float cy, float cz, float dx, float dy, float dz,
            float nx, float ny, float nz) {
        int light = 0xF000F0;
        vc.vertex(mat, ax, ay, az).color(rgb[0], rgb[1], rgb[2], a)
                .texture(0.5F, 0.5F).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
                .light(light).normal(entry, nx, ny, nz);
        vc.vertex(mat, bx, by, bz).color(rgb[0], rgb[1], rgb[2], a)
                .texture(0.5F, 0.5F).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
                .light(light).normal(entry, nx, ny, nz);
        vc.vertex(mat, cx, cy, cz).color(rgb[0], rgb[1], rgb[2], a)
                .texture(0.5F, 0.5F).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
                .light(light).normal(entry, nx, ny, nz);
        vc.vertex(mat, dx, dy, dz).color(rgb[0], rgb[1], rgb[2], a)
                .texture(0.5F, 0.5F).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
                .light(light).normal(entry, nx, ny, nz);
    }
}
