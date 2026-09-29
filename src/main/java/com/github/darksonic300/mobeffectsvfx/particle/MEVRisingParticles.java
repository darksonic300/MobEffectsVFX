package com.github.darksonic300.mobeffectsvfx.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.Nullable;

public final class MEVRisingParticles extends MEVVisualParticles {
	private MEVRisingParticles(SpriteSet sprite, ClientLevel level, double x, double y, double z, double sourceX,
			double sourceY, double sourceZ) {
		super(sprite, level, x, y, z, sourceX, sourceY, sourceZ);
		this.gravity = -0.5f;
	}

	public record Provider(SpriteSet sprite) implements ParticleProvider<MEVRisingOptions> {
		@Override
		public @Nullable Particle createParticle(MEVRisingOptions type, ClientLevel clientLevel, double x, double y,
				double z, double r, double g, double b, RandomSource randomSource) {
			var particle = new MEVRisingParticles(this.sprite, clientLevel, x, y, z, type.sourceX, type.sourceY,
					type.sourceZ);
			particle.setColor((float) r, (float) g, (float) b);
			particle.setSize(5, 5);
			return particle;
		}
	}
}
