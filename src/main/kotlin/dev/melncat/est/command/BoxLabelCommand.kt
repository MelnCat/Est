package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.plugin
import dev.melncat.est.util.get
import dev.melncat.est.util.getValue
import dev.melncat.est.util.set
import dev.melncat.est.util.setValue
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer

@RegisterCommand
object BoxLabelCommand : FCommand {
	private val boxLabelKey = NamespacedKey(plugin, "box_labels")

	class BoxLabelContainer(val container: PersistentDataContainer) {
		var north: String by container
		var south: String by container
		var west: String by container
		var east: String by container
		var up: String by container
		var down: String by container
	}

	private fun getOrCreate(chunk: Chunk, y: Int): BoxLabelContainer {
		if (!chunk.persistentDataContainer.has(boxLabelKey))
			chunk.persistentDataContainer.set(boxLabelKey, chunk.persistentDataContainer.adapterContext.newPersistentDataContainer())
		val container = chunk.persistentDataContainer.get<PersistentDataContainer>(boxLabelKey)!!
		val key = NamespacedKey(plugin, y.toString())
		if (!container.has(key))
			container.set(key, chunk.persistentDataContainer.adapterContext.newPersistentDataContainer())
		val yContainer = container.get<PersistentDataContainer>(key)!!
		val boxLabelContainer = BoxLabelContainer(yContainer)
		boxLabelContainer.apply {
			north = "unknown"
			south = "unknown"
			west = "unknown"
			east = "unknown"
			up = "unknown"
			down = "unknown"
		}
		return boxLabelContainer

	}
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("boxlabel") {
			permission = "est.command.boxlabel"
			senderType<Player>()
			handler { ctx ->

			}
		}
	}
}