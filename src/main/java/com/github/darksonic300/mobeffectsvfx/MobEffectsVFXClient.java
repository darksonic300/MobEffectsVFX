package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(value = MobEffectsVFX.MODID)
public class MobEffectsVFXClient {
    public MobEffectsVFXClient(IEventBus bus) {
        if(!FMLEnvironment.dist.isClient()) return;
        MEVParticles.register(bus);
    }
}
