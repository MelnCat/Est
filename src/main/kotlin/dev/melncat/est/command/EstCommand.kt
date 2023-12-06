package dev.melncat.est.command

import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.listener.CustomItems
import dev.melncat.est.listener.attacksSinceMiss
import dev.melncat.est.listener.translatedUsers
import dev.melncat.est.listener.uwuEnabled
import dev.melncat.est.listener.uwuEnabledUsers
import dev.melncat.est.playergroup.playerGroups
import dev.melncat.est.plugin
import dev.melncat.est.util.isAir
import dev.melncat.est.util.loadConfig
import dev.melncat.est.util.serializeToString
import dev.melncat.est.weaponart.WeaponArtRegistry
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.OfflinePlayer
import org.bukkit.block.Container
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.yaml.snakeyaml.Yaml
import xyz.xenondevs.nova.util.addToInventoryOrDrop
import java.io.File

@RegisterCommand
object EstCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		val est = manager.buildAndRegister("est") {
			permission = "est.command.est"
			handler { ctx ->
				ctx.sender.sendMessage("<b><aqua>EST".mm())
			}
		}
		est.registerCopy("reload") {
			permission += ".reload"
			handler { ctx ->
				loadConfig()
				ctx.sender.sendMessage("<green>Plugin reloaded.".mm())
			}
		}
		est.registerCopy("test") {
			permission += ".test"
			senderType<Player>()
			handler { ctx ->
				val player = ctx.sender as Player
				val block = player.getTargetBlock(5)!!
				val serialized = (block.state as Container).inventory.contents!!.filter { !it.isAir }
					.map { it!!.serializeToString() }
				File(plugin.dataFolder, "classes.yml").writeText(Yaml().dump(serialized))
			}
		}
		est.registerCopy("test2") {
			permission += ".test"
			argument(StringArgument.of("id"))
			handler { ctx ->
				val player = ctx.sender as? Player ?: return@handler
				player.addToInventoryOrDrop(
					listOf(
						CustomItems.createItem(ctx.get("id"))
					)
				)
			}
		}
		est.registerCopy("uwu") {
			permission += ".test"
			argument(OfflinePlayerArgument.optional("player"))
			handler { ctx ->
				val player = ctx.sender as? Player ?: return@handler
				val target = ctx.getOptional<OfflinePlayer>("player")
				if (target.isEmpty) {
					uwuEnabled = !uwuEnabled
					player.sendMessage("is now $uwuEnabled")
				} else {
					val t = target.get().uniqueId
					if (uwuEnabledUsers.contains(t)) uwuEnabledUsers.remove(t)
					else uwuEnabledUsers.add(t)
					player.sendMessage("is now ${uwuEnabledUsers.contains(t)}")
				}
			}
		}
		est.registerCopy("translate") {
			permission += ".test"
			argument(OfflinePlayerArgument.of("player"))
			argument(StringArgument.optional("lang"))
			handler { ctx ->
				val player = ctx.sender as? Player ?: return@handler
				val target = ctx.get<OfflinePlayer>("player")
				val lang = ctx.getOptional<String>("lang")
				if (lang.isEmpty) {
					player.sendMessage("${translatedUsers.remove(target.uniqueId)} removed")
				} else {
					translatedUsers[target.uniqueId] = lang.get()
					player.sendMessage("$lang added")
				}
			}
		}
		est.registerCopy("test3") {
			permission += ".test"
			handler { ctx ->
				val player = ctx.sender as? Player ?: return@handler
				player.sendMessage(playerGroups.keys.joinToString(", "))
			}
		}
	}
}