package com.github.darksonic300.mobeffectsvfx.registry;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import static net.minecraft.client.renderer.RenderPipelines.MATRICES_FOG_SNIPPET;

public final class MEVRenderTypes {

    //TODO: is this worth it?
    public static final RenderType BASE = RenderType.create("base", RenderSetup.builder(
                    RenderPipelines.LIGHTNING
    )
            .createRenderSetup());

	public static final RenderType FLAT = RenderType.create("flat", RenderSetup.builder(
            RenderPipeline.builder(MATRICES_FOG_SNIPPET)
                    .withLocation("pipeline/lightning")
                    .withVertexShader("core/rendertype_lightning")
                    .withFragmentShader("core/rendertype_lightning")
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .build()
    ).createRenderSetup());
}
