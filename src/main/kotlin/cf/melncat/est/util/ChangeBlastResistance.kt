package cf.melncat.est.util

import net.minecraft.core.Registry
import net.minecraft.world.level.block.state.BlockBehaviour
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

fun changeBlastResistance() {
	val block = Registry.BLOCK.first()
	@Suppress("UNCHECKED_CAST")
	val found = BlockBehaviour::class.java.declaredFields.find {
		it.type == Float::class.javaPrimitiveType && run {
			it.isAccessible = true
			it.get(block) == block.explosionResistance
		}
	} ?: throw IllegalStateException("No blast resistance field found.")
	Registry.BLOCK.forEach {
		val key = Registry.BLOCK.getKey(it).path
		val blastResistance = when {
			key.contains("bedrock") -> 100f
			key.contains("tinted_glass") -> 15f
			key.contains("glass") -> 5f
			key.contains("end_stone") -> 12f
			key.contains("deepslate") -> 9f
			else -> when (it.explosionResistance) {
				1200f, 600f, 16f -> 17f
				100f -> 8f
				6f -> 5f
				else -> null
			}
		}
		if (blastResistance !== null) {
			found.set(it, blastResistance)
			assert(it.explosionResistance == blastResistance) { "fuck" }
		}
	}
}