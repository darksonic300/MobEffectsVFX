package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = MobEffectsVFX.MODID, dist = Dist.CLIENT)
public class MobEffectsVFXClient {

	public MobEffectsVFXClient(IEventBus bus) {
		MEVParticles.register(bus);
	}
}
