package com.github.darksonic300.mobeffectsvfx.registry;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

// TODO: Fix Opacity issue
// I gotta redefine the render types to remove culling
public final class MEVRenderTypes {
	public static final RenderType BASE = RenderType
			.create("base",
					RenderSetup
							.builder(RenderPipelines.LIGHTNING.toBuilder()
									.withCull(false).build())
							.createRenderSetup());

	public static final RenderType FLAT = RenderType
			.create("flat",
					RenderSetup
							.builder(RenderPipelines.DRAGON_RAYS.toBuilder()
									.withCull(false).build())
							.createRenderSetup());
}
