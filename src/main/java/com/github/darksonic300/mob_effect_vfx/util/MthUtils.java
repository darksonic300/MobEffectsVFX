package com.github.darksonic300.mob_effect_vfx.util;

import net.minecraft.util.RandomSource;

public class MthUtils {
	public static float fRand(RandomSource random, float min, float max) {
		return min + (max - min) * random.nextFloat();
		// return ThreadLocalRandom.current().nextFloat(min, max);
	}
}
