package cf.melncat.est.command

import cf.melncat.est.util.NTC
import cf.melncat.est.util.component
import cf.melncat.est.util.div
import cf.melncat.est.util.loadConfig
import cf.melncat.est.util.mm
import cf.melncat.est.util.plus
import cf.melncat.est.util.socialCredit
import cf.melncat.est.util.usageError
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@RegisterCommand
object SocialCreditCommand : BaseCommand(
	"socialcredit"
) {

	override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
		if (sender !is Player) return sender.usageError("You must be a player to use this command.")
		sender.sendMessage(
			"<yellow>You have <green><0></green> social credit.".mm(
				socialCredit.getOrDefault(
					sender.uniqueId,
					0
				).toString()
			)
		)
		return true
	}
}