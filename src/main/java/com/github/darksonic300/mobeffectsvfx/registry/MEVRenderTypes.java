package com.github.darksonic300.mobeffectsvfx.registry;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

// I gotta redefine the render types to remove culling
public final class MEVRenderTypes {
	public static final RenderType BASE = RenderType
			.create("base",
					RenderSetup
							.builder(RenderPipelines.LIGHTNING.toBuilder()
									.withCull(false)
									.withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
									.build())
							.sortOnUpload()
							.createRenderSetup());

	public static final RenderType FLAT = RenderType
			.create("flat",
					RenderSetup
							.builder(RenderPipelines.DRAGON_RAYS.toBuilder()
									.withCull(false)
									.withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
									.build())
							.sortOnUpload()
							.createRenderSetup());
}
