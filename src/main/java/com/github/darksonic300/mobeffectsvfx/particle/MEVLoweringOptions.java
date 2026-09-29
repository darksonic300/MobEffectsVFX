package com.github.darksonic300.mobeffectsvfx.particle;

import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class MEVLoweringOptions extends MEVParticleOptions {

	public static final MapCodec<MEVLoweringOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(Codec.FLOAT.fieldOf("sourceX").forGetter(MEVLoweringOptions::getSourceX),
					Codec.FLOAT.fieldOf("sourceY").forGetter(MEVLoweringOptions::getSourceY),
					Codec.FLOAT.fieldOf("sourceZ").forGetter(MEVLoweringOptions::getSourceZ))
			.apply(instance, MEVLoweringOptions::new));

	public static final StreamCodec<ByteBuf, MEVLoweringOptions> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, MEVLoweringOptions::getSourceX, ByteBufCodecs.FLOAT, MEVLoweringOptions::getSourceY,
			ByteBufCodecs.FLOAT, MEVLoweringOptions::getSourceZ, MEVLoweringOptions::new);

	public MEVLoweringOptions(float sourceX, float sourceY, float sourceZ) {
		super(sourceX, sourceY, sourceZ);
	}

	@Override
	public ParticleType<?> getType() {
		return MEVParticles.LOWERING_PARTICLES.get();
	}
}
