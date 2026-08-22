package com.github.darksonic300.mobeffectsvfx.registry;

import com.github.darksonic300.mobeffectsvfx.model.CuboidRenderer;
import com.github.darksonic300.mobeffectsvfx.model.FlatCuboidRenderer;
import com.github.darksonic300.mobeffectsvfx.model.IEffectRenderer;
import com.github.darksonic300.mobeffectsvfx.model.RisingCuboidRenderer;
import com.github.darksonic300.mobeffectsvfx.model.StationaryCuboidRenderer;
import com.github.darksonic300.mobeffectsvfx.util.MEVEffectTypes;
import java.util.HashMap;
import java.util.Map;

public record MEVVFXRenderers() {
	public static final Map<MEVEffectTypes, CuboidRenderer> CUBOID_REGISTRY = new HashMap<>();

	static {
		CUBOID_REGISTRY.put(MEVEffectTypes.RISING, new RisingCuboidRenderer());
		CUBOID_REGISTRY.put(MEVEffectTypes.FLAT, new FlatCuboidRenderer());
		CUBOID_REGISTRY.put(MEVEffectTypes.STATIONARY, new StationaryCuboidRenderer());
	}

	public static IEffectRenderer get(MEVEffectTypes type) {
		return CUBOID_REGISTRY.getOrDefault(type, CUBOID_REGISTRY.get(MEVEffectTypes.RISING));
	}
}
