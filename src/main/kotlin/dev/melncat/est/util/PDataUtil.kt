package dev.melncat.est.util

import dev.melncat.est.plugin
import dev.melncat.est.util.CustomAttributeOperation.Add
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

typealias PDC = PersistentDataContainer

inline val PersistentDataHolder.pd: PersistentDataContainer
	get() = persistentDataContainer

@Suppress("UNCHECKED_CAST")
fun <T : Any> matchPDataType(type: KClass<T>) = when (type) {
	Byte::class -> PersistentDataType.BYTE
	Array<Byte>::class, ByteArray::class -> PersistentDataType.BYTE_ARRAY
	Double::class -> PersistentDataType.DOUBLE
	Float::class -> PersistentDataType.FLOAT
	Int::class -> PersistentDataType.INTEGER
	Array<Int>::class, IntArray::class -> PersistentDataType.INTEGER_ARRAY
	Long::class -> PersistentDataType.LONG
	Array<Long>::class, LongArray::class -> PersistentDataType.LONG_ARRAY
	Short::class -> PersistentDataType.SHORT
	String::class -> PersistentDataType.STRING
	PDC::class -> PersistentDataType.TAG_CONTAINER
	Array<PDC>::class -> PersistentDataType.TAG_CONTAINER_ARRAY
	Boolean::class -> BooleanPDType
	PotionEffect::class -> PotionEffectPDType
	Array<String>::class -> Utf8ArrayPDType
	Array<PotionEffect>::class -> containerArrayPDType(PotionEffectPDType)
	CustomAttributeModifier::class -> CustomAttributeModifierPDType
	Array<CustomAttributeModifier>::class -> containerArrayPDType(CustomAttributeModifierPDType)
	else -> throw IllegalArgumentException("Invalid persistent data type ${type.qualifiedName} / ${type.java.name}")
} as PersistentDataType<T, T>

fun <Z> PersistentDataContainer.get(key: String, type: PersistentDataType<*, Z>): Z? =
	get(
		NamespacedKey(plugin, key),
		type
	)

inline fun <reified T : Any> PersistentDataContainer.get(key: String): T? =
	get(key, matchPDataType(T::class))

inline fun <reified T : Any> PersistentDataContainer.get(key: NamespacedKey): T? =
	get(key, matchPDataType(T::class))

fun <Z> PersistentDataContainer.has(key: String, type: PersistentDataType<*, Z>): Boolean =
	has(
		NamespacedKey(plugin, key),
		type
	)

inline fun <reified T : Any> PersistentDataContainer.has(key: String): Boolean =
	has(key, matchPDataType(T::class))

inline fun <reified T : Any> PersistentDataContainer.has(key: NamespacedKey): Boolean =
	has(key, matchPDataType(T::class))

inline fun <reified T : Any> PersistentDataContainer.set(key: String, value: T) =
	set(key, matchPDataType(T::class), value)

inline fun <reified T : Any> PersistentDataContainer.set(key: NamespacedKey, value: T) =
	set(key, matchPDataType(T::class), value)

fun <Z> PersistentDataContainer.set(key: String, type: PersistentDataType<*, Z>, value: Z) =
	apply {
		set(
			NamespacedKey(plugin, key),
			type, value!!
		)
	}

private class StringArrayPDType(private val charset: Charset) : PersistentDataType<ByteArray, Array<String>> {
	override fun getPrimitiveType() = ByteArray::class.java

	override fun getComplexType() = Array<String>::class.java

	override fun toPrimitive(strings: Array<String>, ctx: PersistentDataAdapterContext): ByteArray {
		val allStringBytes = arrayOfNulls<ByteArray>(strings.size)
		var total = 0
		for (i in allStringBytes.indices) {
			val bytes = strings[i].toByteArray(charset)
			allStringBytes[i] = bytes
			total += bytes.size
		}
		val buffer = ByteBuffer.allocate(total + allStringBytes.size * 4)
		for (bytes in allStringBytes) {
			buffer.putInt(bytes!!.size)
			buffer.put(bytes)
		}
		return buffer.array()
	}

	override fun fromPrimitive(bytes: ByteArray, ctx: PersistentDataAdapterContext): Array<String> {
		val buffer: ByteBuffer = ByteBuffer.wrap(bytes)
		val list = ArrayList<String>()
		while (buffer.remaining() > 0) {
			if (buffer.remaining() < 4) break
			val stringLength = buffer.int
			if (buffer.remaining() < stringLength) break
			val stringBytes = ByteArray(stringLength)
			buffer.get(stringBytes)
			list.add(String(stringBytes, charset))
		}
		return list.toTypedArray()
	}
}

private object PotionEffectPDType : PersistentDataType<PDC, PotionEffect> {
	override fun getPrimitiveType() = PDC::class.java

	override fun getComplexType() = PotionEffect::class.java

