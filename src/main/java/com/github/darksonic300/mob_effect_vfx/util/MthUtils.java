package com.github.darksonic300.mob_effect_vfx.util;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.RandomSource;

public class MthUtils {
	public static float fRand(float min, float max) {
		return min + (max - min) * ThreadLocalRandom.current().nextFloat();
	}
}
