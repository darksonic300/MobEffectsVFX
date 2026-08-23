package com.github.darksonic300.mobeffectsvfx.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MEVLoweringParticles extends MEVVisualParticles {
	private MEVLoweringParticles(SpriteSet sprite, ClientLevel level, double x, double y, double z,
			LivingEntity target) {
		super(sprite, level, x, y, z, target);
		this.gravity = 0.5f;
	}

	@OnlyIn(Dist.CLIENT)
	public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z,
				double r, double g, double b) {
			LivingEntity target = null;
			if (level != null) {
				// We try to find the entity that is closest to the particle's spawn position
				// This is a bit of a hack since ParticleProvider doesn't receive the entity
				target = level.getNearestEntity(LivingEntity.class,
						net.minecraft.world.entity.ai.targeting.TargetingConditions.DEFAULT, null, x, y, z,
						new net.minecraft.world.phys.AABB(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1));
			}

			var particle = new MEVLoweringParticles(this.sprite, level, x, y, z, target);
			particle.setColor((float) r, (float) g, (float) b);
			particle.setSize(5, 5);
			return particle;
		}
	}
}
