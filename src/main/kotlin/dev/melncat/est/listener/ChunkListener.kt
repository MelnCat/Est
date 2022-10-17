package dev.melncat.est.listener

import dev.melncat.est.util.config
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

@RegisterListener
object ChunkListener : FListener {
	@EventHandler
	fun onChunkLoad(event: ChunkLoadEvent) {
		if (!config.removeItemsInContainers) return
		event.chunk.tileEntities.filterIsInstance<Container>().forEach {
			it.inventory.clear()
		}
	}
}