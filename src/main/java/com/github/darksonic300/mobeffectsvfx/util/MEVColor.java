package com.github.darksonic300.mobeffectsvfx.util;

import com.github.darksonic300.mobeffectsvfx.MEVConfig;
import net.minecraft.world.effect.MobEffect;

public record MEVColor(float r, float g, float b, float a) {
	public static MEVColor getEffectColor(MobEffect effect) {
		int color = effect.getColor();
		float r = ((color >> 16) & 0xFF) / 255.0F;
		float g = ((color >> 8) & 0xFF) / 255.0F;
		float b = (color & 0xFF) / 255.0F;

		float a = MEVConfig.COMMON.opacity.get().floatValue();

		return new MEVColor(r, g, b, a);
	}
}
