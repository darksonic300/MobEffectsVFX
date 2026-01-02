package com.github.darksonic300.mob_effect_vfx.registry;

import com.github.darksonic300.mob_effect_vfx.model.CuboidRenderer;
import com.github.darksonic300.mob_effect_vfx.model.FlatCuboidRenderer;
import com.github.darksonic300.mob_effect_vfx.model.IEffectRenderer;
import com.github.darksonic300.mob_effect_vfx.model.RisingCuboidRenderer;
import com.github.darksonic300.mob_effect_vfx.model.StationaryCuboidRenderer;
import com.github.darksonic300.mob_effect_vfx.util.EffectTypes;

import java.util.HashMap;
import java.util.Map;

public class VFXRenderers {
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
