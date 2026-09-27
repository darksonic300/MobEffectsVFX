package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.util.MEVVisualLogic;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.util.UncheckedAutoCloseable;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class MEVDataManager {
	public static final Map<Integer, MobEffect> COLOR_TO_EFFECT = new HashMap<>();

	public static final Queue<MEVVisualLogic.ActiveEffectVisual> ACTIVE_VISUALS = new ConcurrentLinkedQueue<>();
	public static final Cache<UUID, Map<MobEffect, Integer>> EFFECT_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofSeconds(1)).build();

	public static final Set<MobEffect> EFFECT_BLOCKLIST = ConcurrentHashMap.newKeySet();
	public static final Set<EntityType<?>> ENTITY_BLOCKLIST = ConcurrentHashMap.newKeySet();

	private static boolean isServerSide;

	private MEVDataManager() {
	}

	public static void initColorMap() {
		for (MobEffect effect : BuiltInRegistries.MOB_EFFECT) {
			COLOR_TO_EFFECT.put(effect.getColor(), effect);
		}
	}

	public static void clearAllState() {
		EFFECT_CACHE.invalidateAll();
		ACTIVE_VISUALS.clear();
		isServerSide = false;
	}

	public static boolean isServerSide() {
		return isServerSide;
	}

	public static void setIsServerSide(boolean isServerSide) {
		MEVDataManager.isServerSide = isServerSide;
	}
}
