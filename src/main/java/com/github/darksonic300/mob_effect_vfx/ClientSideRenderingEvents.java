package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.model.IEffectRenderer;
import com.github.darksonic300.mob_effect_vfx.registry.MEVParticles;
import com.github.darksonic300.mob_effect_vfx.registry.VFXRenderers;
import com.github.darksonic300.mob_effect_vfx.util.EffectTypes;
import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.github.darksonic300.mob_effect_vfx.util.MthUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = MobEffectsVFX.MODID, value = Dist.CLIENT)
public class ClientSideRenderingEvents {
	private static final float PARTICLE_RANGE = 0.6F;

	private static long animationDurationMs;
	public static final List<MobEffectsVFX.ActiveEffectVisual> activeVisuals = new CopyOnWriteArrayList<>();
	public static final Set<MobEffect> blocklist = ConcurrentHashMap.newKeySet();
	private static final Cache<UUID, Map<MobEffect, Integer>> effectCache = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES).build();

	@SubscribeEvent
	public static void onLivingTick(EntityTickEvent.Post event) {
		if (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.isClientSide())
			return;
		if (event.getEntity() == null || !(event.getEntity() instanceof LivingEntity living))
			return;

		var level = Minecraft.getInstance().level;
		var map = effectCache.asMap().computeIfAbsent(living.getUUID(), k -> new HashMap<>());

		for (var instance : living.getActiveEffects()) {
			var effect = instance.getEffect().value();
			var duration = instance.getDuration();

			if (blocklist.contains(effect))
				continue;

			if (!map.containsKey(effect)) {
				triggerEffectVFX(living, effect);
				triggerSoundAndParticles(level, living, effect);
			} else if (duration > map.getOrDefault(effect, 0) + MEVConfig.CLIENT.refresh_cooldown.get()) {
				triggerEffectVFX(living, effect);
				triggerSoundAndParticles(level, living, effect);
			}

			map.put(effect, duration);
		}
		effectCache.put(living.getUUID(), map);
	}

	@SubscribeEvent
	public static void onEntityLeave(EntityLeaveLevelEvent event) {
		if (event.getEntity() instanceof LivingEntity && event.getLevel().isClientSide()) {
			effectCache.invalidate(event.getEntity().getUUID());
		}
	}

	@SubscribeEvent
	public static void onPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
		effectCache.invalidateAll();
	}

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		animationDurationMs = MEVConfig.CLIENT.duration.get();

		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || activeVisuals.isEmpty())
			return;

		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

		for (MobEffectsVFX.ActiveEffectVisual activeVisual : activeVisuals) {
			poseStack.pushPose();
			animationLoop(event, bufferSource, activeVisual);
			poseStack.popPose();
		}
		bufferSource.endBatch();
	}

	/**
	 * Handles animation logic for the vfx, the model definition is found in
	 * CuboidModel.java
	 */
	private static void animationLoop(RenderLevelStageEvent event, MultiBufferSource.BufferSource bufferSource,
			MobEffectsVFX.ActiveEffectVisual visual) {
		MobEffectCategory effectCategory = visual.effect().getCategory();
		long elapsedTime = Util.getMillis() - visual.startTime();
		// Calculate animation progress (0.0 to 1.0)
		float progress = (float) elapsedTime / animationDurationMs;

		if (progress >= 1.0F) {
			activeVisuals.remove(visual);
			return;
		}

		IEffectRenderer renderer = VFXRenderers.get(MEVConfig.CLIENT.effect_type.get());
		renderer.initRender(bufferSource, event, visual.source(), progress, effectCategory, visual.color());
	}

	private static void triggerSoundAndParticles(ClientLevel level, LivingEntity entity, MobEffect effect) {
		SoundEvent sound = BuiltInRegistries.SOUND_EVENT
				.get(ResourceLocation.tryParse(MEVConfig.CLIENT.soundEffect.get()));

		Minecraft.getInstance().execute(() -> {
			Minecraft.getInstance().getSoundManager()
					.play(new SimpleSoundInstance(sound == null ? SoundEvents.ENCHANTMENT_TABLE_USE : sound,
							SoundSource.AMBIENT, (float) MEVConfig.CLIENT.volume.get() / 100f, 1.0f,
							RandomSource.create(), entity.blockPosition()));
			spawnParticles(level, effect, entity, MEVColor.getEffectColor(effect));
		});
	}

	private static void spawnParticles(ClientLevel level, MobEffect effect, LivingEntity entity, MEVColor color) {
		if (!MEVConfig.CLIENT.effect_type.get().equals(EffectTypes.RISING))
			return;

		var particle = effect.isBeneficial()
				? MEVParticles.RISING_PARTICLES.get()
				: MEVParticles.LOWERING_PARTICLES.get();

		for (int i = 0; i < 3; i++) {
			level.addParticle(particle, entity.getX() + MthUtils.fRand(-PARTICLE_RANGE, 0f),
					entity.getY() + 1 + MthUtils.fRand(0f, PARTICLE_RANGE),
					entity.getZ() + MthUtils.fRand(0f, PARTICLE_RANGE), color.r(), color.g(), color.b());
		}
		for (int i = 0; i < 3; i++) {
			level.addParticle(particle, entity.getX() + MthUtils.fRand(0f, PARTICLE_RANGE),
					entity.getY() + 1 + MthUtils.fRand(-PARTICLE_RANGE, 0f),
					entity.getZ() + MthUtils.fRand(-PARTICLE_RANGE, 0f), color.r(), color.g(), color.b());
		}
	}

	private static void triggerEffectVFX(LivingEntity source, MobEffect effect) {
		MEVColor color = MEVColor.getEffectColor(effect);
		activeVisuals.add(new MobEffectsVFX.ActiveEffectVisual(source, effect, Util.getMillis(), color));
	}
}
