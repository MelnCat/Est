package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@RegisterCommand
object BackCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("back") {
			permission = "est.command.back"
			senderType<Player>()
			handler { ctx ->
				val player = ctx.sender as Player
				if (player.lastDeathLocation != null) player.teleport(player.lastDeathLocation!!)
			}
		}
	}
}