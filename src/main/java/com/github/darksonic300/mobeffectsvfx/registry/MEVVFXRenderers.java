package com.github.darksonic300.mobeffectsvfx.registry;

import com.github.darksonic300.mobeffectsvfx.model.CuboidRenderer;
import com.github.darksonic300.mobeffectsvfx.model.FlatCuboidRenderer;
import com.github.darksonic300.mobeffectsvfx.model.RisingCuboidRenderer;
import com.github.darksonic300.mobeffectsvfx.model.StationaryCuboidRenderer;
import com.github.darksonic300.mobeffectsvfx.util.MEVColor;
import com.github.darksonic300.mobeffectsvfx.util.MEVEffectTypes;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public record MEVVFXRenderers() {
	public static final Map<MEVEffectTypes, BiFunction<MobEffectCategory, MEVColor, CuboidRenderer>> CUBOID_REGISTRY = new HashMap<>();

	static {
		CUBOID_REGISTRY.put(MEVEffectTypes.RISING, RisingCuboidRenderer::new);
		CUBOID_REGISTRY.put(MEVEffectTypes.FLAT, FlatCuboidRenderer::new);
		CUBOID_REGISTRY.put(MEVEffectTypes.STATIONARY, StationaryCuboidRenderer::new);
	}

	public static BiFunction<MobEffectCategory, MEVColor, CuboidRenderer> get(MEVEffectTypes type) {
		return CUBOID_REGISTRY.getOrDefault(type, CUBOID_REGISTRY.get(MEVEffectTypes.RISING));
	}
}
