package dev.melncat.est.listener

import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent


@RegisterListener
object ChatListener : FListener {
	@EventHandler
	fun onCommandPreprocess(event: PlayerCommandPreprocessEvent) {
		if (event.message.contains("e rename ", true)) event.message =
			event.message.replace("e rename ", "e rename &f", true)
	}
}