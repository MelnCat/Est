package dev.melncat.est.command

import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.command.CommandSender

@RegisterCommand
object CTellrawCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("ctellraw") {
			permission = "est.command.ctellraw"
			argument(MultiplePlayerSelectorArgument.of("target"))
			argument(StringArgument.greedy("message"))
			handler { ctx ->
				val selected = ctx.get<MultiplePlayerSelector>("target")
				selected.players.forEach { it.sendMessage(ctx.get<String>("message").mm()) }
				ctx.sender.sendMessage("<green>Tellraw has been sent to <yellow><0></yellow> players.".mm(selected.players.size))
			}
		}
	}
}