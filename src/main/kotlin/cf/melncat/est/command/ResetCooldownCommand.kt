package cf.melncat.est.command

import cf.melncat.est.listener.resetCooldown
import cf.melncat.est.util.mm
import cf.melncat.est.util.socialCredit
import cf.melncat.est.util.usageError
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@RegisterCommand
object ResetCooldownCommand : BaseCommand(
	"resetcooldown"
) {

	override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
		if (sender !is Player) return sender.usageError("You must be a player to use this command.")
		resetCooldown(sender)
		sender.sendMessage("<green>Your weapon art cooldown has been reset.")
		return true
	}
}