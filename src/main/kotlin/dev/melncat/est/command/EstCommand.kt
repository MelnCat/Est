package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.listener.attacksSinceMiss
import dev.melncat.est.plugin
import dev.melncat.est.util.isAir
import dev.melncat.est.util.itemFromString
import dev.melncat.est.util.loadConfig
import dev.melncat.est.util.serializeToString
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.Bukkit
import org.bukkit.block.Container
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.PluginClassLoader
import org.yaml.snakeyaml.Yaml
import xyz.xenondevs.nova.material.NovaMaterialRegistry
import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

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
				println(Thread.currentThread().contextClassLoader)
				println(this.javaClass.classLoader)
				val pl = Bukkit.getPluginManager().getPlugin("Nova")!!
				val classLoader = pl::class.memberFunctions.find { it.name == "getNova" }!!.call(pl)!!.javaClass.classLoader as URLClassLoader
				println(classLoader.loadClass("xyz.xenondevs.nova.material.NovaMaterialRegistry"))
				println(classLoader.urLs.joinToString("\n"))
				PluginClassLoader::class.memberFunctions.find { it.name == "addURL"}?.
				apply { isAccessible = true }?.call(this.javaClass.classLoader as PluginClassLoader, classLoader.urLs.last())
				Thread.currentThread().contextClassLoader = classLoader
				println(Class.forName("xyz.xenondevs.nova.material.NovaMaterialRegistry", false, classLoader))
				println(Thread.currentThread().contextClassLoader)
				println(NovaMaterialRegistry.values.map { it.id }.joinToString(", "))
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