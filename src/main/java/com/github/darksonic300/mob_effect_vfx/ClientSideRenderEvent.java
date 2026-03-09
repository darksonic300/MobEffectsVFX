package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.model.IEffectRenderer;
import com.github.darksonic300.mob_effect_vfx.registry.MEVParticles;
import com.github.darksonic300.mob_effect_vfx.registry.VFXRenderers;
import com.github.darksonic300.mob_effect_vfx.util.ActivationTriggers;
import com.github.darksonic300.mob_effect_vfx.util.EffectTypes;
import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.github.darksonic300.mob_effect_vfx.util.MthUtils;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSideRenderEvent {
	private static final float PARTICLE_RANGE = 0.6F;

	private static long animationDurationMs;
	public static final List<MobEffectsVFX.ActiveEffectVisual> activeVisuals = new ArrayList<>();
	public static final HashSet<MobEffect> blocklist = Sets.newHashSet();
	private static final Map<UUID, Map<MobEffect, Integer>> effectCache = new HashMap<>();

	@SubscribeEvent
	public static void onLivingTick(LivingEvent.LivingTickEvent event) {
		if (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.isClientSide())
			return;
		if (event.getEntity() == null || event.getEntity().getActiveEffects().isEmpty())
			return;

		var level = Minecraft.getInstance().level;
		var entity = event.getEntity();
		var newMap = new HashMap<MobEffect, Integer>();
		var map = effectCache.getOrDefault(entity.getUUID(), Collections.emptyMap());

		for (var instance : entity.getActiveEffects()) {
			var effect = instance.getEffect();
			var duration = instance.getDuration();

			if (blocklist.contains(effect))
				continue;

			if (!effectCache.containsKey(entity.getUUID()) || !map.containsKey(effect) || map.get(effect) == 0) {
				triggerEffectVFX(entity, effect);
				triggerSoundAndParticles(level, entity, effect);
			} else if (duration > map.getOrDefault(effect, 0) + MEVConfig.CLIENT.refresh_cooldown.get()) {
				triggerEffectVFX(entity, effect);
				triggerSoundAndParticles(level, entity, effect);
			}

			newMap.put(effect, duration);
		}
		effectCache.put(entity.getUUID(), newMap);
	}

	@SubscribeEvent
	public static void onEntityLeave(EntityLeaveLevelEvent event) {
		if (event.getEntity() instanceof LivingEntity && event.getLevel().isClientSide()) {
			effectCache.remove(event.getEntity().getUUID());
		}
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

		var cp = List.copyOf(activeVisuals);
		for (MobEffectsVFX.ActiveEffectVisual visual : cp) {
			poseStack.pushPose();
			animationLoop(event, bufferSource, visual);
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
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS
				.getValue(ResourceLocation.tryParse(MEVConfig.CLIENT.soundEffect.get()));
		// level.playLocalSound(entity.blockPosition(), sound == null ?
		// SoundEvents.ENCHANTMENT_TABLE_USE : sound,
		// SoundSource.AMBIENT, (float) MEVConfig.CLIENT.volume.get() / 100, 1, true);

		spawnParticles(level, effect, entity, MEVColor.getEffectColor(effect));
	}

	private static void spawnParticles(ClientLevel level, MobEffect effect, LivingEntity entity, MEVColor color) {
		if (!MEVConfig.CLIENT.effect_type.get().equals(EffectTypes.RISING))
			return;

		var particle = effect.isBeneficial()
				? MEVParticles.RISING_PARTICLES.get()
				: MEVParticles.LOWERING_PARTICLES.get();

		var random = level.getRandom().fork();
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
	}

	private static void triggerEffectVFX(LivingEntity source, MobEffect effect) {
		MEVColor color = MEVColor.getEffectColor(effect);
		activeVisuals.add(new MobEffectsVFX.ActiveEffectVisual(source, effect, Util.getMillis(), color));
	}
}
