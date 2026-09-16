package com.github.darksonic300.mobeffectsvfx.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class MEVRenderTypes {

	// TODO: is this worth it?
	public static final RenderType BASE = RenderType.create("base",
			RenderSetup.builder(RenderPipelines.LIGHTNING).createRenderSetup());

	// We need this for the triangle format
	public static final RenderType FLAT = RenderType.create("flat",
			RenderSetup
					.builder(RenderPipelines.LIGHTNING.toBuilder()
							.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES).build())
					.createRenderSetup());
}
