package com.drag0nge0de.lightsabers.client.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import org.joml.Vector3f;

public final class HolocronGeometry {

    private HolocronGeometry() {
    }

    public static void draw(MatrixStack matrices, VertexConsumerProvider vcp, boolean sith,
            float open, int openTicks) {

        VertexConsumer vc = vcp.getBuffer(
                RenderLayer.getText(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));

        float size = 0.5F;
        float f1 = MathHelper.sin(openTicks / 10.0F) / 20.0F;

        matrices.push();

        matrices.translate(0, size / 2.0F + (size / 2.0F + f1) * open, 0);

        if (sith) {
            drawSith(matrices, vc, size);
        } else {
            drawJedi(matrices, vc, size,
                    0.5F * (0.5775F + open * (0.3F + f1)),
                    180.0F * open, openTicks);
        }

        matrices.pop();
    }

    private static void drawSith(MatrixStack matrices, VertexConsumer vc, float size) {
        Sprite bottom = sprite("sith_holocron_bottom");
        Sprite side = sprite("sith_holocron_side");

        quadDouble(matrices, vc, bottom,
                size / 2, -size / 2, -size / 2,
                size / 2, -size / 2, size / 2,
                -size / 2, -size / 2, size / 2,
                -size / 2, -size / 2, -size / 2);

        float minV = side.getMinV(), maxV = side.getMaxV();
        float leftU = side.getMinU();
        float midU = leftU + (side.getMaxU() - leftU) * 0.5F;
        float endU = side.getMaxU();

        for (int i = 0; i < 4; i++) {

            triDouble(matrices, vc, leftU, maxV, midU, maxV, leftU, minV,
                    size / 2, -size / 2, -size / 2,
                    0, -size / 2, -size / 2,
                    0, size / 2, 0);
            triDouble(matrices, vc, midU, maxV, endU, maxV, endU, minV,
                    0, -size / 2, -size / 2,
                    -size / 2, -size / 2, -size / 2,
                    0, size / 2, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        }
    }

    private static void drawJedi(MatrixStack matrices, VertexConsumer vc, float size,
            float offset, float rot, int openTicks) {
        Sprite side = sprite("jedi_holocron_side");
        Sprite corner = sprite("jedi_holocron_corner");
        Sprite cornerBottom = sprite("jedi_holocron_corner_bottom");
        Sprite cornerSide = sprite("jedi_holocron_corner_side");

        float minU = side.getMinU(), maxU = side.getMaxU();
        float minV = side.getMinV(), maxV = side.getMaxV();

        for (int i = 0; i < 6; i++) {
            matrices.push();
            if (i < 4) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * 90));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((i - 4) * 180 + 90));
            }
            quad(matrices, vc, maxU, maxV, maxU, minV, minU, minV, minU, maxV,
                    -size / 2, 0, size / 2,
                    0, -size / 2, size / 2,
                    size / 2, 0, size / 2,
                    0, size / 2, size / 2);
            matrices.pop();
        }

        float width = 0.707F * size;
        float width1 = MathHelper.sqrt(width * width - (width / 2) * (width / 2));
        float height = size * 0.2875F;

        float cMinU = corner.getMinU();
        float cU8 = cMinU + (corner.getMaxU() - cMinU) * 0.5F;
        float cU16 = corner.getMaxU();
        float cMinV = corner.getMinV(), cMaxV = corner.getMaxV();

        float bMinU = cornerBottom.getMinU();
        float bU8 = bMinU + (cornerBottom.getMaxU() - bMinU) * 0.5F;
        float bU16 = cornerBottom.getMaxU();
        float bMinV = cornerBottom.getMinV(), bMaxV = cornerBottom.getMaxV();

        float dMinU = cornerSide.getMinU();
        float dU8 = dMinU + (cornerSide.getMaxU() - dMinU) * 0.5F;
        float dU16 = cornerSide.getMaxU();
        float dMinV = cornerSide.getMinV(), dMaxV = cornerSide.getMaxV();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {

                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j * 90));
                if (i == 1) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                }
                tri(matrices, vc, cU8, cMinV, cMinU, cMinV, cMinU, cMaxV,
                        -size / 4, size / 2, size / 4,
                        -size / 2, size / 2, 0,
                        -size / 2, 0, size / 2);
                tri(matrices, vc, cU16, cMaxV, cU8, cMinV, cU8, cMinV,
                        -size / 2, 0, size / 2,
                        0, size / 2, size / 2,
                        -size / 4, size / 2, size / 4);
                matrices.pop();

                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 + j * 90));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rot));
                if (i == 1) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                }
                matrices.translate(0, 0, offset);

                tri(matrices, vc, bMinU, bMaxV, bMinU, bMinV, bU8, bMinV,
                        0, (width1 + width1) / 3, 0,
                        width / 2, -width1 / 3, 0,
                        0, -width1 / 3, 0);
                tri(matrices, vc, bU8, bMinV, bU16, bMinV, bU16, bMaxV,
                        0, -width1 / 3, 0,
                        -width / 2, -width1 / 3, 0,
                        0, (width1 + width1) / 3, 0);

                for (int k = 0; k < 3; k++) {
                    tri(matrices, vc, dU8, dMaxV, dU16, dMinV, dU16, dMaxV,
                            0, -width1 / 3, 0,
                            0, 0, height,
                            -width / 2, -width1 / 3, 0);
                    tri(matrices, vc, dMinU, dMaxV, dU8, dMinV, dMinU, dMinV,
                            0, -width1 / 3, 0,
                            width / 2, -width1 / 3, 0,
                            0, 0, height);
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(120));
                }
                matrices.pop();
            }
        }
    }

    private static Sprite sprite(String name) {
        return MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                .apply(Identifier.of("lightsabers", "block/" + name));
    }

    private static void quad(MatrixStack matrices, VertexConsumer vc,
            float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4,
            float ax, float ay, float az, float bx, float by, float bz,
            float cx, float cy, float cz, float dx, float dy, float dz) {

        for (int w = 0; w < 2; w++) {
            if (w == 0) {
                vert(matrices, vc, ax, ay, az, u1, v1);
                vert(matrices, vc, bx, by, bz, u2, v2);
                vert(matrices, vc, cx, cy, cz, u3, v3);
                vert(matrices, vc, dx, dy, dz, u4, v4);
            } else {
                vert(matrices, vc, dx, dy, dz, u4, v4);
                vert(matrices, vc, cx, cy, cz, u3, v3);
                vert(matrices, vc, bx, by, bz, u2, v2);
                vert(matrices, vc, ax, ay, az, u1, v1);
            }
        }
    }

    private static void quadDouble(MatrixStack matrices, VertexConsumer vc, Sprite sprite,
            float ax, float ay, float az, float bx, float by, float bz,
            float cx, float cy, float cz, float dx, float dy, float dz) {
        float minU = sprite.getMinU(), maxU = sprite.getMaxU();
        float minV = sprite.getMinV(), maxV = sprite.getMaxV();
        for (int w = 0; w < 2; w++) {
            if (w == 0) {
                vert(matrices, vc, ax, ay, az, maxU, minV);
                vert(matrices, vc, bx, by, bz, maxU, maxV);
                vert(matrices, vc, cx, cy, cz, minU, maxV);
                vert(matrices, vc, dx, dy, dz, minU, minV);
            } else {
                vert(matrices, vc, dx, dy, dz, minU, minV);
                vert(matrices, vc, cx, cy, cz, minU, maxV);
                vert(matrices, vc, bx, by, bz, maxU, maxV);
                vert(matrices, vc, ax, ay, az, maxU, minV);
            }
        }
    }

    private static void triDouble(MatrixStack matrices, VertexConsumer vc,
            float u1, float v1, float u2, float v2, float u3, float v3,
            float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz) {

        verts(matrices, vc, ax, ay, az, u1, v1, bx, by, bz, u2, v2, cx, cy, cz, u3, v3);
        verts(matrices, vc, cx, cy, cz, u3, v3, bx, by, bz, u2, v2, ax, ay, az, u1, v1);
    }

    private static void tri(MatrixStack matrices, VertexConsumer vc,
            float u1, float v1, float u2, float v2, float u3, float v3,
            float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz) {

        verts(matrices, vc, ax, ay, az, u1, v1, bx, by, bz, u2, v2, cx, cy, cz, u3, v3);
        verts(matrices, vc, cx, cy, cz, u3, v3, bx, by, bz, u2, v2, ax, ay, az, u1, v1);
    }

    private static void verts(MatrixStack matrices, VertexConsumer vc,
            float ax, float ay, float az, float u1, float v1,
            float bx, float by, float bz, float u2, float v2,
            float cx, float cy, float cz, float u3, float v3) {
        vert(matrices, vc, ax, ay, az, u1, v1);
        vert(matrices, vc, bx, by, bz, u2, v2);
        vert(matrices, vc, cx, cy, cz, u3, v3);
        vert(matrices, vc, cx, cy, cz, u3, v3);
    }

    private static void vert(MatrixStack matrices, VertexConsumer vc, float x, float y, float z, float u, float v) {

        MatrixStack.Entry entry = matrices.peek();
        vc.vertex(entry.getPositionMatrix(), x, y, z)
                .texture(u, v)
                .color(255, 255, 255, 255)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }
}
