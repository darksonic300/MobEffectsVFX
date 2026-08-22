package com.github.darksonic300.mobeffectsvfx.registry;

import com.github.darksonic300.mobeffectsvfx.MobEffectsVFX;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@OnlyIn(Dist.CLIENT)
public record MEVParticles() {
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
			.create(ForgeRegistries.PARTICLE_TYPES, MobEffectsVFX.MODID);

	public static final RegistryObject<SimpleParticleType> RISING_PARTICLES = PARTICLE_TYPES
			.register("rising_particles", () -> new SimpleParticleType(true));

	public static final RegistryObject<SimpleParticleType> LOWERING_PARTICLES = PARTICLE_TYPES
			.register("lowering_particles", () -> new SimpleParticleType(true));

	public static void register(IEventBus eventBus) {
		PARTICLE_TYPES.register(eventBus);
	}
}
