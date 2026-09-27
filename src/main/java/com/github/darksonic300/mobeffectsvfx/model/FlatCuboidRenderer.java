package com.github.darksonic300.mobeffectsvfx.model;

import com.github.darksonic300.mobeffectsvfx.registry.MEVRenderTypes;
import com.github.darksonic300.mobeffectsvfx.registry.MEVVFXRenderers;
import com.github.darksonic300.mobeffectsvfx.util.MEVColor;
import com.github.darksonic300.mobeffectsvfx.util.MEVEffectTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.joml.Matrix4f;

public final class FlatCuboidRenderer extends CuboidRenderer {

    public FlatCuboidRenderer(MobEffectCategory category, MEVColor color) {
        super(category, color);
    }

	public void setup(SubmitCustomGeometryEvent event, LivingEntity source, float progress) {
		var deltaTracker = Minecraft.getInstance().getDeltaTracker();

		PoseStack poseStack = event.getPoseStack();
		Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;

		float a = calculateAlpha(color.a(), progress);
		a += 0.1f;
		color = new MEVColor(color.r(), color.g(), color.b(), a);

		// Calculate animated properties
		float scaleOffset = progress * 1.5F;
		float baseSize = category == MobEffectCategory.HARMFUL
				? ((source.getDimensions(Pose.STANDING).width() + 0.7F) * 1.5F) - scaleOffset
				: (source.getScale() + 0.3F) * scaleOffset;

		double visualX = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), source.xo, source.getX()) - (baseSize / 2.0); // Center
																														// the
		// cuboid on the
		// player
		double visualZ = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), source.zo, source.getZ()) - (baseSize / 2.0);

		// Apply camera offset transformation
		double x = visualX - camera.x;
		double y = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), source.yo, source.getY()) - camera.y + 0.01D;
		double z = visualZ - camera.z;

		poseStack.translate(x, y, z);
		poseStack.scale(baseSize, 0, baseSize);

        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, this.getRenderType(), MEVVFXRenderers.get(MEVEffectTypes.FLAT).apply(this.category, this.color));
    }

	@Override
	void drawCuboid(VertexConsumer buffer, MEVColor opaque, MEVColor transparency, Matrix4f matrix) {
		float r = opaque.r();
		float g = opaque.g();
		float b = opaque.b();
		float a = opaque.a();

		float r_t = transparency.r();
		float g_t = transparency.g();
		float b_t = transparency.b();
		float la = transparency.a();

		addVertex(buffer, matrix, 0.5f, 0, 0.5f, r, g, b, la);
		addVertex(buffer, matrix, 0, 0, 1, r_t, g_t, b_t, a);
		addVertex(buffer, matrix, 0, 0, 0, r_t, g_t, b_t, a);

		addVertex(buffer, matrix, 0.5f, 0, 0.5f, r, g, b, la);
		addVertex(buffer, matrix, 1, 0, 0, r_t, g_t, b_t, a);
		addVertex(buffer, matrix, 0, 0, 0, r_t, g_t, b_t, a);

		addVertex(buffer, matrix, 0.5f, 0, 0.5f, r, g, b, la);
		addVertex(buffer, matrix, 1, 0, 0, r_t, g_t, b_t, a);
		addVertex(buffer, matrix, 1, 0, 1, r_t, g_t, b_t, a);

		addVertex(buffer, matrix, 0.5f, 0, 0.5f, r, g, b, la);
		addVertex(buffer, matrix, 0, 0, 1, r_t, g_t, b_t, a);
		addVertex(buffer, matrix, 1, 0, 1, r_t, g_t, b_t, a);
	}

    @Override
    public RenderType getRenderType() {
        return MEVRenderTypes.FLAT;
    }
}
