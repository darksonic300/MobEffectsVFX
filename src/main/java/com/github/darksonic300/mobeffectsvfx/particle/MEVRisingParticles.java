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

@OnlyIn(Dist.CLIENT)
public final class MEVRisingParticles extends MEVVisualParticles {
	private MEVRisingParticles(SpriteSet sprite, ClientLevel level, double x, double y, double z, LivingEntity target) {
		super(sprite, level, x, y, z, target);
		this.gravity = -0.5f;
	}

    @OnlyIn(Dist.CLIENT)
	public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double x, double y, double z,
                                                 double r, double g, double b, RandomSource randomSource) {
            LivingEntity target = null;
            if (clientLevel != null) {
                target = clientLevel.getNearestPlayer(x, y, z, 1, false);
            }

            var particle = new MEVRisingParticles(this.sprite, clientLevel, x, y, z, target);
            particle.setColor((float) r, (float) g, (float) b);
            particle.setSize(5, 5);
            return particle;
        }
    }
}
