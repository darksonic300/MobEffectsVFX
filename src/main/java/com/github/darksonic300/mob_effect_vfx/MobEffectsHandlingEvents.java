package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.util.VisualLogic;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@EventBusSubscriber(modid = MobEffectsVFX.MODID, value = Dist.CLIENT)
public class MobEffectsHandlingEvents {
	public static final Queue<VisualLogic.ActiveEffectVisual> ACTIVE_VISUALS = new ConcurrentLinkedQueue<>();
	private static final Cache<UUID, Map<MobEffect, Integer>> EFFECT_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES).build();

	public static final Set<MobEffect> BLOCKLIST = ConcurrentHashMap.newKeySet();
	public static final Set<EntityType<?>> ENTITY_BLOCKLIST = ConcurrentHashMap.newKeySet();

	@SubscribeEvent
	public static void onLivingTick(final EntityTickEvent.Pre event) {
		final var level = Minecraft.getInstance().level;
		final var entity = event.getEntity();

		if (level == null || !level.isClientSide() || !entity.getClass().isInstance(LivingEntity.class)
				|| ENTITY_BLOCKLIST.contains(entity.getType()))
			return;

		try {
			processLivingVisuals((LivingEntity) entity, level);
		} catch (Throwable t) {
			LogUtils.getLogger().warn("MobEffectsVFX threw an exception: {}", t.fillInStackTrace());
		}
	}

	@SubscribeEvent
	public static void onEntityLeave(final EntityLeaveLevelEvent event) {
		if (event.getEntity() instanceof LivingEntity && event.getLevel().isClientSide()) {
			EFFECT_CACHE.invalidate(event.getEntity().getUUID());
		}
	}

	@SubscribeEvent
	public static void onPlayerLeave(final ClientPlayerNetworkEvent event) {
		EFFECT_CACHE.invalidateAll();
	}

	@SubscribeEvent
	public static void onRenderLevelStage(final RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || ACTIVE_VISUALS.isEmpty())
			return;

		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

		var iterator = ACTIVE_VISUALS.iterator();
		while (iterator.hasNext()) {
			var item = iterator.next();
			poseStack.pushPose();
			boolean hasFinished = VisualLogic.animationLoop(event, bufferSource, item);
			if (hasFinished)
				iterator.remove();
			poseStack.popPose();
		}

		bufferSource.endBatch();
	}

	private static void processLivingVisuals(final LivingEntity entity, final ClientLevel level) {
		final var map = EFFECT_CACHE.asMap().computeIfAbsent(entity.getUUID(), k -> new HashMap<>());

		for (final var instance : entity.getActiveEffects()) {
			final var effect = instance.getEffect().value();
			final var duration = instance.getDuration();

			if (BLOCKLIST.contains(effect))
				continue;

			if (!map.containsKey(effect) || duration > map.get(effect) + MEVConfig.CLIENT.refresh_cooldown.get()) {
				VisualLogic.triggerEffectVFX(entity, effect);
				VisualLogic.triggerSoundAndParticles(level, entity, effect);
			}

			map.put(effect, duration);
		}
	}
}
