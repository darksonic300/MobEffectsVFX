package com.github.darksonic300.mobeffectsvfx.particle;

import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class MEVLoweringParticleType extends ParticleType<MEVLoweringOptions> {
    public MEVLoweringParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public MapCodec<MEVLoweringOptions> codec() {
        return MEVLoweringOptions.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, MEVLoweringOptions> streamCodec() {
        return MEVLoweringOptions.STREAM_CODEC;
    }
}
