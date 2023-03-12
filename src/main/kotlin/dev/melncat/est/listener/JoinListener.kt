package dev.melncat.est.listener

import dev.melncat.est.util.EstKey
import dev.melncat.est.util.config
import dev.melncat.est.util.set
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerLoginEvent.Result.KICK_FULL
import org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER
import org.bukkit.event.player.PlayerPreLoginEvent


@RegisterListener
object JoinListener : FListener {
	@EventHandler
	fun onLogin(event: PlayerLoginEvent) {
		if (config.maintenance && !event.player.hasPermission("est.maintenance.bypass")) {
			event.disallow(KICK_OTHER, Component.text("The server is currently in maintenance."))
		}
	}
	@EventHandler
	fun onJoin(event: PlayerJoinEvent) {
		if (!event.player.persistentDataContainer.has(EstKey.hasStarterKit))
			event.player.performCommand("class")
		if (!event.player.persistentDataContainer.has(EstKey.hasJoined)) {
			//event.player.persistentDataContainer.set(EstKey.hasJoined, true)
			// TODO - starter book
		}
	}
}