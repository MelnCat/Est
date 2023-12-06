package dev.melncat.est.listener

import dev.melncat.est.playergroup.playerGroups
import dev.melncat.est.plugin
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.config
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.component
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGHEST
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER
import xyz.xenondevs.nmsutils.network.PacketHandler
import xyz.xenondevs.nova.util.serverPlayer


@RegisterListener
object JoinListener : FListener {
	@EventHandler(priority = HIGHEST)
	fun onLogin(event: PlayerLoginEvent) {
		if (config.maintenance && !event.player.hasPermission("est.maintenance.bypass")) {
			event.disallow(KICK_OTHER, Component.text("The server is currently in maintenance."))
		}
		if (PlainTextComponentSerializer.plainText().serialize(event.kickMessage()).contains("Something went wrong")) {
			event.allow()
			Bukkit.getScheduler().runTaskLater(plugin, { ->
				val channel = event.player.serverPlayer.connection.connection.channel
				channel.pipeline()
					.addBefore("packet_handler", "Nova_packet_handler", PacketHandler(channel, event.player))
			}, 3L)
		}
	}

	@EventHandler
	fun onJoin(event: PlayerJoinEvent) {
		if (!event.player.persistentDataContainer.has(EstKey.hasJoined)) {
			//event.player.persistentDataContainer.set(EstKey.hasJoined, true)
			// TODO - starter book
		}
		val invited = playerGroups.filter { it.value.isInvited(event.player.uniqueId) }
		if (invited.isNotEmpty()) {
			event.player.sendMessage("<green>You have invites to join the group${
				if (invited.size == 1) "" else "s"
			} <0>. Run <white>/pg join <group></yellow> to join.".mm(
				Component.join(JoinConfiguration.commas(true), invited.map { it.key.component(NTC.YELLOW) }.toList())
			))
		}
	}
}