package com.github.darksonic300.mobeffectsvfx.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class MEVRisingParticleType extends ParticleType<MEVRisingOptions> {
    public MEVRisingParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public MapCodec<MEVRisingOptions> codec() {
        return MEVRisingOptions.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, MEVRisingOptions> streamCodec() {
        return MEVRisingOptions.STREAM_CODEC;
    }
}
