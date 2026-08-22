package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.model.IEffectRenderer;
import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import com.github.darksonic300.mobeffectsvfx.registry.VFXRenderers;
import com.github.darksonic300.mobeffectsvfx.util.EffectTypes;
import com.github.darksonic300.mobeffectsvfx.util.MEVColor;
import com.github.darksonic300.mobeffectsvfx.util.MthUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MobEffectsHandlingEvents {
	private static final float PARTICLE_RANGE = 0.6F;

	private static final Queue<MobEffectsVFX.ActiveEffectVisual> ACTIVE_VISUALS = new ConcurrentLinkedQueue<>();
	private static final Cache<UUID, Map<MobEffect, Integer>> EFFECT_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES).build();

	public static final Set<MobEffect> BLOCKLIST = ConcurrentHashMap.newKeySet();
	public static final Set<EntityType<?>> ENTITY_BLOCKLIST = ConcurrentHashMap.newKeySet();

	@SubscribeEvent
	public static void onLivingTick(final LivingEvent.LivingTickEvent event) {
		final var level = Minecraft.getInstance().level;
		final var entity = event.getEntity();

		if (level == null || !level.isClientSide() || entity == null || ENTITY_BLOCKLIST.contains(entity.getType()))
			return;

		try {
			processLivingVisuals(entity, level);
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
			boolean hasFinished = animationLoop(event, bufferSource, item);
			if (hasFinished)
				iterator.remove();
			poseStack.popPose();
		}

		bufferSource.endBatch();
	}

	private static void processLivingVisuals(final LivingEntity entity, final ClientLevel level) {
		final var map = EFFECT_CACHE.asMap().computeIfAbsent(entity.getUUID(), k -> new HashMap<>());

		for (final var instance : entity.getActiveEffects()) {
			final var effect = instance.getEffect();
			final var duration = instance.getDuration();

			if (BLOCKLIST.contains(effect))
				continue;

			if (!map.containsKey(effect) || duration > map.get(effect) + MEVConfig.CLIENT.refresh_cooldown.get()) {
				triggerEffectVFX(entity, effect);
				triggerSoundAndParticles(level, entity, effect);
			}

			map.put(effect, duration);
		}
	}

	/**
	 * Handles animation logic for the vfx, the model definition is found in
	 * CuboidModel.java
	 */
	private static boolean animationLoop(final RenderLevelStageEvent event,
			final MultiBufferSource.BufferSource bufferSource, MobEffectsVFX.ActiveEffectVisual visual) {
		MobEffectCategory effectCategory = visual.effect().getCategory();
		MEVColor color = MEVColor.getEffectColor(visual.effect());
		long elapsedTime = Util.getMillis() - visual.startTime();
		// Calculate animation progress (0.0 to 1.0)
		float progress = (float) elapsedTime / MEVConfig.CLIENT.duration.get();

		if (progress >= 1.0F) {
			return true;
		}

		IEffectRenderer renderer = VFXRenderers.get(MEVConfig.CLIENT.effect_type.get());
		renderer.initRender(bufferSource, event, visual.source(), progress, effectCategory, color);
		return false;
	}

	private static void triggerSoundAndParticles(final ClientLevel level, final LivingEntity entity,
			final MobEffect effect) {

		SoundEvent sound = ForgeRegistries.SOUND_EVENTS
				.getValue(ResourceLocation.tryParse(MEVConfig.CLIENT.soundEffect.get()));

		Minecraft.getInstance().getSoundManager()
				.play(new SimpleSoundInstance(sound == null ? SoundEvents.ENCHANTMENT_TABLE_USE : sound,
						SoundSource.AMBIENT, (float) MEVConfig.CLIENT.volume.get() / 100f, 1.0f,
						level.getRandom().fork(), entity.blockPosition()));
		spawnParticles(level, effect, entity, MEVColor.getEffectColor(effect));
	}

	private static void spawnParticles(final ClientLevel level, final MobEffect effect, final LivingEntity entity,
			final MEVColor color) {
		if (!MEVConfig.CLIENT.effect_type.get().equals(EffectTypes.RISING))
			return;

		var particle = effect.isBeneficial()
				? MEVParticles.RISING_PARTICLES.get()
				: MEVParticles.LOWERING_PARTICLES.get();

		var random = level.getRandom().fork();

		try {
			for (int i = 0; i < 3; i++) {
				level.addParticle(particle, entity.getX() + MthUtils.fRand(random, -PARTICLE_RANGE, 0f),
						entity.getY() + 1 + MthUtils.fRand(random, 0f, PARTICLE_RANGE),
						entity.getZ() + MthUtils.fRand(random, 0f, PARTICLE_RANGE), color.r(), color.g(), color.b());
			}
			for (int i = 0; i < 3; i++) {
				level.addParticle(particle, entity.getX() + MthUtils.fRand(random, 0f, PARTICLE_RANGE),
						entity.getY() + 1 + MthUtils.fRand(random, -PARTICLE_RANGE, 0f),
						entity.getZ() + MthUtils.fRand(random, -PARTICLE_RANGE, 0f), color.r(), color.g(), color.b());
			}
		} catch (NullPointerException e) {
			LogUtils.getLogger().warn("Failed to add particles for {}", effect);
		}
	}

	private static void triggerEffectVFX(LivingEntity source, MobEffect effect) {
		ACTIVE_VISUALS.add(new MobEffectsVFX.ActiveEffectVisual(source, effect, Util.getMillis()));
	}
}
