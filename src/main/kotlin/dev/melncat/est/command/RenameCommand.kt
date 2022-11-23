package dev.melncat.est.command

import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.kotlin.extension.commandBuilder
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.util.isAir
import dev.melncat.est.util.meta
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.DyeColor
import org.bukkit.Material.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@RegisterCommand
object RenameCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("rename") {
			permission = "est.command.rename"
			argument(StringArgument.greedy("message"))
			senderType<Player>()
			handler { ctx ->
				val player = ctx.sender as Player
				player.inventory.itemInMainHand.editMeta { it.displayName("<!i>${ctx.get<String>("message")}".mm()) }
			}
		}
		val lore = manager.commandBuilder("lore") {
			permission = "est.command.lore"
			senderType<Player>()
			handler { ctx ->
				val player = ctx.sender as Player
				player.sendMessage("<red>Syntax: <white>/lore <add/insert/remove/clear>")
			}
		}
		lore.registerCopy("add") {
			argument(StringArgument.greedy("lore"))
			handler { ctx ->
				val player = ctx.sender as Player
				player.inventory.itemInMainHand.editMeta {
					val lore = if (it.hasLore()) it.lore()!! else listOf()
					it.lore(lore + "<!i><dark_gray>${ctx.get<String>("lore")}".mm())
				}
			}
		}
		lore.registerCopy("insert") {
			argument(IntegerArgument.of("position"))
			argument(StringArgument.greedy("lore"))
			handler { ctx ->
				val player = ctx.sender as Player
				player.inventory.itemInMainHand.editMeta {
					val lore = (if (it.hasLore()) it.lore()!! else listOf()).toMutableList()
					lore.add(ctx.get<Int>("position"), "<!i><dark_gray>${ctx.get<String>("lore")}".mm())
					it.lore(lore)
				}
			}
		}
		lore.registerCopy("remove") {
			argument(IntegerArgument.of("position"))
			handler { ctx ->
				val player = ctx.sender as Player
				player.inventory.itemInMainHand.editMeta {
					val lore = (if (it.hasLore()) it.lore()!! else listOf()).toMutableList()
					lore.removeAt(ctx.get<Int>("position"))
					it.lore(lore)
				}
			}
		}
		lore.registerCopy("set") {
			argument(IntegerArgument.of("position"))
			argument(StringArgument.greedy("lore"))
			handler { ctx ->
				val player = ctx.sender as Player
				player.inventory.itemInMainHand.editMeta {
					val lore = (if (it.hasLore()) it.lore()!! else listOf()).toMutableList()
					lore[ctx.get<Int>("position")] = "<!i><dark_gray>${ctx.get<String>("lore")}".mm()
					it.lore(lore)
				}
			}
		}
		lore.registerCopy("clear") {
			handler { ctx ->
				val player = ctx.sender as Player
				player.inventory.itemInMainHand.editMeta {
					it.lore(null)
				}
			}
		}
		manager.buildAndRegister("bundlecolor") {
			permission = "est.command.bundlecolor"
			argument(EnumArgument.of(DyeColor::class.java, "color"))
			senderType<Player>()
			handler { ctx ->
				val player = ctx.sender as Player
				val item = player.inventory.itemInMainHand
				if (item.isAir || item.type != BUNDLE) return@handler
				item.editMeta {
					it.setCustomModelData(
						when (ctx.get<DyeColor>("color")) {
							DyeColor.BLACK -> 1101
							DyeColor.BLUE -> 1102
							DyeColor.BROWN -> 1103
							DyeColor.CYAN -> 1104
							DyeColor.GRAY -> 1105
							DyeColor.GREEN -> 1106
							DyeColor.LIGHT_BLUE -> 1107
							DyeColor.LIGHT_GRAY -> 1108
							DyeColor.LIME -> 1109
							DyeColor.MAGENTA -> 1110
							DyeColor.ORANGE -> 1111
							DyeColor.PINK -> 1112
							DyeColor.PURPLE -> 1113
							DyeColor.RED -> 1114
							DyeColor.WHITE -> 1115
							DyeColor.YELLOW -> 1116
						}
					)
				}
			}
		}
	}
}