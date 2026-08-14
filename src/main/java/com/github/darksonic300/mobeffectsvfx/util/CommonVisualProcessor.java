package com.github.darksonic300.mobeffectsvfx.util;

import com.github.darksonic300.mobeffectsvfx.MEVConfig;
import com.github.darksonic300.mobeffectsvfx.MEVDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommonVisualProcessor {
	public static void processVisual(final int entityId) {
		final var level = Minecraft.getInstance().level;
		final var entity = (LivingEntity) level.getEntity(entityId);
		final var map = MEVDataManager.EFFECT_CACHE.asMap().computeIfAbsent(entity.getUUID(), k -> new HashMap<>());

		for (final var instance : entity.getActiveEffects()) {
			executeInstance(entity, level, instance, map);
		}
	}

	public static void processVisual(final int entityId, MobEffectInstance instance) {
		final var level = Minecraft.getInstance().level;
		final var entity = (LivingEntity) level.getEntity(entityId);
		final var map = MEVDataManager.EFFECT_CACHE.asMap().computeIfAbsent(entity.getUUID(), k -> new HashMap<>());

		executeInstance(entity, level, instance, map);
	}

	public static void processVisual(final int entityId, Collection<MobEffectInstance> instanceList) {
		final var level = Minecraft.getInstance().level;
		final var entity = (LivingEntity) level.getEntity(entityId);
		final var map = MEVDataManager.EFFECT_CACHE.asMap().computeIfAbsent(entity.getUUID(), k -> new HashMap<>());

		for (final var instance : instanceList) {
			executeInstance(entity, level, instance, map);
		}
	}

	static void executeInstance(LivingEntity entity, ClientLevel level, MobEffectInstance instance,
			Map<MobEffect, Integer> map) {
		final var effect = instance.getEffect().value();
		final var duration = instance.getDuration();

		if (MEVDataManager.EFFECT_BLOCKLIST.contains(effect))
			return;

		if (!map.containsKey(effect) || duration > map.get(effect) + MEVConfig.CLIENT.refresh_cooldown.get()) {
			VisualLogic.triggerEffectVFX(entity, effect);
			VisualLogic.triggerSoundAndParticles(level, entity, effect);
		}

		map.put(effect, duration);
	}
}
