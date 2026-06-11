package com.github.darksonic300.mob_effect_vfx.util;

import java.util.concurrent.ThreadLocalRandom;

public record MthUtils() {
	public static float nextFloat(float min, float max) {
		return min + (max - min) * ThreadLocalRandom.current().nextFloat();
	}
}
