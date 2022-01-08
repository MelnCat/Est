package cf.melncat.est.command

import cf.melncat.est.util.ARMOR_EFFECT_KEY
import cf.melncat.est.util.PDC
import cf.melncat.est.util.TD
import cf.melncat.est.util.get
import cf.melncat.est.util.has
import cf.melncat.est.util.meta
import cf.melncat.est.util.pd
import cf.melncat.est.util.set
import cf.melncat.est.util.usageError
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.Locale

@RegisterCommand
object ItemEffectCommand : BaseCommand(
	"ief"
) {
	private val types = PotionEffectType.values().map { it.name }

	@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
	override fun execute(sender: CommandSender, label: String, _args: Array<out String>): Boolean {
		val args = _args.asList().toMutableList()
		val silent = args.isNotEmpty() && args.first() == "-s"
		if (silent) args.removeAt(0)
		if (args.isEmpty()) return sender.usageError("You must specify an effect.")
		val type = PotionEffectType.getByName(args[0]) ?: return sender.usageError("No effect found.")
		val amplifier = Integer.parseInt(args.getOrElse(1) { "1" }) - 1
		val time = Integer.parseInt(args.getOrElse(2) { "10" })
		if (sender !is Player) return sender.usageError("You must be a player.")
		val item = sender.inventory.itemInMainHand
		if (item.amount <= 0) return sender.usageError("You must be holding an item.")
		item.meta<ItemMeta> {
			if (!pd.has<Array<PotionEffect>>(ARMOR_EFFECT_KEY)) pd.set<Array<PotionEffect>>(ARMOR_EFFECT_KEY, arrayOf())
			val effects = pd.get<Array<PotionEffect>>(ARMOR_EFFECT_KEY)!!
				.filter { it.type !== type }.toTypedArray()
			if (amplifier == -1) pd.set(ARMOR_EFFECT_KEY, effects)
			else pd.set(
				ARMOR_EFFECT_KEY,
				effects + PotionEffect(type, time, amplifier, true)
			)
		}
		return true
	}

	override fun tabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> {
		val arguments = args.filter { it != "-s" }
		return when {
			args.size <= 1 -> listOf("-s") + types
			arguments.size <= 1 -> types
			else -> listOf()
		}
	}
}