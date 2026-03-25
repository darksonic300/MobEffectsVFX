package com.github.darksonic300.mob_effect_vfx.model;

import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import org.joml.Matrix4f;

public abstract class CuboidRenderer implements IEffectRenderer {
	protected static final float LIGHTEN_FACTOR = 0.3F;

	/**
	 * Renders a cuboid with the given color. Scale and other factors are defined in
	 * the animation logic.
	 *
	 * @param poseStack
	 *            The current PoseStack.
	 * @param buffer
	 *            VertexConsumer to write to.
	 * @param color
	 *            RGB Color with alpha value.
	 * @param category
	 *            The effect to render.
	 */
	public void render(PoseStack poseStack, VertexConsumer buffer, MEVColor color, MobEffectCategory category) {
		Matrix4f matrix = poseStack.last().pose();

		float la = color.a() - 0.8f;
		la = Mth.clamp(la, 0, 1.0f);

		MEVColor transparency = new MEVColor(Math.min(1.0F, color.r() + LIGHTEN_FACTOR),
				Math.min(1.0F, color.g() + LIGHTEN_FACTOR), Math.min(1.0F, color.b() + LIGHTEN_FACTOR), la);

		if (category != MobEffectCategory.HARMFUL)
			drawCuboid(buffer, color, transparency, matrix);
		else
			drawCuboid(buffer, transparency, color, matrix);
	}

	abstract void drawCuboid(VertexConsumer builder, MEVColor opaque, MEVColor transparency, Matrix4f matrix);

	static void addVertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float r, float g, float b,
			float a) {
		buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
	}

	static float calculateAlpha(float alpha, double progress) {
		return (float) Mth.clamp(alpha * Math.exp(-2.5 * progress), 0, 1);
	}
}
