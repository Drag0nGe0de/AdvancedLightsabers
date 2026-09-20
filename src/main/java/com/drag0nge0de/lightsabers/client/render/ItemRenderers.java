package com.drag0nge0de.lightsabers.client.render;

import com.drag0nge0de.lightsabers.client.render.model.HiltModels;
import com.drag0nge0de.lightsabers.client.render.model.TileModels;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.registry.ALBlocks;
import com.drag0nge0de.lightsabers.registry.ALItems;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class ItemRenderers {

    public static final float GUI_SCALE = 0.25F;

    public static volatile float[] fpProbe = null;

    public static volatile float[] tpProbe = null;

    public static volatile int tpAnimApplied = 0;

    public static volatile int tpAnimSkippedWhileSwinging = 0;

    public static volatile int fpAnimApplied = 0;

    public static volatile int guiPoseProbe = -1;

    private static final ThreadLocal<net.minecraft.entity.LivingEntity> RENDER_ENTITY = new ThreadLocal<>();

    private static volatile boolean animDebugLogged = false;

    public static void beginEntityRender(net.minecraft.entity.LivingEntity entity) {
        RENDER_ENTITY.set(entity);
    }

    public static void endEntityRender() {
        RENDER_ENTITY.remove();
    }

    private static net.minecraft.entity.LivingEntity renderEntity() {
        return RENDER_ENTITY.get();
    }

    private static boolean isMainHandStack(net.minecraft.entity.LivingEntity entity, boolean leftHanded) {
        if (entity == null) {
            return false;
        }

        net.minecraft.util.Arm arm = leftHanded ? net.minecraft.util.Arm.LEFT : net.minecraft.util.Arm.RIGHT;
        return arm == entity.getMainArm();
    }

    private static org.joml.Matrix4f leftHandConjugate(org.joml.Matrix4f delta, boolean leftHanded) {
        if (!leftHanded) {
            return delta;
        }

        return new org.joml.Matrix4f().scale(-1.0F, 1.0F, 1.0F).mul(delta).scale(-1.0F, 1.0F, 1.0F);
    }

    private ItemRenderers() {
    }

    private static void applyTpProbe(MatrixStack matrices) {
        float[] p = tpProbe;
        if (p == null) {
            return;
        }

        matrices.translate(p[0], p[1], p[2]);
    }

    private static void applyFpProbe(MatrixStack matrices) {
        float[] p = fpProbe;
        if (p == null) {
            return;
        }

        float rad = (float) (Math.PI / 180.0);
        Matrix4f s = new Matrix4f().scaling(0.2F, 0.2F, 0.2F);
        Matrix4f r0 = new Matrix4f().rotationXYZ(0.0F, -65.0F * rad, 15.0F * rad);
        Matrix4f rp = new Matrix4f().rotationXYZ(0.0F, -65.0F * rad, (15.0F + p[3]) * rad);
        Matrix4f d = new Matrix4f().translation(p[0], p[1], p[2]);
        Matrix4f probe = new Matrix4f(s).invert()
                .mul(new Matrix4f(r0).invert())
                .mul(d)
                .mul(rp)
                .mul(s);
        matrices.multiplyPositionMatrix(probe);
    }

    public static void register() {
        BuiltinItemRendererRegistry.INSTANCE.register(ALItems.LIGHTSABER, new Lightsaber(false));
        BuiltinItemRendererRegistry.INSTANCE.register(ALItems.DOUBLE_LIGHTSABER, new Lightsaber(true));
        BuiltinItemRendererRegistry.INSTANCE.register(ALItems.EMITTER, new Part("emitter", false));
        BuiltinItemRendererRegistry.INSTANCE.register(ALItems.SWITCH_MODULE, new Part("switch_section", false));
        BuiltinItemRendererRegistry.INSTANCE.register(ALItems.GRIP, new Part("body", true));
        BuiltinItemRendererRegistry.INSTANCE.register(ALItems.POMMEL, new Part("pommel", true));
        BuiltinItemRendererRegistry.INSTANCE.register(ALItems.KYBER_CRYSTAL, new Crystal());

        BuiltinItemRendererRegistry.INSTANCE.register(ALBlocks.CRYSTAL_ORE.asItem(), new Crystal());
        BuiltinItemRendererRegistry.INSTANCE.register(ALBlocks.LIGHTSABER_FORGE.asItem(), new Forge(false));
        BuiltinItemRendererRegistry.INSTANCE.register(ALBlocks.LIGHTSABER_FORGE_DARK.asItem(), new Forge(true));
        BuiltinItemRendererRegistry.INSTANCE.register(ALBlocks.DISASSEMBLY_STATION.asItem(), new DisassemblyStation());
        BuiltinItemRendererRegistry.INSTANCE.register(ALBlocks.SITH_SARCOPHAGUS.asItem(), new SithSarcophagusItem());
        BuiltinItemRendererRegistry.INSTANCE.register(ALBlocks.SITH_STONE_COFFIN.asItem(), new SithStoneCoffinItem());
        BuiltinItemRendererRegistry.INSTANCE.register(ALBlocks.LIGHTSABER_STAND.asItem(), new LightsaberStandItem());
        BuiltinItemRendererRegistry.INSTANCE.register(ALBlocks.HOLOCRON_JEDI.asItem(), new HolocronItem(false));
        BuiltinItemRendererRegistry.INSTANCE.register(ALBlocks.HOLOCRON_SITH.asItem(), new HolocronItem(true));
        for (net.minecraft.item.Item hiltItem : ALItems.HILTS.values()) {
            BuiltinItemRendererRegistry.INSTANCE.register(hiltItem, new HiltCollectible());
        }
    }

    private static boolean isGui(ModelTransformationMode mode) {
        return mode == ModelTransformationMode.GUI;
    }

    private static void calOffset(MatrixStack matrices, int count) {
        if (com.drag0nge0de.lightsabers.client.ALShotsBot.iconcal) {
            if (count == 1) matrices.translate(16, 0, 0);
            else if (count == 2) matrices.translate(0, 16, 0);
            else if (count == 3) matrices.translate(0, 0, 16);
        }
    }

    private static void guiLegacyPx(MatrixStack matrices) {
        matrices.scale(0.0625F, -0.0625F, 0.0625F);
        matrices.translate(-8, -8, 0);
    }

    private static void forgeInventoryPreamble(MatrixStack matrices, boolean legacyAngle) {
        matrices.translate(-2, 3, 0);
        matrices.scale(10, 10, 10);
        matrices.translate(1, 0.5F, 1);
        matrices.scale(1, 1, -1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(210));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
        if (!legacyAngle) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        }
    }

    private static void forgeInventoryPreamble(MatrixStack matrices) {
        forgeInventoryPreamble(matrices, false);
    }

    private static boolean isFirstPerson(ModelTransformationMode mode) {
        return mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND
                || mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
    }

    private static int guiLight(int light) {
        return 0xF000F0;
    }

    public static class Lightsaber implements DynamicItemRenderer {

        public static volatile int firstPersonCalls = 0;
        public static volatile int thirdPersonCalls = 0;

        private final boolean doubleSaber;

        public Lightsaber(boolean doubleSaber) {
            this.doubleSaber = doubleSaber;
        }

        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            LightsaberComponent c = LightsaberItemAccess.component(stack);
            boolean jeb = "jeb_".equals(stack.getName().getString());
            boolean leftHanded = mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND
                    || mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
            matrices.push();

            if (mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND) {
                applyFpProbe(matrices);
            }

            boolean tp = mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
                    || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND;

            if (tp) {
                applyTpProbe(matrices);
            }

            matrices.translate(0.5F, 0.5F, 0.5F);

            net.minecraft.entity.LivingEntity animEntity = renderEntity();
            boolean mainHandStack = isMainHandStack(animEntity, leftHanded);
            boolean fpAnim = !tp && (mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND
                    || mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND)
                    && this.doubleSaber && c.active()
                    && com.drag0nge0de.lightsabers.client.SaberAnimations.firstPersonEnabled()
                    && mainHandStack;
            org.joml.Matrix4f fpDelta = null;
            if (fpAnim) {
                fpDelta = com.drag0nge0de.lightsabers.client.SaberAnimations.firstPersonDoubleDelta(
                        animEntity, com.drag0nge0de.lightsabers.client.SaberAnimations.tickDelta());
                fpAnimApplied++;
            }

            if (isGui(mode)) {

                if (doubleSaber) {
                    HiltRenderer.renderDoubleGuiItem(matrices, vcp, c);
                } else {
                    HiltRenderer.renderGuiItem(matrices, vcp, c);
                }
                matrices.pop();
                return;
            }

            if (tp) {

                net.minecraft.entity.LivingEntity holder = renderEntity();
                boolean tpAnim = holder != null && mainHandStack
                        && com.drag0nge0de.lightsabers.client.SaberAnimations.thirdPersonEnabled();

                if (!tpAnim && holder != null && holder.handSwinging
                        && holder.getHandSwingProgress(
                                com.drag0nge0de.lightsabers.client.SaberAnimations.tickDelta()) > 0.0F) {
                    tpAnimSkippedWhileSwinging++;
                }

                float deltaTick = tpAnim ? com.drag0nge0de.lightsabers.client.SaberAnimations.tickDelta() : 0.0F;

                if (tpAnim && !animDebugLogged) {
                    animDebugLogged = true;
                    com.drag0nge0de.lightsabers.client.SaberAnimations.dumpAnimState(this.doubleSaber, holder, deltaTick);
                }

                if (tpAnim) {
                    org.joml.Matrix4f tpDelta = com.drag0nge0de.lightsabers.client.SaberAnimations.thirdPersonDelta(
                            this.doubleSaber, holder, deltaTick);
                    matrices.multiplyPositionMatrix(leftHandConjugate(tpDelta, leftHanded));
                    tpAnimApplied++;
                }

                float[] cgStats = HiltModels.stats(c.hilts()[0]).crossguard();
                boolean cross = cgStats != null && cgStats.length > 0;
                if (doubleSaber && !cross && c.second().isPresent()) {
                    float[] cg2 = HiltModels.stats(c.second().get().emitterHilt()).crossguard();
                    cross = cg2 != null && cg2.length > 0;
                }

                if (cross) {
                    float angle = doubleSaber ? 60.0F : 90.0F;
                    if (tpAnim) {
                        float still = 1.0F - com.drag0nge0de.lightsabers.client.SaberAnimations
                                .walkAmount(holder, deltaTick);
                        angle *= doubleSaber ? still
                                : (holder.isSneaking() ? 0.0F : still);
                    }

                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
                }

                HiltRenderer.noCullRender = true;
            }

            try {
                if (doubleSaber) {

                    if (!tp) {
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                    }
                    if (fpDelta != null) {
                        matrices.multiplyPositionMatrix(leftHandConjugate(fpDelta, leftHanded));
                    }
                    if (c.active()) {
                        HiltRenderer.renderDoubleSaber(matrices, vcp, c, light, overlay, true, jeb);
                    } else {
                        HiltRenderer.renderDoubleHilt(matrices, vcp, c.hilts(), light, overlay);
                    }
                } else {
                    if (!tp) {
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                    }
                    if (fpDelta != null) {
                        matrices.multiplyPositionMatrix(leftHandConjugate(fpDelta, leftHanded));
                    }
                    if (c.active()) {
                        HiltRenderer.renderSaber(matrices, vcp, c, light, overlay, true, jeb);
                    } else {
                        HiltRenderer.renderHilt(matrices, vcp, c.hilts(), light, overlay);
                    }
                }
            } finally {
                if (tp) {
                    HiltRenderer.noCullRender = false;
                }
            }
            matrices.pop();
        }
    }

    public static class Part implements DynamicItemRenderer {
        private final String part;
        private final boolean lower;

        public Part(String part, boolean lower) {
            this.part = part;
            this.lower = lower;
        }

        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            String hilt = LightsaberItemAccess.partHilt(stack);
            matrices.push();
            matrices.translate(0.5F, 0.5F, 0.5F);

            if (isGui(mode)) {

                partGuiPose(matrices, hilt, part);
                VertexConsumer vc = vcp.getBuffer(
                        RenderLayer.getEntityCutoutNoCull(HiltRenderer.texture(hilt, part)));
                HiltRenderer.part(hilt, part).render(matrices, vc, guiLight(light), overlay);
                matrices.pop();
                return;
            }

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            HiltRenderer.renderSinglePart(matrices, vcp, hilt, part, light, overlay,
                    RenderLayer.getEntitySolid(HiltRenderer.texture(hilt, part)), -1);
            matrices.pop();
        }

        public static void partGuiPose(MatrixStack matrices, String hilt, String part) {
            com.drag0nge0de.lightsabers.hilt.HiltStats s = HiltModels.stats(hilt);
            float heightPx = "emitter".equals(part) ? s.emitterH()
                    : "switch_section".equals(part) ? s.switchH()
                    : "body".equals(part) ? s.bodyH()
                    : s.pommelH();

            if (guiPoseProbe == 0) {

                float[] bb = HiltRenderer.partBounds(hilt, part);

                float fit = "pommel".equals(part) && s.pommelH() <= 4
                        ? 2.0F : 1.0F;
                float proj = HiltRenderer.partProjectedExtent(hilt, part) * fit;
                float cap = 0.9375F;
                if (proj > cap) {
                    fit *= cap / proj;
                }

                matrices.scale(fit, fit, fit);
                HiltRenderer.applyPartGuiOrbit(matrices);
                HiltRenderer.applyPartGuiPose(matrices);
                matrices.translate(-(bb[0] + bb[3]) / 2.0F, -(bb[1] + bb[4]) / 2.0F,
                        -(bb[2] + bb[5]) / 2.0F);
                return;
            }

            float fit = "pommel".equals(part) && s.pommelH() <= 4 ? 2.0F : 1.0F;
            if (heightPx * fit > 20.0F) {
                fit = 20.0F / heightPx;
            }

            guiLegacyPx(matrices);
            forgeInventoryPreamble(matrices);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            matrices.translate(0, 0.05F, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-110));
            matrices.scale(fit, fit, fit);
            boolean lowerPart = "body".equals(part) || "pommel".equals(part);
            matrices.translate(0, heightPx * (lowerPart ? -1.0F : 1.0F) / 2.0F * 0.0625F, 0);
        }
    }

    public static class Crystal implements DynamicItemRenderer {
        public static final int DEFAULT_INT = 0x59B9FF;
        public static final float[] DEFAULT_RGB = HiltRenderer.rgb(DEFAULT_INT);

        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            float[] rgb = LightsaberItemAccess.crystalColor(stack);
            matrices.push();
            if (isGui(mode)) {

                matrices.translate(0.5F, 0.5F, 0.5F);

                if (guiPoseProbe == 1) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
                } else if (guiPoseProbe == 2) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-35));
                } else if (guiPoseProbe == 3) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
                } else if (guiPoseProbe == 4) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-35));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
                } else {

                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(35));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
                }

                matrices.scale(1.6F, 1.6F, 1.6F);
                BlockEntityRenderers.renderCrystal(matrices, vcp, rgb, 0.6F, 0, true);
                matrices.pop();
                return;
            }
            if (mode == ModelTransformationMode.FIXED) {

                matrices.translate(0.5F, 0.5F, 0.5F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                matrices.scale(1.4F, 1.4F, 1.4F);
                BlockEntityRenderers.renderCrystal(matrices, vcp, rgb, 0.6F, 0, true);
                matrices.pop();
                return;
            }

            matrices.translate(0.5F, 0.5F, 0.5F);
            BlockEntityRenderers.renderCrystal(matrices, vcp, rgb, 0.6F, 180, true);
            matrices.pop();
        }
    }

    public static class Forge implements DynamicItemRenderer {
        private final boolean dark;

        public Forge(boolean dark) {
            this.dark = dark;
        }

        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            matrices.push();
            matrices.translate(0.5F, 0.5F, 0.5F);
            if (isGui(mode)) {

                guiLegacyPx(matrices);
                forgeInventoryPreamble(matrices);
                matrices.scale(0.65F, 0.65F, 0.65F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
                matrices.translate(0, -0.75F, -0.5F);
                matrices.translate(0.5F, 1.5F, 0.5F);
                matrices.scale(1, -1, -1);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                BlockEntityRenderers.renderModel(matrices, vcp, 0xF000F0, false, false, dark
                        ? com.drag0nge0de.lightsabers.AL.id("textures/models/lightsaber_forge_dark.png")
                        : com.drag0nge0de.lightsabers.AL.id("textures/models/lightsaber_forge_light.png"));
                matrices.pop();
                return;
            }
            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            BlockEntityRenderers.renderModel(matrices, vcp, light, true, false, dark
                    ? com.drag0nge0de.lightsabers.AL.id("textures/models/lightsaber_forge_dark.png")
                    : com.drag0nge0de.lightsabers.AL.id("textures/models/lightsaber_forge_light.png"));
            matrices.pop();
        }
    }

    public static class DisassemblyStation implements DynamicItemRenderer {
        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            matrices.push();
            matrices.translate(0.5F, 0.5F, 0.5F);
            if (isGui(mode)) {

                matrices.scale(0.38F, 0.38F, 0.38F);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(225));

                matrices.scale(1, -1, -1);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                BlockEntityRenderers.renderStationModel(matrices, vcp, light, true, true);
                matrices.pop();
                return;
            }

            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            BlockEntityRenderers.renderStationModel(matrices, vcp, light, true, false);
            matrices.pop();
        }
    }

    public static class SithSarcophagusItem implements DynamicItemRenderer {
        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            matrices.push();
            if (isGui(mode)) {

                matrices.translate(0.5F, 0.5F, 0.0F);
                guiLegacyPx(matrices);
                forgeInventoryPreamble(matrices, true);
                matrices.scale(0.6F, 0.6F, 0.6F);
                matrices.translate(0.5F, -0.8F, 1.0F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                matrices.translate(0.5F, 1.5F, 0.5F);
                matrices.scale(1, -1, -1);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                calOffset(matrices, stack.getCount());
                BlockEntityRenderers.renderSarcophagusModel(matrices, vcp, 0xF000F0, 0.0F);
                matrices.pop();
                return;
            }

            matrices.translate(0.5F, 1.5F, 0.5F);
            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            BlockEntityRenderers.renderSarcophagusModel(matrices, vcp, light, 0.0F);
            matrices.pop();
        }
    }

    public static class SithStoneCoffinItem implements DynamicItemRenderer {
        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            matrices.push();
            if (isGui(mode)) {

                matrices.translate(0.5F, 0.5F, 0.0F);
                guiLegacyPx(matrices);
                forgeInventoryPreamble(matrices, true);
                matrices.scale(0.6F, 0.6F, 0.6F);
                matrices.translate(-0.5F, -1.0F, -0.5F);
                matrices.translate(0.5F, 1.5F, 0.5F);
                matrices.scale(1, -1, -1);
                BlockEntityRenderers.renderStoneCoffinModel(matrices, vcp, 0xF000F0, true);
                matrices.pop();
                return;
            }

            matrices.translate(0.5F, 1.5F, 0.5F);
            matrices.scale(1, -1, -1);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            BlockEntityRenderers.renderStoneCoffinModel(matrices, vcp, light, true);
            matrices.pop();
        }
    }

    public static class HiltCollectible implements DynamicItemRenderer {
        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            String hilt = stack.getItem() instanceof com.drag0nge0de.lightsabers.item.HiltItem hiltItem
                    ? hiltItem.getHilt().getId()
                    : com.drag0nge0de.lightsabers.hilt.Hilt.DEFAULT.getId();
            matrices.push();
            matrices.translate(0.5F, 0.5F, 0.5F);
            if (isGui(mode)) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-110));

                float fit = Math.min(12.0F / HiltModels.stats(hilt).totalHeight(), 1.6F);
                matrices.scale(fit, fit, fit);
                HiltRenderer.renderHilt(matrices, vcp, hilt, 0xF000F0, overlay);
                matrices.pop();
                return;
            }

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            HiltRenderer.renderHilt(matrices, vcp, hilt, light, overlay);
            matrices.pop();
        }
    }

    public static class LightsaberStandItem implements DynamicItemRenderer {
        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            float[] b = BlockEntityRenderers.tileBounds("LightsaberStand");
            float cx = (b[0] + b[3]) / 2.0F;
            float cy = (b[1] + b[4]) / 2.0F;
            float cz = (b[2] + b[5]) / 2.0F;
            float span = Math.max(b[3] - b[0], Math.max(b[4] - b[1], b[5] - b[2]));
            float fit = Math.min(1.5F / span, 1.15F);

            matrices.push();
            if (isGui(mode)) {

                matrices.translate(0.5F, 0.5F, 0.0F);
                guiLegacyPx(matrices);
                forgeInventoryPreamble(matrices, true);
                matrices.scale(2, 2, 2);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                matrices.translate(-0.5F, -0.125F, -0.5F);
                matrices.translate(0.5F, 1.5F, 0.5F);
                matrices.scale(1, -1, -1);
                calOffset(matrices, stack.getCount());
                BlockEntityRenderers.renderStandParts(matrices, vcp, 0xF000F0, ItemStack.EMPTY);
            } else {
                matrices.translate(0.5F, 0.5F, 0.5F);
                matrices.scale(fit * 0.55F, fit * 0.55F, fit * 0.55F);
                matrices.scale(1, -1, -1);
                matrices.translate(-cx, -cy, -cz);
                BlockEntityRenderers.renderStandParts(matrices, vcp, light, ItemStack.EMPTY);
            }
            matrices.pop();
        }
    }

    public static class HolocronItem implements DynamicItemRenderer {
        private final boolean sith;

        public HolocronItem(boolean sith) {
            this.sith = sith;
        }

        @Override
        public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                VertexConsumerProvider vcp, int light, int overlay) {
            matrices.push();
            if (isGui(mode)) {
                Identifier tex = com.drag0nge0de.lightsabers.AL.id(
                      sith ? "textures/item/holocron_sith.png" : "textures/item/holocron_jedi.png");

                VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucentEmissive(tex));

                matrices.translate(0.5F, 0.5F, 0.5F);
                MatrixStack.Entry entry = matrices.peek();
                org.joml.Matrix4f mat = entry.getPositionMatrix();
                vc.vertex(mat, -0.5F, 0.5F, 0.0F).texture(0, 0).color(255, 255, 255, 255)
                      .overlay(overlay).light(0xF000F0).normal(entry, 0, 1, 0);
                vc.vertex(mat, -0.5F, -0.5F, 0.0F).texture(0, 1).color(255, 255, 255, 255)
                      .overlay(overlay).light(0xF000F0).normal(entry, 0, 1, 0);
                vc.vertex(mat, 0.5F, -0.5F, 0.0F).texture(1, 1).color(255, 255, 255, 255)
                      .overlay(overlay).light(0xF000F0).normal(entry, 0, 1, 0);
                vc.vertex(mat, 0.5F, 0.5F, 0.0F).texture(1, 0).color(255, 255, 255, 255)
                      .overlay(overlay).light(0xF000F0).normal(entry, 0, 1, 0);
                matrices.pop();
                return;
            }

            matrices.translate(0.5F, 0.25F, 0.5F);
            HolocronGeometry.draw(matrices, vcp, sith, 0.0F, 0);
            matrices.pop();
        }
    }

    public static void renderSaberPreview(MatrixStack matrices, VertexConsumerProvider vcp,
            LightsaberComponent c, float spin) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) Math.sin(spin / 20) * 2.5F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.sin(spin / 20 + 2) * 2.5F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 + spin));
        matrices.scale(20, 20, 20);
        HiltRenderer.renderSaber(matrices, vcp, c, 0xF000F0, 0, false);
        matrices.pop();
    }
}
