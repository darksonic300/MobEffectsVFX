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
import java.util.HashSet;
import java.util.List;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
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
	private static final HashSet<MobEffect> potions = Sets.newHashSet();

	@SubscribeEvent
	public static void onEffectGain(MobEffectEvent.Added event) {
		var action = MEVConfig.CLIENT.action.get();

		if (event.getEntity() instanceof Player) {
			if (((event.getEffectSource() != null && event.getEffectSource() != event.getEntity())
					&& action == ActivationTriggers.SELF)
					|| ((event.getEffectSource() == null || event.getEffectSource() == event.getEntity())
							&& action == ActivationTriggers.OTHER))
				potions.add(event.getEffectInstance().getEffect());
		}

		activeVisuals.clear();

		var effect = event.getEffectInstance().getEffect();

		if (blocklist.contains(effect) || potions.contains(effect))
			return;

		var oldDuration = event.getOldEffectInstance() == null ? 0 : event.getOldEffectInstance().getDuration();
		var currentEffects = event.getEntity().getActiveEffects().stream().map(MobEffectInstance::getEffect).toList();

		if (!currentEffects.contains(effect)) {
			triggerEffectVFX(event.getEntity(), effect);
            triggerSoundAndParticles(event.getEntity(), effect);
		} else if (event.getEffectInstance().getDuration() > (oldDuration + MEVConfig.CLIENT.refresh_cooldown.get())) {
			triggerEffectVFX(event.getEntity(), effect);
            triggerSoundAndParticles(event.getEntity(), effect);
		}

		potions.clear();
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

        poseStack.pushPose();
		var cp = List.copyOf(activeVisuals);
		for (MobEffectsVFX.ActiveEffectVisual visual : cp) {
			animationLoop(event, bufferSource, visual);
            bufferSource.endBatch();
		}
        poseStack.popPose();
	}

	/**
	 * Handles animation logic for the vfx, the model definition is found in
	 * CuboidModel.java
	 */
	private static void animationLoop(RenderLevelStageEvent event,
			MultiBufferSource.BufferSource bufferSource, MobEffectsVFX.ActiveEffectVisual visual) {
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

	private static void triggerSoundAndParticles(LivingEntity entity, MobEffect effect) {

        if(Minecraft.getInstance().level == null || !Minecraft.getInstance().level.isClientSide()) return;
        var level = Minecraft.getInstance().level;

		SoundEvent sound = ForgeRegistries.SOUND_EVENTS
				.getValue(ResourceLocation.tryParse(MEVConfig.CLIENT.soundEffect.get()));
        level.playLocalSound(entity.blockPosition(), sound == null ? SoundEvents.ENCHANTMENT_TABLE_USE : sound,
				SoundSource.AMBIENT, (float) MEVConfig.CLIENT.volume.get() / 100, 1, true);

        spawnParticles(level, effect, entity, MEVColor.getEffectColor(effect));
	}

	private static void spawnParticles(ClientLevel level, MobEffect effect, LivingEntity entity, MEVColor color) {
		if (!MEVConfig.CLIENT.effect_type.get().equals(EffectTypes.RISING))
			return;

		var particle = effect.isBeneficial()
				? MEVParticles.RISING_PARTICLES.get()
				: MEVParticles.LOWERING_PARTICLES.get();

		var random = level.getRandom();
		for (int i = 0; i < 3; i++) {
            level.addParticle(particle, entity.getX() + fRand(random, -PARTICLE_RANGE, 0f),
					entity.getY() + 1 + fRand(random, 0f, PARTICLE_RANGE),
					entity.getZ() + fRand(random, 0f, PARTICLE_RANGE), color.r(), color.g(), color.b());
		}
		for (int i = 0; i < 3; i++) {
            level.addParticle(particle, entity.getX() + fRand(random, 0f, PARTICLE_RANGE),
					entity.getY() + 1 + fRand(random, -PARTICLE_RANGE, 0f),
					entity.getZ() + fRand(random, -PARTICLE_RANGE, 0f), color.r(), color.g(), color.b());
		}
	}

	private static void triggerEffectVFX(LivingEntity source, MobEffect effect) {
		MEVColor color = MEVColor.getEffectColor(effect);
		activeVisuals.add(new MobEffectsVFX.ActiveEffectVisual(source, effect, Util.getMillis(), color));
	}

    public static float fRand(RandomSource random, float min, float max) {
        return min + (max - min) * random.nextFloat();
        //return ThreadLocalRandom.current().nextFloat(min, max);
    }
}
