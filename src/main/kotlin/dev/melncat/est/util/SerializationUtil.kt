package dev.melncat.est.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.*
import org.bukkit.NamespacedKey

object NamespacedKeySerializer : KSerializer<NamespacedKey> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("key", PrimitiveKind.STRING)
	override fun serialize(encoder: Encoder, value: NamespacedKey) = encoder.encodeString(value.asString())
	override fun deserialize(decoder: Decoder): NamespacedKey =
		NamespacedKey.fromString(decoder.decodeString()) ?: throw IllegalArgumentException("Invalid NamespacedKey.")
}