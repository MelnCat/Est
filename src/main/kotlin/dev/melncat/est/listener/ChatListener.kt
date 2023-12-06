package dev.melncat.est.listener

import dev.melncat.est.playergroup.PlayerGroup
import dev.melncat.est.playergroup.PlayerGroup.Permission
import dev.melncat.est.playergroup.playerGroups
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.util.UUID


val playerGroupChats = mutableMapOf<UUID, String>()

@RegisterListener
object ChatListener : FListener {
	@EventHandler
	fun onCommandPreprocess(event: PlayerCommandPreprocessEvent) {
		if (event.message.contains("ie rename", true)) event.message =
			event.message.replace("ie rename", "rename", true)
	}

	@EventHandler
	fun on(event: AsyncChatEvent) {
		val chat = playerGroupChats[event.player.uniqueId] ?: return
		val group = playerGroups[chat]
		if (group == null || !group.hasPermission(event.player, Permission.Chat)) {
			playerGroupChats.remove(event.player.uniqueId)
			return
		}
		event.isCancelled = true
		group.chat(event)
	}
}