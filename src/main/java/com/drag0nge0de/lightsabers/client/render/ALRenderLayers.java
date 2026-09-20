package com.drag0nge0de.lightsabers.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public final class ALRenderLayers extends RenderLayer {

    public static final RenderLayer GLOW = RenderLayer.of(
            "lightsaber_glow",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(LIGHTNING_PROGRAM)
                    .transparency(LIGHTNING_TRANSPARENCY)
                    .writeMaskState(COLOR_MASK)
                    .cull(DISABLE_CULLING)
                    .depthTest(LEQUAL_DEPTH_TEST)
                    .build(false));

    private ALRenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode,
            int expectedBufferSize, boolean hasCrumbling, boolean translucent,
            Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }
}