	override fun toPrimitive(effect: PotionEffect, ctx: PersistentDataAdapterContext): PDC {
		return ctx.newPersistentDataContainer()
			.apply {
				set("name", effect.type.name)
				set("duration", effect.duration)
				set("amplifier", effect.amplifier)
				set("ambient", effect.isAmbient)
				set("particles", effect.hasParticles())
				set("icon", effect.hasIcon())
			}
	}

	override fun fromPrimitive(container: PDC, ctx: PersistentDataAdapterContext): PotionEffect {
		return PotionEffect(
			PotionEffectType.getByName(container.get("name")!!)!!,
			container.get("duration") ?: 10,
			container.get("amplifier") ?: 1,
			container.get("ambient") ?: true,
			container.get("particles") ?: true,
			container.get("icon") ?: container.get("particles") ?: true,
		)
	}
}

private object CustomAttributeModifierPDType : PersistentDataType<PDC, CustomAttributeModifier> {
	override fun getPrimitiveType() = PDC::class.java

	override fun getComplexType() = CustomAttributeModifier::class.java

	override fun toPrimitive(modifier: CustomAttributeModifier, ctx: PersistentDataAdapterContext): PDC {
		return ctx.newPersistentDataContainer()
			.apply {
				set("type", modifier.type.id)
				set("operation", modifier.operation.name)
				set("amount", modifier.amount)
			}
	}

	override fun fromPrimitive(container: PDC, ctx: PersistentDataAdapterContext): CustomAttributeModifier {
		return CustomAttributeModifier(
			CustomAttributeRegistry[container.get("type")!!]!!,
			CustomAttributeOperation.valueOf(container.get("operation")!!),
			container.get("amount")!!
		)
	}
}

private object BooleanPDType : PersistentDataType<Byte, Boolean> {
	override fun getPrimitiveType() = Byte::class.javaObjectType

	override fun getComplexType() = Boolean::class.javaObjectType

	override fun toPrimitive(bool: Boolean, ctx: PersistentDataAdapterContext): Byte
		= if (bool) 1 else 0

	override fun fromPrimitive(byte: Byte, ctx: PersistentDataAdapterContext): Boolean
		= byte != (0).toByte()
}

private val containerArrayPDTypeCache =
	mutableMapOf<KClass<*>, PersistentDataType<Array<PDC>, Array<*>>>()

@Suppress("UNCHECKED_CAST")
private inline fun <reified T : Any> containerArrayPDType(type: PersistentDataType<PDC, T>):
		PersistentDataType<Array<PDC>, Array<T>> =
	if (containerArrayPDTypeCache.containsKey(T::class)) containerArrayPDTypeCache[T::class] as PersistentDataType<Array<PDC>, Array<T>>
	else object : PersistentDataType<Array<PDC>, Array<T>> {
		override fun getPrimitiveType() = Array<PDC>::class.java
		override fun getComplexType() = Array<T>::class.java

		override fun toPrimitive(complex: Array<T>, ctx: PersistentDataAdapterContext) =
			complex.map { type.toPrimitive(it, ctx) }.toTypedArray()

		override fun fromPrimitive(primitive: Array<PDC>, ctx: PersistentDataAdapterContext) =
			primitive.map { type.fromPrimitive(it, ctx) }.toTypedArray()

	}.also { containerArrayPDTypeCache[T::class] = it as PersistentDataType<Array<PDC>, Array<*>> }.also { println("pd ${T::class}") }

private val Utf8ArrayPDType = StringArrayPDType(StandardCharsets.UTF_8)

abstract class PDCSerializable(protected val tag: PersistentDataContainer) {
	protected open class OptionalPDataDelegate<T>(private val key: String, private val type: PersistentDataType<*, T>) {
		open operator fun getValue(thisRef: PDCSerializable, property: KProperty<*>): T? {
			return thisRef.tag.get(key, type)
		}

		operator fun setValue(thisRef: PDCSerializable, property: KProperty<*>, value: T) {
			thisRef.tag.set(key, type, value)
		}
	}
	protected class PDataDelegate<T>(private val key: String, private val type: PersistentDataType<*, T>, private val default: T)
		: OptionalPDataDelegate<T>(key, type) {

		override operator fun getValue(thisRef: PDCSerializable, property: KProperty<*>): T {
			return thisRef.tag.get(key, type) ?: default
		}
	}

	protected inline fun <reified T: Any> pd(key: String)
			= OptionalPDataDelegate(key, matchPDataType(T::class))

	protected inline fun <reified T: Any> pd(key: String, default: T)
			= PDataDelegate(key, matchPDataType(T::class), default)
}

inline operator fun <reified T : Any> PersistentDataContainer.getValue(thisRef: Any, property: KProperty<*>): T {
	return get(property.name, matchPDataType(T::class)) as T
}

inline operator fun <reified T : Any> PersistentDataContainer.setValue(thisRef: Any, property: KProperty<*>, newValue: T) {
	set(property.name, matchPDataType(T::class), newValue)
}