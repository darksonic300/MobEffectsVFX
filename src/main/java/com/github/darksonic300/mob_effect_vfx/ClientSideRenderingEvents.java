package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.util.VisualLogic;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = MobEffectsVFX.MODID, value = Dist.CLIENT)
public class ClientSideRenderingEvents {
	public static final List<VisualLogic.ActiveEffectVisual> activeVisuals = new CopyOnWriteArrayList<>();
	public static final Set<MobEffect> blocklist = ConcurrentHashMap.newKeySet();
	private static final Cache<UUID, Map<MobEffect, Integer>> effectCache = CacheBuilder.newBuilder()
			.expireAfterAccess(3, TimeUnit.MINUTES).build();

	@SubscribeEvent
	public static void onLivingTick(EntityTickEvent.Pre event) {
		if (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.isClientSide())
			return;
		if (!(event.getEntity() instanceof LivingEntity living))
			return;

		try {
			var map = effectCache.asMap().computeIfAbsent(living.getUUID(), k -> new HashMap<>());

			var filteredList = living.getActiveEffects().stream().map(MobEffectInstance::getEffect).map(Holder::value)
					.filter(Predicate.not(blocklist::contains)).toList();

			for (var effect : filteredList) {
				int duration;
				try {
					duration = living.getActiveEffectsMap().get(Holder.direct(effect)).getDuration();
				} catch (Exception e) {
					duration = 0;
				}

				if (!map.containsKey(effect)) {
					MobEffectsVFX.LOGGER.debug("Effect {} not present yet. Applying...", effect);
					VisualLogic.triggerEffectVFX(living, effect);
					VisualLogic.triggerSoundAndParticles(living, effect);
				} else if (duration > map.getOrDefault(effect, 0) + MEVConfig.CLIENT.refresh_cooldown.get()) {
					MobEffectsVFX.LOGGER.debug("Effect {} has to be refreshed. Reapplying...", effect);
					VisualLogic.triggerEffectVFX(living, effect);
					VisualLogic.triggerSoundAndParticles(living, effect);
				}
				map.put(effect, duration);
			}
			effectCache.put(living.getUUID(), map);
		} catch (Exception e) {
			MobEffectsVFX.LOGGER.warn("Error processing effect visuals for entity {}. Skipping.",
					event.getEntity().getUUID(), e);
		}
	}

	@SubscribeEvent
	public static void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
		MobEffectsVFX.LOGGER.info("Player exited. Clearing effect cache");
		effectCache.invalidateAll();
	}

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || activeVisuals.isEmpty())
			return;

		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

		try {
			for (var activeVisual : activeVisuals) {
				poseStack.pushPose();
				VisualLogic.animationLoop(event, bufferSource, activeVisual);
				poseStack.popPose();
			}
			bufferSource.endBatch();
		} catch (Exception e) {
			MobEffectsVFX.LOGGER.error("Error during activeVisuals' iteration", e);
			throw e;
		}
	}
}
