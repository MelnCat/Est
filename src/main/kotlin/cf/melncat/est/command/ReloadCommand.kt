package cf.melncat.est.command

import cf.melncat.est.plugin
import cf.melncat.est.util.NTC
import cf.melncat.est.util.TD
import cf.melncat.est.util.div
import cf.melncat.est.util.giveItems
import cf.melncat.est.util.loadConfig
import cf.melncat.est.util.meta
import cf.melncat.est.util.usageError
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.map.MinecraftFont
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.math.ceil
import kotlin.math.floor

@RegisterCommand
object ReloadCommand : BaseCommand(
	"reload"
) {

	override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
		loadConfig()
		sender.sendMessage("Plugin reloaded." / NTC.GREEN)
		return true
	}
}