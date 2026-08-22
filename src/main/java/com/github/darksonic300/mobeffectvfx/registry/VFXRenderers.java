package com.github.darksonic300.mobeffectvfx.registry;

import com.github.darksonic300.mobeffectvfx.model.CuboidRenderer;
import com.github.darksonic300.mobeffectvfx.model.FlatCuboidRenderer;
import com.github.darksonic300.mobeffectvfx.model.IEffectRenderer;
import com.github.darksonic300.mobeffectvfx.model.RisingCuboidRenderer;
import com.github.darksonic300.mobeffectvfx.model.StationaryCuboidRenderer;
import com.github.darksonic300.mobeffectvfx.util.EffectTypes;
import java.util.HashMap;
import java.util.Map;

public record VFXRenderers() {
	public static final Map<EffectTypes, CuboidRenderer> CUBOID_REGISTRY = new HashMap<>();

	static {
		CUBOID_REGISTRY.put(EffectTypes.RISING, new RisingCuboidRenderer());
		CUBOID_REGISTRY.put(EffectTypes.FLAT, new FlatCuboidRenderer());
		CUBOID_REGISTRY.put(EffectTypes.STATIONARY, new StationaryCuboidRenderer());
	}

	public static IEffectRenderer get(EffectTypes type) {
		return CUBOID_REGISTRY.getOrDefault(type, CUBOID_REGISTRY.get(EffectTypes.RISING));
	}
}
