package com.github.darksonic300.mob_effect_vfx.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

// We extend ShaderStateShard only to use the states freely. This is a registry class.
public class MEVRenderTypes extends RenderStateShard.ShaderStateShard {

	public static RenderType RISING = RenderType.create("rising", DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.QUADS, 256, false, // draggable
			true, // sortOnUpload (needed for transparency)
			RenderType.CompositeState.builder()
					.setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader))
					.setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
					.setCullState(RenderStateShard.NO_CULL).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(false));

	public static RenderType STATIONARY = RenderType.create("stationary", DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader))
					.setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
					.setCullState(RenderStateShard.NO_CULL).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(false));

	public static RenderType FLAT = RenderType.create("flat", DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.TRIANGLES, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader))
					.setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
					.setCullState(RenderStateShard.NO_CULL).setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(false));
}
