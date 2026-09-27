package com.github.darksonic300.mobeffectsvfx.model;

import com.github.darksonic300.mobeffectsvfx.registry.MEVRenderTypes;
import com.github.darksonic300.mobeffectsvfx.registry.MEVVFXRenderers;
import com.github.darksonic300.mobeffectsvfx.util.MEVColor;
import com.github.darksonic300.mobeffectsvfx.util.MEVEffectTypes;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.joml.Matrix4f;

public final class RisingCuboidRenderer extends CuboidRenderer {

    public RisingCuboidRenderer(MobEffectCategory category, MEVColor color) {
        super(category, color);
    }

    @Override
    public void setup(SubmitCustomGeometryEvent event, LivingEntity source, float progress) {
        var partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
		PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;

		float a = calculateAlpha(color.a(), progress);
		color = new MEVColor(color.r(), color.g(), color.b(), a);

		// Calculate animated properties
		float baseSize = source.getDimensions(Pose.STANDING).width() + 0.7F;
		float yOffset = progress * ((source.getDimensions(Pose.STANDING).height() / 2) + 0.5F);

		// Apply camera offset transformation
		double x = Mth.lerp(partialTick, source.xo, source.getX()) - (baseSize / 2.0) - camera.x;
		double y = Mth.lerp(partialTick, source.yo, source.getY()) - camera.y;
		y = category != MobEffectCategory.HARMFUL ? y + yOffset : y + 1.7 - yOffset;
		double z = Mth.lerp(partialTick, source.zo, source.getZ()) - (baseSize / 2.0) - camera.z;

		poseStack.translate(x, y, z);
		poseStack.scale(baseSize, baseSize, baseSize);

        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, this.getRenderType(), MEVVFXRenderers.get(MEVEffectTypes.RISING).apply(this.category, this.color));
	}

	@Override
	public void drawCuboid(VertexConsumer buffer, MEVColor opaque, MEVColor transparency, Matrix4f matrix) {
		float r = opaque.r();
		float g = opaque.g();
		float b = opaque.b();
		float a = opaque.a();

		float r_t = transparency.r();
		float g_t = transparency.g();
		float b_t = transparency.b();
		float la = transparency.a();

        for (int axis = 0; axis <= 1; axis++) {
            for (int fixed = 0; fixed <= 1; fixed++) {
                float f = (float) fixed;
                int inv = 1 - axis;

                float x1 = axis * f;
                float z1 = inv * f;
                float x2 = x1 + inv;
                float z2 = z1 + axis;

                CuboidRenderer.addVertex(buffer, matrix, x1, 0, z1, r, g, b, la);
                CuboidRenderer.addVertex(buffer, matrix, x2, 0, z2, r, g, b, la);
                CuboidRenderer.addVertex(buffer, matrix, x2, 0.7f, z2, r_t, g_t, b_t, a);
                CuboidRenderer.addVertex(buffer, matrix, x1, 0.7f, z1, r_t, g_t, b_t, a);
            }
        }
	}

    @Override
    public RenderType getRenderType() {
        return MEVRenderTypes.BASE;
    }
}
