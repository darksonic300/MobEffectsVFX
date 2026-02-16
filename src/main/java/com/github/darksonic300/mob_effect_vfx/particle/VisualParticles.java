package com.github.darksonic300.mob_effect_vfx.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class VisualParticles extends TextureSheetParticle {
	protected VisualParticles(SpriteSet sprite, ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);

		this.setSpriteFromAge(sprite);
		this.rCol = (float) Math.min(1.0F, this.rCol + 0.2);
		this.gCol = (float) Math.min(1.0F, this.gCol + 0.2);
		this.bCol = (float) Math.min(1.0F, this.bCol + 0.2);

		this.friction = 0.8F;
		this.quadSize *= 0.5F;
		this.lifetime = 20;
	}

	@Override
	public void tick() {
		super.tick();

		this.gravity *= 0.8F;
		this.alpha = (-(1 / (float) lifetime) * age + 1);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	protected int getLightColor(float level) {
		return LightTexture.pack(15, 15);
	}
}
