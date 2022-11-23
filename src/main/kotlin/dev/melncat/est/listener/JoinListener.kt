package dev.melncat.est.listener

import dev.melncat.est.util.EstKey
import dev.melncat.est.util.set
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.mm
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent


@RegisterListener
object JoinListener : FListener {
	@EventHandler
	fun onJoin(event: PlayerJoinEvent) {
		if (!event.player.persistentDataContainer.has(EstKey.hasStarterKit))
			event.player.sendMessage("<yellow>You haven't claimed a starter kit yet! Claim one by running <green><click:run_command:'/class'>/class</click></green>!".mm())
		if (!event.player.persistentDataContainer.has(EstKey.hasJoined)) {
			//event.player.persistentDataContainer.set(EstKey.hasJoined, true)
			// TODO - starter book
		}
	}
}