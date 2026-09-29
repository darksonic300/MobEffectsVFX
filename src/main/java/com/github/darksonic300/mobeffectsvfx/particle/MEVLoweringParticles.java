package com.github.darksonic300.mobeffectsvfx.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public final class MEVLoweringParticles extends MEVVisualParticles {
	private MEVLoweringParticles(SpriteSet sprite, ClientLevel level, double x, double y, double z, double sourceX,
			double sourceY, double sourceZ) {
		super(sprite, level, x, y, z, sourceX, sourceY, sourceZ);
		this.gravity = 0.5f;
	}

	public record Provider(SpriteSet sprite) implements ParticleProvider<MEVLoweringOptions> {
		@Override
		public @Nullable Particle createParticle(MEVLoweringOptions type, ClientLevel clientLevel, double x, double y,
				double z, double r, double g, double b, RandomSource randomSource) {
			var particle = new MEVLoweringParticles(this.sprite, clientLevel, x, y, z, type.sourceX, type.sourceY,
					type.sourceZ);
			particle.setColor((float) r, (float) g, (float) b);
			particle.setSize(5, 5);
			return particle;
		}
	}
}
