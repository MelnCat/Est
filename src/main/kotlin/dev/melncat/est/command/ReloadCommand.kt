package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.util.loadConfig
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.command.CommandSender

@RegisterCommand
object ReloadCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("reload") {
			permission = "est.command.reload"
			handler { ctx ->
				loadConfig()
				ctx.sender.sendMessage("<green>Plugin reloaded.".mm())
			}
		}
	}
}