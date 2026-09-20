package com.drag0nge0de.lightsabers.client.render;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.client.render.model.HiltModels;
import com.drag0nge0de.lightsabers.hilt.HiltStats;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class HiltRenderer {

    public static final Identifier WHITE = AL.id("textures/misc/white.png");
    public static final int BLADE_LENGTH = 38;
    public static final int CROSSGUARD_LENGTH = 4;

    private static final Map<String, ModelPart> PARTS = new HashMap<>();

    private static final Map<String, Identifier> TEXTURES = new HashMap<>();

    private static final Map<String, float[]> BOUNDS = new HashMap<>();

    private static final Map<String, Float> PROJ = new HashMap<>();

    public static volatile boolean noCullRender = false;

    private HiltRenderer() {
    }

    public static float[] partBounds(String hilt, String part) {
        String key = com.drag0nge0de.lightsabers.hilt.HiltStatsTable.legacyModelKey(
                com.drag0nge0de.lightsabers.hilt.Hilt.byName(hilt).getId()) + "/" + part;
        return BOUNDS.computeIfAbsent(key, k -> {
            float[] out = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                    -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            MatrixStack ms = new MatrixStack();
            part(key.split("/")[0], part).forEachCuboid(ms, (entry, path, idx, cuboid) ->
                    accumulateCuboid(entry, cuboid, out));
            return out;
        });
    }

    private static void accumulateCuboid(MatrixStack.Entry entry,
            net.minecraft.client.model.ModelPart.Cuboid cuboid, float[] out) {
        Matrix4f m = entry.getPositionMatrix();
        for (int cx = 0; cx < 2; cx++) {
            for (int cy = 0; cy < 2; cy++) {
                for (int cz = 0; cz < 2; cz++) {
                    float x = (cx == 0 ? cuboid.minX : cuboid.maxX) / 16.0F;
                    float y = (cy == 0 ? cuboid.minY : cuboid.maxY) / 16.0F;
                    float z = (cz == 0 ? cuboid.minZ : cuboid.maxZ) / 16.0F;
                    Vector3f v = m.transformPosition(x, y, z, new Vector3f());
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

    public static void applyPartGuiRotation(MatrixStack matrices) {
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-110));
    }

    public static void applyPartGuiPose(MatrixStack matrices) {
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45));
        applyPartGuiRotation(matrices);
    }

    public static void applyPartGuiOrbit(MatrixStack matrices) {
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-38));
    }

    public static float partProjectedExtent(String hilt, String part) {
        String key = com.drag0nge0de.lightsabers.hilt.HiltStatsTable.legacyModelKey(
                com.drag0nge0de.lightsabers.hilt.Hilt.byName(hilt).getId()) + "/" + part;
        return PROJ.computeIfAbsent(key, k -> {
            float[] bb = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                    -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            MatrixStack ms = new MatrixStack();
            applyPartGuiOrbit(ms);
            applyPartGuiPose(ms);
            part(hilt, part).forEachCuboid(ms, (entry, path, idx, cuboid) ->
                    accumulateCuboid(entry, cuboid, bb));
            return Math.max(bb[3] - bb[0], bb[4] - bb[1]);
        });
    }

    public static void renderSinglePart(MatrixStack matrices, VertexConsumerProvider vcp,
            String hilt, String part, int light, int overlay, RenderLayer layer, int tint) {
        HiltStats s = HiltModels.stats(hilt);
        float total = s.emitterH() + s.switchH() + s.bodyH() + s.pommelH();
        matrices.push();

        MatrixStack ms = new MatrixStack();
        applyPartPlacement(ms, s, part, total);
        float[] bb = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        part(hilt, part).forEachCuboid(ms, (entry, path, idx, cuboid) ->
                accumulateCuboid(entry, cuboid, bb));
        matrices.translate(-(bb[0] + bb[3]) / 2.0F, -(bb[1] + bb[4]) / 2.0F,
                -(bb[2] + bb[5]) / 2.0F);
        applyPartPlacement(matrices, s, part, total);

        VertexConsumer vc = vcp.getBuffer(layer);
        part(hilt, part).render(matrices, vc, light, overlay, tint);
        matrices.pop();
    }

    private static void applyPartPlacement(MatrixStack matrices, HiltStats s,
            String part, float total) {
        matrices.translate(0, -(s.bodyH() + s.pommelH() - total / 2) / 16.0F, 0);

        if ("emitter".equals(part)) {
            matrices.translate(0, -s.switchH() / 16.0F, 0);
        } else if ("pommel".equals(part)) {
            float[] gl = s.bodyGl();
            if (gl.length > 0) {
                for (int i = 0; i < gl.length; ++i) {
                    float f = gl[i];
                    if ((i & 1) == 0) {
                        matrices.translate(0, f / 16.0F, 0);
                    } else {
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f));
                    }
                }
            } else {
                matrices.translate(0, s.bodyH() / 16.0F, 0);
            }
        }
    }

    public static ModelPart part(String hilt, String part) {

        String id = com.drag0nge0de.lightsabers.hilt.Hilt.byName(hilt).getId();
        String key = ("graflex".equals(id) ? "grafx" : id) + "/" + part;
        return PARTS.computeIfAbsent(key, k -> {
            HiltModels.Part p = HiltModels.get(k);
            if (p == null) {

                System.out.println("[AL] Missing hilt model data: " + k + " (falling back)");
                p = HiltModels.get("grafx/" + part);
                if (p == null) {
                    p = HiltModels.get("graflex/" + part);
                }
                if (p == null) {
                    throw new IllegalStateException("Missing hilt model data: " + k);
                }
            }
            return HiltModels.buildModel(p);
        });
    }

    public static Identifier texture(String hilt, String part) {

        hilt = com.drag0nge0de.lightsabers.hilt.HiltStatsTable.legacyModelKey(
                com.drag0nge0de.lightsabers.hilt.Hilt.byName(hilt).getId());

        String file = hilt.equals("reborn") ? "reborn" : part + "_" + hilt;
        return TEXTURES.computeIfAbsent(file, f -> AL.id("textures/models/lightsaber/" + f + ".png"));
    }

    private static void renderPart(MatrixStack matrices, VertexConsumerProvider vcp,
            String hilt, String part, int light, int overlay) {
        Identifier tex = texture(hilt, part);
        VertexConsumer vc = vcp.getBuffer(noCullRender
                ? RenderLayer.getEntityCutoutNoCull(tex)
                : RenderLayer.getEntitySolid(tex));

        part(hilt, part).render(matrices, vc, light, overlay);
    }

    public static void renderHilt(MatrixStack matrices, VertexConsumerProvider vcp,
            String hilt, int light, int overlay) {
        renderHilt(matrices, vcp, new String[]{hilt, hilt, hilt, hilt}, light, overlay);
    }

    public static void renderHilt(MatrixStack matrices, VertexConsumerProvider vcp,
            String[] hilts, int light, int overlay) {
        HiltStats emitterStats = HiltModels.stats(hilts[0]);
        HiltStats switchStats = HiltModels.stats(hilts[1]);
        HiltStats bodyStats = HiltModels.stats(hilts[2]);
        HiltStats pommelStats = HiltModels.stats(hilts[3]);
        float total = emitterStats.emitterH() + switchStats.switchH() + bodyStats.bodyH() + pommelStats.pommelH();
        matrices.push();
        matrices.translate(0, -(bodyStats.bodyH() + pommelStats.pommelH() - total / 2) / 16.0F, 0);

        renderPart(matrices, vcp, hilts[1], "switch_section", light, overlay);
        renderPart(matrices, vcp, hilts[2], "body", light, overlay);

        matrices.push();
        matrices.translate(0, -switchStats.switchH() / 16.0F, 0);
        renderPart(matrices, vcp, hilts[0], "emitter", light, overlay);
        matrices.pop();

        matrices.push();
        float[] gl = bodyStats.bodyGl();
        if (gl.length > 0) {
            for (int i = 0; i < gl.length; ++i) {
                float f = gl[i];
                if ((i & 1) == 0) {
                    matrices.translate(0, f / 16.0F, 0);
                } else {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f));
                }
            }
        } else {
            matrices.translate(0, bodyStats.bodyH() / 16.0F, 0);
        }
        renderPart(matrices, vcp, hilts[3], "pommel", light, overlay);
        matrices.pop();

        matrices.pop();
    }

    public static void renderSaber(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c, int light, int overlay, boolean inWorld) {
        renderSaber(matrices, vcp, c, light, overlay, inWorld, false);
    }

    public static void renderSaber(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c, int light, int overlay, boolean inWorld, boolean jeb) {
        String[] hilts = c.hilts();
        HiltStats emitterStats = HiltModels.stats(hilts[0]);
        HiltStats switchStats = HiltModels.stats(hilts[1]);
        HiltStats bodyStats = HiltModels.stats(hilts[2]);
        HiltStats pommelStats = HiltModels.stats(hilts[3]);
        float total = emitterStats.emitterH() + switchStats.switchH() + bodyStats.bodyH() + pommelStats.pommelH();
        matrices.push();
        renderHilt(matrices, vcp, hilts, light, overlay);
        if (c.active()) {

            matrices.translate(0, -(bodyStats.bodyH() + pommelStats.pommelH() - total / 2.0F) / 16.0F, 0);
            matrices.push();
            matrices.scale(3, 3, 3);
            matrices.translate(0, -(switchStats.switchH() + emitterStats.emitterH()) * 0.0234375F, 0);
            renderBlade(matrices, vcp, c, inWorld, light, jeb);
            matrices.pop();
        }
        matrices.pop();
    }

    public static void renderDoubleSaber(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c, int light, int overlay, boolean inWorld) {
        renderDoubleSaber(matrices, vcp, c, light, overlay, inWorld, false);
    }

    public static void renderDoubleSaber(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c, int light, int overlay, boolean inWorld, boolean jeb) {
        String[] hilts = c.hilts();
        HiltStats emitterStats = HiltModels.stats(hilts[0]);
        HiltStats switchStats = HiltModels.stats(hilts[1]);
        HiltStats bodyStats = HiltModels.stats(hilts[2]);
        HiltStats pommelStats = HiltModels.stats(hilts[3]);
        float half = (emitterStats.emitterH() + switchStats.switchH() + bodyStats.bodyH() + pommelStats.pommelH()) / 32.0F;
        matrices.push();
        matrices.translate(0, -half, 0);
        renderSaber(matrices, vcp, c, light, overlay, inWorld, jeb);
        matrices.pop();
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(0, -half, 0);

        if (c.second().isPresent()) {
            LightsaberComponent.Blade blade = c.second().get();
            LightsaberComponent lower = new LightsaberComponent(c.active(), blade.emitterHilt(), blade.switchHilt(),
                blade.gripHilt(), blade.pommelHilt(), blade.color(), false, blade.focusing(), null, c.special());
            renderSaber(matrices, vcp, lower, light, overlay, inWorld, jeb);
        } else {
            renderSaber(matrices, vcp, c, light, overlay, inWorld, jeb);
        }

        matrices.pop();
    }

    public static void renderDoubleHilt(MatrixStack matrices, VertexConsumerProvider vcp,
            String hilt, int light, int overlay) {
        renderDoubleHilt(matrices, vcp, new String[]{hilt, hilt, hilt, hilt}, light, overlay);
    }

    public static void renderDoubleHilt(MatrixStack matrices, VertexConsumerProvider vcp,
            String[] hilts, int light, int overlay) {
        HiltStats emitterStats = HiltModels.stats(hilts[0]);
        HiltStats switchStats = HiltModels.stats(hilts[1]);
        HiltStats bodyStats = HiltModels.stats(hilts[2]);
        HiltStats pommelStats = HiltModels.stats(hilts[3]);
        float half = (emitterStats.emitterH() + switchStats.switchH() + bodyStats.bodyH() + pommelStats.pommelH()) / 32.0F;
        matrices.push();
        matrices.translate(0, -half, 0);
        renderHilt(matrices, vcp, hilts, light, overlay);
        matrices.pop();
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.translate(0, -half, 0);
        renderHilt(matrices, vcp, hilts, light, overlay);
        matrices.pop();
    }

    public static void renderBlade(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c, boolean inWorld, int light) {
        renderBlade(matrices, vcp, c, inWorld, light, false);
    }

    public static void renderBlade(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c, boolean inWorld, int light, boolean jeb) {
        if (c.special() != null && !c.special().isEmpty()) {
            renderFishBlade(matrices, vcp, c, light);
            return;
        }

        float[] rgb = rgb(c.color());
        boolean compressed = c.hasFocusing(0);
        boolean cracked = c.hasFocusing(1);
        boolean inverting = c.hasFocusing(2);
        boolean prismatic = c.hasFocusing(4);
        boolean fineCut = c.hasFocusing(3);

        float[] core = inverting ? new float[]{0, 0, 0} : (prismatic ? rgb : new float[]{1, 1, 1});

        float[] glowRgb = inverting && prismatic ? new float[]{0, 0, 0} : rgb;
        float glowAlpha = compressed ? 0.07F : 0.1F;

        if (inverting && prismatic) {
            glowAlpha *= 1.5F;
        }

        matrices.translate(0, 0.095F, 0);

        VertexConsumer glowVc = vcp.getBuffer(ALRenderLayers.GLOW);

        float[] cg = HiltModels.stats(c.emitterHilt()).crossguard();
        if (cg != null) {
            for (int i = -1; i <= 1; i += 2) {
                matrices.push();
                matrices.translate(cg[0], cg[1], cg[2] * -i);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i * 90));
                if (i == 1) {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                }
                renderGlow(matrices, glowVc, glowRgb, glowAlpha, compressed ? 0.2F : 0.4F, compressed ? 7 : 10, CROSSGUARD_LENGTH, inWorld, false, fineCut);
                matrices.pop();
            }
        }

        renderGlow(matrices, glowVc, glowRgb, glowAlpha, compressed ? 0.4F : 0.6F, jeb ? 2 : (compressed ? 7 : 10), BLADE_LENGTH, inWorld, true, fineCut);

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getText(WHITE));

        if (cg != null) {
            for (int i = -1; i <= 1; i += 2) {
                matrices.push();
                matrices.translate(cg[0], cg[1], cg[2] * -i);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i * 90));
                if (i == 1) {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                }
                matrices.push();
                if (compressed) {
                    matrices.scale(0.6F, 1.0F, 0.6F);
                }
                if (fineCut) {

                    matrices.scale(1.0F, 1.2F, 1.0F);
                }
                renderBladeBox(matrices, vc, core, 1.0F, CROSSGUARD_LENGTH, fineCut, cracked, light);
                matrices.pop();
                matrices.pop();
            }
        }

        matrices.push();
        if (compressed) {
            matrices.scale(0.6F, 1.0F, 0.6F);
        }
        renderBladeBox(matrices, vc, core, 1.0F, BLADE_LENGTH, fineCut, cracked, light);
        matrices.pop();
    }

    private static void renderFishBlade(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c, int light) {
        net.minecraft.client.texture.Sprite sprite = net.minecraft.client.MinecraftClient.getInstance()
            .getSpriteAtlas(net.minecraft.client.texture.SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
            .apply(net.minecraft.util.Identifier.of(c.special()));
        VertexConsumer vc = vcp.getBuffer(net.minecraft.client.render.RenderLayer.getEntityCutoutNoCull(
            net.minecraft.client.texture.SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));

        float f = 0;

        if (net.minecraft.client.MinecraftClient.getInstance().player != null) {
            f = net.minecraft.client.MinecraftClient.getInstance().player.age;
        }

        matrices.push();
        matrices.scale(-2, -2 + MathHelper.cos(f) * 0.1F, 2 + MathHelper.sin(f) * 0.15F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (-(1 - 2 * MathHelper.cos(f / 2 + 1)))));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (1 - 2 * MathHelper.sin(f / 2 + 3))));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        matrices.translate(0.705F, 0.4F, 0.0625F / 2);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(135));

        float u0 = sprite.getMinU();
        float u1 = sprite.getMaxU();
        float v0 = sprite.getMinV();
        float v1 = sprite.getMaxV();
        float d = 0.0625F;

        spriteVertex(matrices, vc, 16, 16, d, u1, v1);
        spriteVertex(matrices, vc, 0, 16, d, u0, v1);
        spriteVertex(matrices, vc, 0, 0, d, u0, v0);
        spriteVertex(matrices, vc, 16, 0, d, u1, v0);

        spriteVertex(matrices, vc, 16, 16, 0, u1, v1);
        spriteVertex(matrices, vc, 0, 16, 0, u0, v1);
        spriteVertex(matrices, vc, 0, 0, 0, u0, v0);
        spriteVertex(matrices, vc, 16, 0, 0, u1, v0);

        matrices.pop();
    }

    private static void spriteVertex(MatrixStack matrices, VertexConsumer vc,
            float x, float y, float z, float u, float v) {
        Entry entry = matrices.peek();
        vc.vertex(entry.getPositionMatrix(), x, y, z)
            .color(1.0F, 1.0F, 1.0F, 1.0F)
            .texture(u, v)
            .overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
            .light(0xF000F0)
            .normal(entry, 0, 0, 1);
    }

    private static void renderGlow(MatrixStack matrices, VertexConsumer vc, float[] rgb,
            float alpha, float width, int smooth, int length, boolean inWorld, boolean main, boolean fineCut) {
        com.drag0nge0de.lightsabers.config.ALConfig config = com.drag0nge0de.lightsabers.config.ALConfig.get();
        float f = 1, f1 = 1, f2 = 1;
        if (fineCut) {
            f *= 0.55F;
            f1 *= 0.925F;
            f2 *= 1.1F;
        }
        f *= config.renderGlobalMultiplier * config.renderWidthMultiplier;
        f1 *= config.renderGlobalMultiplier * config.renderSmoothingMultiplier;
        f2 *= config.renderGlobalMultiplier * config.renderWidthMultiplier;
        alpha *= config.renderGlobalMultiplier * config.renderOpacityMultiplier;
        int layerCount = 5 * smooth;
        for (int i = 0; i < layerCount; ++i) {
            float a = alpha / smooth;
            float scale = 1 + i * (width / smooth);
            float f4 = (float) i / layerCount * 50;
            matrices.push();

            matrices.scale(scale * f, (1 - f4 * (fineCut ? 0.003F : (main ? 0.005F : 0.05F)) + (main ? 0.2F : 2.0F)) * f1, scale * f2);
            matrices.translate(0, -f4 / 400 + 0.06F, 0);
            matrices.push();
            matrices.scale(0.0625F, 0.0625F, 0.0625F);
            float bright = config.renderLightingMultiplier;
            glowBox(matrices, vc, Math.min(rgb[0] * bright, 1.0F), Math.min(rgb[1] * bright, 1.0F), Math.min(rgb[2] * bright, 1.0F), a, -0.5F, -length, -0.5F, 1, length, 1);
            matrices.pop();
            matrices.pop();
        }
    }

    private static void renderBladeBox(MatrixStack matrices, VertexConsumer vc, float[] rgb,
            float widthScale, int length, boolean fineCut, boolean cracked, int light) {
        if (cracked) {

            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            int ticks = client.player != null ? client.player.age : 0;
            java.util.Random rand = new java.util.Random(ticks % 100 * 1000);
            java.util.Random prev = new java.util.Random(((ticks - 1) % 100) * 1000);
            java.util.function.Supplier<Float> next = () -> (rand.nextFloat() + prev.nextFloat()) / 2F;

            for (int i = 0; i < 4; ++i) {
                matrices.push();

                if (i != 0) {
                    matrices.translate((next.get() - 0.5F) / 60, 0, (next.get() - 0.5F) / 60);
                }

                for (int j = 0; j < length; ++j) {
                    matrices.push();
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(next.get() * 360));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                    matrices.translate(0, 0.05F - (1 - next.get() * 0.2F) / 16, (1 + next.get() * length) / 16);
                    drawTip(matrices, vc, rgb, 0.04F, 0);
                    matrices.pop();
                }

                if (!fineCut) {
                    renderBladeCore(matrices, vc, rgb, length, true);
                }

                matrices.pop();
            }
        }

        if (fineCut) {
            renderFineCutBlade(matrices, vc, rgb, length);
        } else if (!cracked) {
            renderBladeCore(matrices, vc, rgb, length, true);
        }
    }

    private static void renderBladeCore(MatrixStack matrices, VertexConsumer vc, float[] rgb,
            int length, boolean withTip) {
        matrices.push();

        matrices.push();
        matrices.scale(0.0625F, 0.0625F, 0.0625F);
        box(matrices, vc, rgb[0], rgb[1], rgb[2], 1.0F, -0.5F, -length, -0.5F, 1, length, 1);
        matrices.pop();

        if (withTip) {
            matrices.translate(0, -0.0625F * (0.5F + length), 0.0625F / 2);
            drawTip(matrices, vc, rgb, 0.03125F, 0.125F);
        }
        matrices.pop();
    }

    private static void renderFineCutBlade(MatrixStack matrices, VertexConsumer vc, float[] rgb, int length) {

        float f = 0.0625F;
        float l = f * length * 0.7F;
        float edge = f * 1.5F;
        float edgeAngle = -f * 1.5F;
        float length1 = f * length * 0.3F;
        float edge1 = f / 2;
        float tip = f * 1.5F;
        float root = f * length;

        quad(matrices, vc, rgb, -f / 2, -l, f / 2, 0, -l, edge, 0, edgeAngle, edge, -f / 2, -f, f / 2);
        quad(matrices, vc, rgb, f / 2, -l, f / 2, 0, -l, edge, 0, edgeAngle, edge, f / 2, -f, f / 2);
        quad(matrices, vc, rgb, f / 2, -f, f / 2, 0, edgeAngle, edge, 0, edgeAngle, edge, -f / 2, -f, f / 2);
        quad(matrices, vc, rgb, -f / 2, -l, f / 2, -f / 2, -length1 - l, edge1, 0, -length1 - l, edge1, 0, -l, edge);
        quad(matrices, vc, rgb, f / 2, -l, f / 2, f / 2, -length1 - l, edge1, 0, -length1 - l, edge1, 0, -l, edge);
        quad(matrices, vc, rgb, -f / 2, -root, f / 2, 0, -tip - root, -f / 2, 0, -tip - root, -f / 2, -f / 2, -root, -f / 2);
        quad(matrices, vc, rgb, f / 2, -root, f / 2, 0, -tip - root, -f / 2, 0, -tip - root, -f / 2, f / 2, -root, -f / 2);
        quad(matrices, vc, rgb, -f / 2, -root, -f / 2, 0, -tip - root, -f / 2, 0, -tip - root, -f / 2, f / 2, -root, -f / 2);
        quad(matrices, vc, rgb, -f / 2, -root, f / 2, 0, -tip - root, -f / 2, 0, -tip - root, -f / 2, f / 2, -root, f / 2);

        renderBladeCore(matrices, vc, rgb, length, false);
    }

    private static void drawTip(MatrixStack matrices, VertexConsumer vc, float[] rgb, float size, float tip) {
        float f = 0.0625F;
        float f1 = f / 2;
        float y0 = size;
        float y1 = -size - tip;

        quad(matrices, vc, rgb, size, y0, 0, -size, y0, 0, -size + f1, y1, -f1, size - f1, y1, -f1);
        quad(matrices, vc, rgb, size, y0, -f, -size, y0, -f, -size + f1, y1, -f + f1, size - f1, y1, -f + f1);
        quad(matrices, vc, rgb, -f1, y0, size - f1, -f1, y0, -size - f1, 0, y1, -size, 0, y1, size - f);
        quad(matrices, vc, rgb, f1, y0, size - f1, f1, y0, -size - f1, 0, y1, -size, 0, y1, size - f);
    }

    private static void quad(MatrixStack matrices, VertexConsumer vc, float[] rgb,
            float ax, float ay, float az, float bx, float by, float bz,
            float cx, float cy, float cz, float dx, float dy, float dz) {

        quadWinding(matrices, vc, rgb, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz);
        quadWinding(matrices, vc, rgb, dx, dy, dz, cx, cy, cz, bx, by, bz, ax, ay, az);
    }

    private static void quadWinding(MatrixStack matrices, VertexConsumer vc, float[] rgb,
            float ax, float ay, float az, float bx, float by, float bz,
            float cx, float cy, float cz, float dx, float dy, float dz) {
        Entry entry = matrices.peek();
        Matrix4f mat = entry.getPositionMatrix();
        fullVertex(mat, entry, vc, rgb, 1.0F, ax, ay, az, 0, 1, 0);
        fullVertex(mat, entry, vc, rgb, 1.0F, bx, by, bz, 0, 1, 0);
        fullVertex(mat, entry, vc, rgb, 1.0F, cx, cy, cz, 0, 1, 0);
        fullVertex(mat, entry, vc, rgb, 1.0F, dx, dy, dz, 0, 1, 0);
    }

    private static void fullVertex(Matrix4f mat, Entry entry, VertexConsumer vc, float[] rgb, float a,
            float x, float y, float z, float nx, float ny, float nz) {

        vc.vertex(mat, x, y, z).color(rgb[0], rgb[1], rgb[2], a)
                .texture(0.5F, 0.5F).light(0xF000F0);
    }

    private static void glowBox(MatrixStack matrices, VertexConsumer vc, float r, float g, float b,
            float a, float x, float y, float z, float w, float h, float d) {
        Matrix4f mat = matrices.peek().getPositionMatrix();
        float x2 = x + w, y2 = y + h, z2 = z + d;

        glowFace(mat, vc, r, g, b, a, x, y, z, x2, y, z, x2, y, z2, x, y, z2);
        glowFace(mat, vc, r, g, b, a, x, y2, z, x, y2, z2, x2, y2, z2, x2, y2, z);

        glowFace(mat, vc, r, g, b, a, x2, y, z2, x2, y2, z2, x, y2, z2, x, y, z2);
        glowFace(mat, vc, r, g, b, a, x, y, z, x, y2, z, x2, y2, z, x2, y, z);

        glowFace(mat, vc, r, g, b, a, x, y, z2, x, y2, z2, x, y2, z, x, y, z);
        glowFace(mat, vc, r, g, b, a, x2, y, z, x2, y2, z, x2, y2, z2, x2, y, z2);
    }

    private static void glowFace(Matrix4f mat, VertexConsumer vc,
            float r, float g, float b, float a,
            float ax, float ay, float az, float bx, float by, float bz,
            float cx, float cy, float cz, float dx, float dy, float dz) {
        glowVertex(mat, vc, r, g, b, a, ax, ay, az);
        glowVertex(mat, vc, r, g, b, a, bx, by, bz);
        glowVertex(mat, vc, r, g, b, a, cx, cy, cz);
        glowVertex(mat, vc, r, g, b, a, dx, dy, dz);
    }

    private static void glowVertex(Matrix4f mat, VertexConsumer vc,
            float r, float g, float b, float a, float x, float y, float z) {
        vc.vertex(mat, x, y, z).color(r, g, b, a);
    }

    private static void box(MatrixStack matrices, VertexConsumer vc, float r, float g, float b,
            float a, float x, float y, float z, float w, float h, float d) {
        Entry entry = matrices.peek();
        Matrix4f mat = entry.getPositionMatrix();
        float x2 = x + w, y2 = y + h, z2 = z + d;

        face(mat, entry, vc, r, g, b, a, x, y, z, x2, y, z, x2, y, z2, x, y, z2, 0, -1, 0);
        face(mat, entry, vc, r, g, b, a, x, y2, z, x, y2, z2, x2, y2, z2, x2, y2, z, 0, 1, 0);

        face(mat, entry, vc, r, g, b, a, x2, y, z2, x2, y2, z2, x, y2, z2, x, y, z2, 0, 0, 1);
        face(mat, entry, vc, r, g, b, a, x, y, z, x, y2, z, x2, y2, z, x2, y, z, 0, 0, -1);

        face(mat, entry, vc, r, g, b, a, x, y, z2, x, y2, z2, x, y2, z, x, y, z, -1, 0, 0);
        face(mat, entry, vc, r, g, b, a, x2, y, z, x2, y2, z, x2, y2, z2, x2, y, z2, 1, 0, 0);
    }

    private static void face(Matrix4f mat, Entry entry, VertexConsumer vc,
            float r, float g, float b, float a,
            float ax, float ay, float az, float bx, float by, float bz,
            float cx, float cy, float cz, float dx, float dy, float dz,
            float nx, float ny, float nz) {
        fullVertex(mat, entry, vc, new float[]{r, g, b}, a, ax, ay, az, nx, ny, nz);
        fullVertex(mat, entry, vc, new float[]{r, g, b}, a, bx, by, bz, nx, ny, nz);
        fullVertex(mat, entry, vc, new float[]{r, g, b}, a, cx, cy, cz, nx, ny, nz);
        fullVertex(mat, entry, vc, new float[]{r, g, b}, a, dx, dy, dz, nx, ny, nz);
    }

    public static float[] rgb(int color) {
        return new float[]{(color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F};
    }

    public static void renderGuiItem(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c) {
        float[] rgb = rgb(c.color());

        matrices.push();
        matrices.scale(0.0625F, -0.0625F, 0.0625F);
        matrices.translate(-8, -8, 0);

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getText(WHITE));
        Matrix4f mat = matrices.peek().getPositionMatrix();
        tri(mat, vc, rgb, 2, 2, 4, 0, 0, 0, 0, 4);
        if (c.hasFocusing(2)) {
            float t = 4 / 1.5F;
            matrices.push();
            matrices.translate(t / 8, t / 8, 0);
            Matrix4f inset = matrices.peek().getPositionMatrix();
            tri(inset, vc, new float[]{0, 0, 0}, t / 2, t / 2, t, 0, 0, 0, 0, t);
            matrices.pop();
        }

        matrices.translate(-2, 3, 0);
        matrices.scale(10, 10, 10);
        matrices.translate(1, 0.5F, 1);
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(210));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-110));
        matrices.translate(0, 0.05F, 0);
        matrices.scale(0.5F, 0.5F, 0.5F);

        renderHilt(matrices, vcp, c.hilt(), 0xF000F0, net.minecraft.client.render.OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    public static void renderDoubleGuiItem(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c) {
        float[] rgb1 = rgb(c.color());
        float[] rgb2 = c.second().isPresent() ? rgb(c.second().get().color()) : rgb1;
        boolean inv1 = c.hasFocusing(2);
        boolean inv2 = c.second().isPresent() && (c.second().get().focusing() & (1 << 2)) != 0;

        matrices.push();
        matrices.scale(0.0625F, -0.0625F, 0.0625F);
        matrices.translate(-8, -8, 0);

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getText(WHITE));
        Matrix4f mat = matrices.peek().getPositionMatrix();
        tri3(mat, vc, rgb1, 2, 2, 4, 0, 0, 0);
        tri3(mat, vc, rgb2, 0, 4, 2, 2, 0, 0);
        float t = 4 / 1.5F;
        matrices.push();
        matrices.translate(t / 8, t / 8, 0);
        Matrix4f inset = matrices.peek().getPositionMatrix();
        if (inv1) {
            tri3(inset, vc, new float[]{0, 0, 0}, t / 2, t / 2, t, 0, 0, 0);
        }
        if (inv2) {
            tri3(inset, vc, new float[]{0, 0, 0}, 0, t, t / 2, t / 2, 0, 0);
        }
        matrices.pop();

        matrices.translate(-2, 3, 0);
        matrices.scale(10, 10, 10);
        matrices.translate(1, 0.5F, 1);
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(210));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-110));
        matrices.translate(0, 0.05F, 0.05F);
        matrices.scale(0.3F, 0.3F, 0.3F);

        renderDoubleHilt(matrices, vcp, c.hilts(), 0xF000F0, net.minecraft.client.render.OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    private static void tri(Matrix4f mat, VertexConsumer vc, float[] rgb,
            float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy) {

        for (int w = 0; w < 2; w++) {
            float[][] v = w == 0
                    ? new float[][]{{ax, ay}, {bx, by}, {cx, cy}, {dx, dy}}
                    : new float[][]{{dx, dy}, {cx, cy}, {bx, by}, {ax, ay}};
            for (float[] p : v) {
                vc.vertex(mat, p[0], p[1], 0).color(rgb[0], rgb[1], rgb[2], 1)
                        .texture(0.5F, 0.5F).light(0xF000F0);
            }
        }
    }

    private static void tri3(Matrix4f mat, VertexConsumer vc, float[] rgb,
            float ax, float ay, float bx, float by, float cx, float cy) {

        for (int w = 0; w < 2; w++) {
            float[][] v = w == 0
                    ? new float[][]{{ax, ay}, {bx, by}, {cx, cy}, {cx, cy}}
                    : new float[][]{{cx, cy}, {cx, cy}, {bx, by}, {ax, ay}};
            for (float[] p : v) {
                vc.vertex(mat, p[0], p[1], 0).color(rgb[0], rgb[1], rgb[2], 1)
                        .texture(0.5F, 0.5F).light(0xF000F0);
            }
        }
    }

}
