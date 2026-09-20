package com.drag0nge0de.lightsabers.mixin.client;

import com.drag0nge0de.lightsabers.client.ForceClientState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Unique
    private static boolean al$xpShifted;

    @Unique
    private boolean al$shouldShift() {
        MinecraftClient client = MinecraftClient.getInstance();
        return ForceClientState.hasSensitivity() && client.player != null && !client.options.hudHidden;
    }

    @Unique
    private void al$pushShift(MatrixStack matrices) {
        matrices.push();
        matrices.translate(0, -6, 0);
        al$xpShifted = true;
    }

    @Unique
    private void al$popShift(MatrixStack matrices) {
        if (al$xpShifted) {
            matrices.pop();
            al$xpShifted = false;
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"))
    private void al$xpBarPush(DrawContext context, int x, CallbackInfo ci) {
        if (al$shouldShift()) {
            al$pushShift(context.getMatrices());
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("RETURN"))
    private void al$xpBarPop(DrawContext context, int x, CallbackInfo ci) {
        al$popShift(context.getMatrices());
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
    private void al$xpLevelPush(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (al$shouldShift()) {
            al$pushShift(context.getMatrices());
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
    private void al$xpLevelPop(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        al$popShift(context.getMatrices());
    }

    @Inject(method = "renderMountJumpBar", at = @At("HEAD"))
    private void al$jumpBarPush(net.minecraft.entity.JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        if (al$shouldShift()) {
            al$pushShift(context.getMatrices());
        }
    }

    @Inject(method = "renderMountJumpBar", at = @At("RETURN"))
    private void al$jumpBarPop(net.minecraft.entity.JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        al$popShift(context.getMatrices());
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void al$statusBarsPush(DrawContext context, CallbackInfo ci) {
        if (al$shouldShift()) {
            al$pushShift(context.getMatrices());
        }
    }

    @Inject(method = "renderStatusBars", at = @At("RETURN"))
    private void al$statusBarsPop(DrawContext context, CallbackInfo ci) {
        al$popShift(context.getMatrices());
    }
}
