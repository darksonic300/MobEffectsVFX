package com.github.darksonic300.mobeffectvfx.util;

import net.minecraft.util.RandomSource;

public record MthUtils() {
	public static float fRand(RandomSource random, float min, float max) {
		return min + (max - min) * random.nextFloat();
		// return ThreadLocalRandom.current().nextFloat(min, max);
	}
}
