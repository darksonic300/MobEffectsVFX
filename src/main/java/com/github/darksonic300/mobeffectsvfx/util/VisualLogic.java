package com.github.darksonic300.mobeffectsvfx.util;

import com.github.darksonic300.mobeffectsvfx.MEVConfig;
import com.github.darksonic300.mobeffectsvfx.model.IEffectRenderer;
import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import com.github.darksonic300.mobeffectsvfx.registry.VFXRenderers;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import static com.github.darksonic300.mobeffectsvfx.MobEffectsHandlingEvents.ACTIVE_VISUALS;

public final class VisualLogic {
	private static final float PARTICLE_RANGE = 0.6F;

	public record ActiveEffectVisual(LivingEntity source, MobEffect effect, long startTime) {
	}

	/**
	 * Handles animation logic for the vfx, the model definition is found in
	 * CuboidModel.java
	 */
	public static boolean animationLoop(final RenderLevelStageEvent event,
			final MultiBufferSource.BufferSource bufferSource, ActiveEffectVisual visual) {
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

	public static void triggerSoundAndParticles(final ClientLevel level, final LivingEntity entity,
			final MobEffect effect) {
		SoundEvent sound = BuiltInRegistries.SOUND_EVENT
				.get(ResourceLocation.tryParse(MEVConfig.CLIENT.soundEffect.get()));

		Minecraft.getInstance().getSoundManager()
				.play(new SimpleSoundInstance(sound == null ? SoundEvents.ENCHANTMENT_TABLE_USE : sound,
						SoundSource.AMBIENT, (float) MEVConfig.CLIENT.volume.get() / 100f, 1.0f,
						level.getRandom().fork(), entity.blockPosition()));
		spawnParticles(level, effect, entity, MEVColor.getEffectColor(effect));
	}

	public static void spawnParticles(ClientLevel level, MobEffect effect, LivingEntity entity, MEVColor color) {
		if (!MEVConfig.CLIENT.effect_type.get().equals(EffectTypes.RISING))
			return;

		var particle = effect.isBeneficial()
				? MEVParticles.RISING_PARTICLES.get()
				: MEVParticles.LOWERING_PARTICLES.get();

		for (int i = 0; i < 3; i++) {
			level.addParticle(particle, entity.getX() + MthUtils.nextFloat(-PARTICLE_RANGE, 0f),
					entity.getY() + 1 + MthUtils.nextFloat(0f, PARTICLE_RANGE),
					entity.getZ() + MthUtils.nextFloat(0f, PARTICLE_RANGE), color.r(), color.g(), color.b());
		}
		for (int i = 0; i < 3; i++) {
			level.addParticle(particle, entity.getX() + MthUtils.nextFloat(0f, PARTICLE_RANGE),
					entity.getY() + 1 + MthUtils.nextFloat(-PARTICLE_RANGE, 0f),
					entity.getZ() + MthUtils.nextFloat(-PARTICLE_RANGE, 0f), color.r(), color.g(), color.b());
		}
	}

	public static void triggerEffectVFX(LivingEntity source, MobEffect effect) {
		ACTIVE_VISUALS.add(new ActiveEffectVisual(source, effect, Util.getMillis()));
	}
}
