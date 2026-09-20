package com.github.darksonic300.mobeffectsvfx.registry;

import com.github.darksonic300.mobeffectsvfx.MobEffectsVFX;
import com.github.darksonic300.mobeffectsvfx.particle.MEVLoweringParticleType;
import com.github.darksonic300.mobeffectsvfx.particle.MEVRisingParticleType;
import com.github.darksonic300.mobeffectsvfx.particle.MEVRisingParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public record MEVParticles() {
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
			.create(BuiltInRegistries.PARTICLE_TYPE, MobEffectsVFX.MODID);

	public static final DeferredHolder<ParticleType<?>, MEVRisingParticleType> RISING_PARTICLES = PARTICLE_TYPES
			.register("rising_particles", () -> new MEVRisingParticleType(false));

	public static final DeferredHolder<ParticleType<?>, MEVLoweringParticleType> LOWERING_PARTICLES = PARTICLE_TYPES
			.register("lowering_particles", () -> new MEVLoweringParticleType(false));

	public static void register(IEventBus eventBus) {
		PARTICLE_TYPES.register(eventBus);
	}
}
