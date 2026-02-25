package com.github.darksonic300.mob_effect_vfx.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class VisualParticles extends TextureSheetParticle {
    private final double offsetx;
    private double offsety;
    private final double offsetz;

	protected VisualParticles(SpriteSet sprite, ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);

		this.setSpriteFromAge(sprite);
		this.rCol = (float) Math.min(1.0F, this.rCol + 0.2);
		this.gCol = (float) Math.min(1.0F, this.gCol + 0.2);
		this.bCol = (float) Math.min(1.0F, this.bCol + 0.2);

        var mc = Minecraft.getInstance();
        assert mc.player != null; // The player WILL be present

        this.offsetx = this.x - mc.player.getX();
        this.offsety = this.y - mc.player.getY();
        this.offsetz = this.z - mc.player.getZ();

		this.friction = 0.8F;
		this.quadSize *= 0.5F;
		this.lifetime = 20;
	}

	@Override
	public void tick() {
        super.tick();
        this.xo = this.x; this.yo = this.y; this.zo = this.z;

		this.gravity *= 0.8F;
		this.alpha = (-(1 / (float) lifetime) * age + 1);

        var mc = Minecraft.getInstance();
        assert mc.player != null;

        this.yd -= 0.04D * (double)this.gravity;
        this.offsety += this.yd;

        this.setPos(
            Mth.lerp(mc.getPartialTick(), this.x, mc.player.getX() + offsetx),
            Mth.lerp(mc.getPartialTick(), this.y, mc.player.getY() + offsety),
            Mth.lerp(mc.getPartialTick(), this.z, mc.player.getZ() + offsetz)
        );


	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}

	@Override
	protected int getLightColor(float level) {
		return LightTexture.pack(15, 15);
	}
}
