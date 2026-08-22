package com.github.darksonic300.mobeffectsvfx.mixin.client;

import com.github.darksonic300.mobeffectsvfx.MEVDataManager;
import com.github.darksonic300.mobeffectsvfx.MobEffectsVFX;
import com.github.darksonic300.mobeffectsvfx.util.MEVCommonVisualProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class MEVLivingDataInjector extends Entity {

	@Shadow
	@Final
	public static EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES;

	public MEVLivingDataInjector(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "onSyncedDataUpdated", at = @At("TAIL"))
	private void mobeffectsvfx$onEffectDataUpdated(EntityDataAccessor<?> pKey, CallbackInfo ci) {
		if (MEVDataManager.isServerSide())
			return;

		final var level = Minecraft.getInstance().level;
		if (level == null || !level.isClientSide() || !DATA_EFFECT_PARTICLES.equals(pKey))
			return;

		LivingEntity entity = (LivingEntity) (Object) this;
		var instanceList = new HashSet<MobEffectInstance>();

		var particleList = entity.getEntityData().get(LivingEntity.DATA_EFFECT_PARTICLES);

		for (var particle : particleList) {
			if (particle instanceof ColorParticleOption colorParticle) {
				int color = FastColor.ARGB32.colorFromFloat(0, colorParticle.getRed(), colorParticle.getGreen(),
						colorParticle.getBlue());

				MobEffect effect = MEVDataManager.COLOR_TO_EFFECT.get(color);
				if (effect != null) {
					instanceList.add(new MobEffectInstance(Holder.direct(effect)));
				}
			}
		}

		try {
			MEVCommonVisualProcessor.processVisual(entity.getId(), instanceList);
		} catch (Throwable t) {
			MobEffectsVFX.LOGGER.warn("MobEffectsVFX threw an exception: %s".formatted(t.fillInStackTrace()));
		}
	}
}