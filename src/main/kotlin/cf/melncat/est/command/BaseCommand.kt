package cf.melncat.est.command

import cf.melncat.est.plugin
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender

annotation class RegisterCommand

private fun filterTab(completed: List<String>, args: Array<out String>)
	= if (args.last().isEmpty()) completed else args.filter { it.startsWith(args.last(), true) }

abstract class BaseCommand(
	val name: String,
	val desc: String = "No description specified.",
	val usage: String = "/$name",
	val permission: String = "est.command.$name",
	val aliases: List<String> = listOf()
) {
	open val filterTabEnabled = true

	abstract fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean

	open fun tabComplete(sender: CommandSender, label: String, args: Array<out String>, location: Location?) =
		tabComplete(sender, label, args)

	open fun tabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> = listOf()

	private fun asCommand() = object : Command(name, desc, usage, aliases) {
		init {
			permission = this@BaseCommand.permission
		}
		override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
			if (!testPermission(sender)) return true
			return execute(sender, label, args)
		}

		override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>)
			= this@BaseCommand.tabComplete(sender, alias, args).let { if (filterTabEnabled) filterTab(it, args) else it }

		override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>, location: Location?)
			= this@BaseCommand.tabComplete(sender, alias, args, location).let { if (filterTabEnabled) filterTab(it, args) else it }
	}

	fun register(map: CommandMap) {
		val name = plugin.name.lowercase()
		map.knownCommands.remove(name)
		map.knownCommands.remove("${plugin.name.lowercase()}:${name}")
		map.register(
			name,
			asCommand()
		)
	}

}