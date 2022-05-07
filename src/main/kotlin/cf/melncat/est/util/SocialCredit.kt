package cf.melncat.est.util

import cf.melncat.est.plugin
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.tukaani.xz.XZInputStream
import java.io.File
import java.io.FileInputStream
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZ
import org.tukaani.xz.XZOutputStream

private val file = File(plugin.dataFolder, "socialCredit.json.xz")


val socialCredit = mutableMapOf<UUID, Int>()
private val options = LZMA2Options()

object UUIDSerializer : KSerializer<UUID> {
	override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())

	override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

private val serializer = MapSerializer(UUIDSerializer, Int.serializer())

suspend fun loadSocialCredit() {
	val data = withContext(Dispatchers.IO) {
		if (!file.exists()) {
			file.parentFile.mkdirs()
			file.createNewFile()
		}
		if (file.length() == 0L) return@withContext null
		XZInputStream(file.inputStream()).readBytes().decodeToString()
	} ?: return
	if (data.isEmpty()) return
	val loaded = Json.decodeFromString(serializer, data)
	socialCredit.putAll(loaded)
}

suspend fun saveSocialCredit() {
	withContext(Dispatchers.IO) {
		XZOutputStream(file.outputStream(), options).use {
			it.write(Json.encodeToString(serializer, socialCredit).encodeToByteArray())
		}
	}
}