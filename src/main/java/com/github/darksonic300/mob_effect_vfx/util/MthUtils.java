package com.github.darksonic300.mob_effect_vfx.util;

import java.util.concurrent.ThreadLocalRandom;

public class MthUtils {
	public static float nextFloat(float min, float max) {
		return min + (max - min) * ThreadLocalRandom.current().nextFloat();
	}
}
