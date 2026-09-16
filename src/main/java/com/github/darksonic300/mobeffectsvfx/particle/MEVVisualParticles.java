package com.github.darksonic300.mobeffectsvfx.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class MEVVisualParticles extends SingleQuadParticle {
	private final LivingEntity target;
	private final double offsetx;
	private double offsety;
	private final double offsetz;

    public static final SingleQuadParticle.Layer LAYER = new SingleQuadParticle.Layer(
            false,
            TextureAtlas.LOCATION_PARTICLES,
            RenderPipelines.OPAQUE_PARTICLE
    );

	protected MEVVisualParticles(SpriteSet sprite, ClientLevel level, double x, double y, double z,
			LivingEntity target) {
		super(level, x, y, z, sprite.first());
		this.target = target;

        this.setSpriteFromAge(sprite);
		this.rCol = (float) Math.min(1.0F, this.rCol + 0.2);
		this.gCol = (float) Math.min(1.0F, this.gCol + 0.2);
		this.bCol = (float) Math.min(1.0F, this.bCol + 0.2);

		if (this.target == null) {
			this.remove();
			this.offsetx = 0;
			this.offsety = 0;
			this.offsetz = 0;
			return;
		}

		this.offsetx = this.x - this.target.getX();
		this.offsety = this.y - this.target.getY();
		this.offsetz = this.z - this.target.getZ();

		this.friction = 0.8F;
		this.quadSize *= 0.5F;
		this.lifetime = 20;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.target == null || !this.target.isAlive()) {
			this.remove();
			return;
		}

		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		this.gravity *= 0.8F;
		this.alpha = (-(1 / (float) lifetime) * age + 1);

		var mc = Minecraft.getInstance();

		this.yd -= 0.04D * (double) this.gravity;
		this.offsety += this.yd;

		this.setPos(Mth.lerp(mc.getDeltaTracker().getGameTimeDeltaTicks(), this.x, this.target.getX() + offsetx),
				Mth.lerp(mc.getDeltaTracker().getGameTimeDeltaTicks(), this.y, this.target.getY() + offsety),
				Mth.lerp(mc.getDeltaTracker().getGameTimeDeltaTicks(), this.z, this.target.getZ() + offsetz));

		this.yd *= this.friction;
	}

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return LAYER;
    }
}
