package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.model.IEffectRenderer;
import com.github.darksonic300.mob_effect_vfx.registry.MEVParticles;
import com.github.darksonic300.mob_effect_vfx.registry.VFXRenderers;
import com.github.darksonic300.mob_effect_vfx.util.ActivationTriggers;
import com.github.darksonic300.mob_effect_vfx.util.EffectTypes;
import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSideRenderEvent {
	private static final float PARTICLE_RANGE = 0.6F;

	private static long animationDurationMs;
	private static final Map<MobEffect, Integer> activeEffectsTracker = new HashMap<>();
	public static final List<MobEffectsVFX.ActiveEffectVisual> activeVisuals = new ArrayList<>();

	public static final HashSet<MobEffect> blocklist = Sets.newHashSet();
	private static final HashSet<MobEffect> potions = Sets.newHashSet();

	@SubscribeEvent
	public static void checkTriggers(MobEffectEvent.Added event) {
		var action = MEVConfig.CLIENT.action.get();

		if (((event.getEffectSource() != event.getEntity() && event.getEffectSource() != null)
				&& action == ActivationTriggers.SELF)
				|| ((event.getEffectSource() == event.getEntity() || event.getEffectSource() == null)
						&& action == ActivationTriggers.OTHER))
			potions.add(event.getEffectInstance().getEffect());
	}

	@SubscribeEvent
	public static void registerRenderers(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			activeEffectsTracker.clear();
			activeVisuals.clear();
			return;
		}
		LocalPlayer player = mc.player;

		Map<MobEffect, Integer> currentEffects = new HashMap<>();

		for (MobEffectInstance instance : player.getActiveEffects()) {
			MobEffect effect = instance.getEffect();
			int currentDuration = instance.getDuration();

			if (blocklist.contains(effect) || potions.contains(effect))
				continue;

			if (!activeEffectsTracker.containsKey(effect)) {
				triggerEffectVFX(effect);
				triggerSoundAndParticles(mc, player, effect);
			} else if (currentDuration > (activeEffectsTracker.get(effect) + MEVConfig.CLIENT.refresh_cooldown.get())) {
				triggerEffectVFX(effect);
				triggerSoundAndParticles(mc, player, effect);
			}

			currentEffects.put(effect, currentDuration);
		}

		potions.clear();

		activeEffectsTracker.clear();
		activeEffectsTracker.putAll(currentEffects);
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
		Vec3 cameraPos = event.getCamera().getPosition();
		long currentTime = Util.getMillis();
		IEffectRenderer renderer = VFXRenderers.get(MEVConfig.CLIENT.effect_type.get());

		poseStack.pushPose();

		var copy = List.copyOf(activeVisuals);
		for (MobEffectsVFX.ActiveEffectVisual visual : copy)
			animationLoop(event, renderer, bufferSource, visual, currentTime, poseStack, mc.player, cameraPos);
		bufferSource.endBatch();

		poseStack.popPose();
	}

	/**
	 * Handles animation logic for the vfx, the model definition is found in
	 * CuboidModel.java
	 */
	private static void animationLoop(RenderLevelStageEvent event, IEffectRenderer renderer,
			MultiBufferSource.BufferSource bufferSource, MobEffectsVFX.ActiveEffectVisual visual, long currentTime,
			PoseStack poseStack, Player player, Vec3 camera) {
		MobEffectCategory effectCategory = visual.effect().getCategory();
		long elapsedTime = currentTime - visual.startTime();
		// Calculate animation progress (0.0 to 1.0)
		float progress = (float) elapsedTime / animationDurationMs;

		if (progress >= 1.0F) {
			activeVisuals.remove(visual);
			return;
		}

		poseStack.pushPose();
		renderer.startRendering(bufferSource, event, poseStack, player, camera, progress, effectCategory,
				visual.color());
		poseStack.popPose();
	}

	private static void triggerSoundAndParticles(Minecraft mc, LocalPlayer player, MobEffect effect) {
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS
				.getValue(ResourceLocation.tryParse(MEVConfig.CLIENT.soundEffect.get()));

		mc.level.playLocalSound(player.blockPosition(), sound == null ? SoundEvents.ENCHANTMENT_TABLE_USE : sound,
				SoundSource.PLAYERS, (float) MEVConfig.CLIENT.volume.get() / 100, 1, true);
		spawnParticles(effect, player, getEffectColor(effect));
	}

	private static void spawnParticles(MobEffect effect, LocalPlayer player, MEVColor color) {
		if (!MEVConfig.CLIENT.effect_type.get().equals(EffectTypes.RISING))
			return;

		var particle = effect.isBeneficial()
				? MEVParticles.RISING_PARTICLES.get()
				: MEVParticles.LOWERING_PARTICLES.get();

		var random = player.level().random;
		for (int i = 0; i < 3; i++) {
			player.level().addParticle(particle, player.getX() + randomRange(random, -PARTICLE_RANGE, 0f),
					player.getY() + 1 + randomRange(random, 0f, PARTICLE_RANGE),
					player.getZ() + randomRange(random, 0f, PARTICLE_RANGE), color.r(), color.g(), color.b());
		}
		for (int i = 0; i < 3; i++) {
			player.level().addParticle(particle, player.getX() + randomRange(random, 0f, PARTICLE_RANGE),
					player.getY() + 1 + randomRange(random, -PARTICLE_RANGE, 0f),
					player.getZ() + randomRange(random, -PARTICLE_RANGE, 0f), color.r(), color.g(), color.b());
		}
	}

	private static void triggerEffectVFX(MobEffect effect) {
		MEVColor color = getEffectColor(effect);
		activeVisuals.add(new MobEffectsVFX.ActiveEffectVisual(effect, Util.getMillis(), color));
	}

	private static MEVColor getEffectColor(MobEffect effect) {
		int color = effect.getColor();
		float r = ((color >> 16) & 0xFF) / 255.0F;
		float g = ((color >> 8) & 0xFF) / 255.0F;
		float b = (color & 0xFF) / 255.0F;

		float a = MEVConfig.CLIENT.opacity.get().floatValue();

		return new MEVColor(r, g, b, a);
	}

	private static float randomRange(RandomSource random, float min, float max) {
		return min + (max - min) * random.nextFloat();
	}
}
