package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.util.socialCredit
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@RegisterCommand
object SocialCreditCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("socialcredit") {
			permission = "est.command.socialcredit"
			senderType<Player>()
			handler { ctx ->
				val player = ctx.sender as Player
				player.sendMessage(
					"<yellow>You have <green><0></green> social credit.".mm(
						socialCredit.getOrDefault(
							player.uniqueId,
							0
						)
					)
				)
			}
		}
	}
}