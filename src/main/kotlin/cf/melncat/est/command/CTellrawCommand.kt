package cf.melncat.est.command

import cf.melncat.est.util.defaultSelectors
import cf.melncat.est.util.matchEntityTypeOrThrow
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@RegisterCommand
object CTellrawCommand : BaseCommand(
	"ctellraw"
) {
	override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
		val players = sender.matchEntityTypeOrThrow<Player>(args[0]) ?: return false
		val component = LegacyComponentSerializer.legacyAmpersand().deserialize(args.drop(1).joinToString(" "))
		players.forEach { it.sendMessage(component) }
		return true
	}
	override fun tabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> {
		return when {
			args.size <= 1 -> defaultSelectors()
			else -> listOf()
		}
	}
}