package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MobEffectsVFX.MODID, dist = Dist.CLIENT)
public class MobEffectsVFXClient {

	public MobEffectsVFXClient(ModContainer container, IEventBus bus) {
		MEVParticles.register(bus);

		container.registerExtensionPoint(IConfigScreenFactory.class,
				(mc, parent) -> new ConfigurationScreen(container, parent));
	}
}
