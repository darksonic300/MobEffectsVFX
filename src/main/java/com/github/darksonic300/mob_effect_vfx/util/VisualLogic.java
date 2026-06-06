package com.github.darksonic300.mob_effect_vfx.util;

import com.github.darksonic300.mob_effect_vfx.MEVConfig;
import com.github.darksonic300.mob_effect_vfx.MobEffectsVFX;
import com.github.darksonic300.mob_effect_vfx.model.IEffectRenderer;
import com.github.darksonic300.mob_effect_vfx.registry.MEVParticles;
import com.github.darksonic300.mob_effect_vfx.registry.VFXRenderers;
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
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import static com.github.darksonic300.mob_effect_vfx.ClientSideRenderingEvents.activeVisuals;

public final class VisualLogic {
    private static final float PARTICLE_RANGE = 0.6F;

    /**
     * Handles animation logic for the vfx, the model definition is found in
     * CuboidModel.java
     */
    public static void animationLoop(RenderLevelStageEvent event, MultiBufferSource.BufferSource bufferSource,
                                      MobEffectsVFX.ActiveEffectVisual visual) {
        int animationDurationMs = MEVConfig.CLIENT.duration.get();
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

    public static void triggerSoundAndParticles(LivingEntity entity, MobEffect effect) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT
                .get(ResourceLocation.tryParse(MEVConfig.CLIENT.soundEffect.get()));

        Minecraft.getInstance().execute(() -> {
            var clientLevel = Minecraft.getInstance().level;
            if (clientLevel == null)
                return;

            Minecraft.getInstance().getSoundManager()
                    .play(new SimpleSoundInstance(sound == null ? SoundEvents.ENCHANTMENT_TABLE_USE : sound,
                            SoundSource.AMBIENT, (float) MEVConfig.CLIENT.volume.get() / 100f, 1.0f,
                            RandomSource.create(), entity.blockPosition()));
            spawnParticles(clientLevel, effect, entity, MEVColor.getEffectColor(effect));
        });
    }

    public static void spawnParticles(ClientLevel level, MobEffect effect, LivingEntity entity, MEVColor color) {
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

    public static void triggerEffectVFX(LivingEntity source, MobEffect effect) {
        MEVColor color = MEVColor.getEffectColor(effect);
        activeVisuals.add(new MobEffectsVFX.ActiveEffectVisual(source, effect, Util.getMillis(), color));
    }
}
