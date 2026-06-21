package com.github.darksonic300.mobeffectsvfx.util;

import java.util.concurrent.ThreadLocalRandom;

public record MthUtils() {
	public static float nextFloat(float min, float max) {
		return min + (max - min) * ThreadLocalRandom.current().nextFloat();
	}
}
