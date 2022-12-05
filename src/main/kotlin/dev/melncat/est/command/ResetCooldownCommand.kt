package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.weaponart.ActiveWeaponArts.resetCooldown
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@RegisterCommand
object ResetCooldownCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("resetcooldown") {
			permission = "est.command.resetcooldown"
			senderType<Player>()
			handler { ctx ->
				resetCooldown(ctx.sender as Player)
				ctx.sender.sendMessage("<green>Your weapon art cooldown has been reset.".mm())
			}
		}
	}
}