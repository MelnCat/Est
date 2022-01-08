package cf.melncat.est.util

import cf.melncat.est.plugin
import com.charleskorn.kaml.EmptyYamlDocumentException
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.bukkit.Material
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

private val configFile = File(plugin.dataFolder, "config.yml")
lateinit var config: Config

@Serializable
data class FireballConfig(
	val materials: Map<Material, Float> = mapOf(
		Material.MAGMA_CREAM to 2f,
		Material.HEART_OF_THE_SEA to 3f,
		Material.SCUTE to 4f,
		Material.NETHER_STAR to 7f,
		Material.DRAGON_EGG to 22f,
	),
	val customItems: Map<String, Float> = mapOf(
		"diamond_singularity" to 44f,
		"spark_singularity" to 88f,
		"soul_of_fortitude" to 155f,
	)
)

@Serializable
data class Config(
	@SerialName("custom-item-tag")
	val customItemTag: String = "est:custom_item_type",
	val fireballs: FireballConfig = FireballConfig()
)

fun loadConfig() {
	if (!configFile.exists()) {
		configFile.parentFile.mkdirs()
		configFile.createNewFile()
	}
	config = try {
		Yaml.default.decodeFromString(configFile.readText(StandardCharsets.UTF_8))
	} catch (exception: EmptyYamlDocumentException) {
		Config()
	}
	configFile.writeText(Yaml.default.encodeToString(config), StandardCharsets.UTF_8)
}