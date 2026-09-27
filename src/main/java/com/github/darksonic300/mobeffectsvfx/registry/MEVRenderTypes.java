package com.github.darksonic300.mobeffectsvfx.registry;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

// I gotta redefine the LIGHTNING render type to remove culling and support triangle format
public final class MEVRenderTypes {
	public static final RenderType BASE = RenderType.create("base",
			RenderSetup.builder(RenderPipelines.LIGHTNING.toBuilder().withCull(false).build())
                    .setOitPipelines(RenderPipelines.OIT_LIGHTNING).sortOnUpload().createRenderSetup());

	public static final RenderType FLAT = RenderType.create("flat",
            RenderSetup.builder(RenderPipelines.LIGHTNING.toBuilder()
                            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                            .withCull(false).build())
                    .setOitPipelines(RenderPipelines.OIT_LIGHTNING).sortOnUpload().createRenderSetup());
}
