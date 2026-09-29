package com.github.darksonic300.mobeffectsvfx.particle;

import net.minecraft.core.particles.ParticleOptions;

public abstract class MEVParticleOptions implements ParticleOptions {
	protected final float sourceX, sourceY, sourceZ;

	public MEVParticleOptions(float sourceX, float sourceY, float sourceZ) {
		this.sourceX = sourceX;
		this.sourceY = sourceY;
		this.sourceZ = sourceZ;
	}

	public float getSourceX() {
		return sourceX;
	}

	public float getSourceY() {
		return sourceY;
	}

	public float getSourceZ() {
		return sourceZ;
	}
}
