package com.github.darksonic300.mobeffectsvfx.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

// I gotta redefine the LIGHTNING render type to remove culling and support triangle format
public final class MEVRenderTypes {
	public static final RenderType BASE = RenderType.create("base",
			RenderSetup.builder(RenderPipelines.LIGHTNING.toBuilder()
                    .withCull(false)
                    .build()).createRenderSetup());

    public static final RenderType FLAT = RenderType.create("flat",
			RenderSetup.builder(RenderPipelines.LIGHTNING.toBuilder()
                    .withCull(false)
					.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
					.build()).createRenderSetup());
}
