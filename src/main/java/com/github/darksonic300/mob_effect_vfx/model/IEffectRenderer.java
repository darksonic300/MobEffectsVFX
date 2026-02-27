package com.github.darksonic300.mob_effect_vfx.model;

import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface IEffectRenderer {
	void initRender(MultiBufferSource.BufferSource bufferSource, RenderLevelStageEvent event, LivingEntity source,
			float progress, MobEffectCategory effectCategory, MEVColor color);
}
