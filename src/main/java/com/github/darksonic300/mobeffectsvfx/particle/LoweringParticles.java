package com.github.darksonic300.mobeffectsvfx.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class LoweringParticles extends VisualParticles {
	protected LoweringParticles(SpriteSet sprite, ClientLevel level, double x, double y, double z) {
		super(sprite, level, x, y, z);
		this.gravity = 0.5f;
	}

	@OnlyIn(Dist.CLIENT)
	public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(@NotNull SimpleParticleType particleType, @NotNull ClientLevel level, double x,
				double y, double z, double r, double g, double b) {
			var particle = new LoweringParticles(this.sprite, level, x, y, z);
			particle.setColor((float) r, (float) g, (float) b);
			particle.setSize(5, 5);
			return particle;
		}
	}
}
