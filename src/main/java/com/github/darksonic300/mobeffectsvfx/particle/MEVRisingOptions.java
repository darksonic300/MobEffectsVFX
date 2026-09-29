package com.github.darksonic300.mobeffectsvfx.particle;

import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class MEVRisingOptions extends MEVParticleOptions {

	public static final MapCodec<MEVRisingOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(Codec.FLOAT.fieldOf("sourceX").forGetter(MEVRisingOptions::getSourceX),
					Codec.FLOAT.fieldOf("sourceY").forGetter(MEVRisingOptions::getSourceY),
					Codec.FLOAT.fieldOf("sourceZ").forGetter(MEVRisingOptions::getSourceZ))
			.apply(instance, MEVRisingOptions::new));

	public static final StreamCodec<ByteBuf, MEVRisingOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT,
			MEVRisingOptions::getSourceX, ByteBufCodecs.FLOAT, MEVRisingOptions::getSourceY, ByteBufCodecs.FLOAT,
			MEVRisingOptions::getSourceZ, MEVRisingOptions::new);

	public MEVRisingOptions(float sourceX, float sourceY, float sourceZ) {
		super(sourceX, sourceY, sourceZ);
	}

	@Override
	public ParticleType<?> getType() {
		return MEVParticles.RISING_PARTICLES.get();
	}
}
