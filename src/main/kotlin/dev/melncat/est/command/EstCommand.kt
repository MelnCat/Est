package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.listener.attacksSinceMiss
import dev.melncat.est.plugin
import dev.melncat.est.util.isAir
import dev.melncat.est.util.loadConfig
import dev.melncat.est.util.serializeToString
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.block.Container
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.yaml.snakeyaml.Yaml
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
				val serialized = (block.state as Container).inventory.contents!!.filter { !it.isAir }.map { it!!.serializeToString() }
				File(plugin.dataFolder, "classes.yml").writeText(Yaml().dump(serialized))
			}
		}
		est.registerCopy("test2") {
			permission += ".test"
			handler { ctx ->
			}
		}
		est.registerCopy("test3") {
			permission += ".test"
			handler { ctx ->
				val player = ctx.sender as? Player ?: return@handler
				player.sendMessage((attacksSinceMiss[player.uniqueId] ?: 0).toString())
			}
		}
	}
}