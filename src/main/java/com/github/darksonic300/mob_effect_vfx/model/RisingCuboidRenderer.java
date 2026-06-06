package com.github.darksonic300.mob_effect_vfx.model;

import com.github.darksonic300.mob_effect_vfx.registry.MEVRenderTypes;
import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public class RisingCuboidRenderer extends CuboidRenderer {
	/*
	 * @SubscribeEvent public static void onLivingTick(LivingEvent.LivingTickEvent
	 * event) { if (Minecraft.getInstance().level == null ||
	 * !Minecraft.getInstance().level.isClientSide()) return; ClientLevel level =
	 * Minecraft.getInstance().level; LivingEntity entity = event.getEntity();
	 * if(entity == null || entity.getActiveEffects().isEmpty()) { return; }
	 * 
	 * for (MobEffectInstance entry : entity.getActiveEffects()) { MobEffect effect
	 * = entry.getEffect(); int currentDuration = entry.getDuration(); int
	 * oldDuration = 0;
	 * 
	 * if (blocklist.contains(effect)) continue;
	 * 
	 * boolean flag = false; for(var oldEffect :
	 * effectCache.getOrDefault(entity.getUUID(), Collections.emptySet())) {
	 * if(effect == oldEffect.getEffect()) { oldDuration = oldEffect.getDuration();
	 * flag = true; } }
	 * 
	 * if (!effectCache.containsKey(entity.getUUID()) || !flag) {
	 * triggerEffectVFX(entity, effect); triggerSoundAndParticles(level, entity,
	 * effect); } else if (currentDuration > oldDuration +
	 * MEVConfig.CLIENT.refresh_cooldown.get()) { triggerEffectVFX(entity, effect);
	 * triggerSoundAndParticles(level, entity, effect); } }
	 * 
	 * if(!entity.getActiveEffects().isEmpty()) effectCache.put(entity.getUUID(),
	 * new HashSet<>(entity.getActiveEffects())); }
	 */

	@Override
	public void initRender(MultiBufferSource.BufferSource bufferSource, RenderLevelStageEvent event,
			LivingEntity source, float progress, MobEffectCategory effectCategory, MEVColor color) {
		PoseStack poseStack = event.getPoseStack();
		Vec3 camera = event.getCamera().getPosition();

		float a = calculateAlpha(color.a(), progress);
		color = new MEVColor(color.r(), color.g(), color.b(), a);

		// Calculate animated properties
		float baseSize = source.getDimensions(Pose.STANDING).width() + 0.7F;
		float yOffset = progress * ((source.getDimensions(Pose.STANDING).height() / 2) + 0.5F);

		// Apply camera offset transformation
		float partialTick = event.getPartialTick().getRealtimeDeltaTicks();
		double x = Mth.lerp(partialTick, source.xo, source.getX()) - (baseSize / 2.0) - camera.x;
		double y = Mth.lerp(partialTick, source.yo, source.getY()) - camera.y;
		y = effectCategory != MobEffectCategory.HARMFUL ? y + yOffset : y + 1.7 - yOffset;
		double z = Mth.lerp(partialTick, source.zo, source.getZ()) - (baseSize / 2.0) - camera.z;

		poseStack.translate(x, y, z);
		poseStack.scale(baseSize, baseSize, baseSize);
		this.render(poseStack, bufferSource.getBuffer(MEVRenderTypes.BASE), color, effectCategory);
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

		// FRONT FACE (Z = 0)

		CuboidRenderer.addVertex(buffer, matrix, 0, 0, 0, r, g, b, la);
		CuboidRenderer.addVertex(buffer, matrix, 1, 0, 0, r, g, b, la);
		CuboidRenderer.addVertex(buffer, matrix, 1, 0.7f, 0, r_t, g_t, b_t, a);
		CuboidRenderer.addVertex(buffer, matrix, 0, 0.7f, 0, r_t, g_t, b_t, a);

		// BACK FACE (Z = 1)

		CuboidRenderer.addVertex(buffer, matrix, 0, 0, 1, r, g, b, la);
		CuboidRenderer.addVertex(buffer, matrix, 0, 0.7f, 1, r_t, g_t, b_t, a);
		CuboidRenderer.addVertex(buffer, matrix, 1, 0.7f, 1, r_t, g_t, b_t, a);
		CuboidRenderer.addVertex(buffer, matrix, 1, 0, 1, r, g, b, la);

		// LEFT FACE (X = 0)

		CuboidRenderer.addVertex(buffer, matrix, 0, 0, 0, r, g, b, la);
		CuboidRenderer.addVertex(buffer, matrix, 0, 0, 1, r, g, b, la);
		CuboidRenderer.addVertex(buffer, matrix, 0, 0.7f, 1, r_t, g_t, b_t, a);
		CuboidRenderer.addVertex(buffer, matrix, 0, 0.7f, 0, r_t, g_t, b_t, a);

		// RIGHT FACE (X = 1)

		CuboidRenderer.addVertex(buffer, matrix, 1, 0, 0, r, g, b, la);
		CuboidRenderer.addVertex(buffer, matrix, 1, 0.7f, 0, r_t, g_t, b_t, a);
		CuboidRenderer.addVertex(buffer, matrix, 1, 0.7f, 1f, r_t, g_t, b_t, a);
		CuboidRenderer.addVertex(buffer, matrix, 1, 0, 1f, r, g, b, la);
	}
}
