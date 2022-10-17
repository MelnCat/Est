package dev.melncat.est.util

import net.minecraft.core.Registry

fun changeBlastResistance() {
	Registry.BLOCK.forEach {
		val key = Registry.BLOCK.getKey(it).path
		val blastResistance = when {
			key.contains("bedrock") -> 100f
			key.contains("tinted_glass") -> 5f
			key.contains("quartz") -> 6f
			key.contains("brick") -> 6f
			key.contains("glass") -> 5f
			key.contains("end_stone") -> 12f
			key.contains("deepslate") -> 7f
			else -> when (it.explosionResistance) {
				1200f, 600f, 16f -> 17f
				100f -> 8f
				6f -> 5f
				else -> null
			}
		}
		if (blastResistance !== null) it.explosionResistance = blastResistance
	}
}