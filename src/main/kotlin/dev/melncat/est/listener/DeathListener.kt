package dev.melncat.est.listener

import dev.melncat.est.util.getTotalExperience
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent


@RegisterListener
object DeathListener : FListener {
	@EventHandler
	fun on(event: PlayerDeathEvent) {
		event.droppedExp = getTotalExperience(event.player)
		if (event.player.killer == null || event.player.killer == event.player) {
			event.keepInventory = true
			event.drops.clear()
		}
	}
}