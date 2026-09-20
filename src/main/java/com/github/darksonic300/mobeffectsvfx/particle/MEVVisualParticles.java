package com.github.darksonic300.mobeffectsvfx.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;

public abstract class MEVVisualParticles extends SingleQuadParticle {
	private final double offsetx;
	private double offsety;
	private final double offsetz;
    private final double sourceX, sourceY, sourceZ;

	protected MEVVisualParticles(SpriteSet sprite, ClientLevel level, double x, double y, double z,
                                 double sourceX, double sourceY, double sourceZ) {
		super(level, x, y, z, sprite.first());
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceZ = sourceZ;

        this.setSpriteFromAge(sprite);
		this.rCol = (float) Math.min(1.0F, this.rCol + 0.2);
		this.gCol = (float) Math.min(1.0F, this.gCol + 0.2);
		this.bCol = (float) Math.min(1.0F, this.bCol + 0.2);

		this.offsetx = this.x - this.sourceX;
		this.offsety = this.y - this.sourceY;
		this.offsetz = this.z - this.sourceZ;

		this.friction = 0.8F;
		this.quadSize *= 0.5F;
		this.lifetime = 20;
	}

	@Override
	public void tick() {
		super.tick();

		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		this.gravity *= 0.8F;
		this.alpha = (-(1 / (float) lifetime) * age + 1);

		var mc = Minecraft.getInstance();

		this.yd -= 0.04D * (double) this.gravity;
		this.offsety += this.yd;

        var ticks = mc.getDeltaTracker().getGameTimeDeltaTicks();

		this.setPos(Mth.lerp(ticks, this.x, this.sourceX + offsetx),
				Mth.lerp(ticks, this.y, this.sourceY + offsety),
				Mth.lerp(ticks, this.z, this.sourceZ + offsetz));

		this.yd *= this.friction;
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return Layer.OPAQUE;
	}
}
