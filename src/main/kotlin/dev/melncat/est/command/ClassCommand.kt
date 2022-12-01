package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import de.studiocode.invui.gui.GUI
import de.studiocode.invui.gui.builder.GUIBuilder
import de.studiocode.invui.gui.builder.guitype.GUIType
import de.studiocode.invui.item.ItemProvider
import de.studiocode.invui.item.ItemWrapper
import de.studiocode.invui.item.impl.BaseItem
import de.studiocode.invui.window.impl.single.SimpleWindow
import dev.melncat.est.plugin
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.giveItems
import dev.melncat.est.util.itemFromString
import dev.melncat.est.util.set
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.Material.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.random.Random


@RegisterCommand
object ClassCommand : FCommand {
	private val yaml = Yaml()
	private val classes =
		yaml.load<List<String>>(File(plugin.dataFolder, "classes.yml").readText()).map(::itemFromString)

	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("class") {
			permission = "est.command.class"
			senderType<Player>()
			handler { ctx ->
				val player = ctx.sender as Player
				if (player.persistentDataContainer.has(EstKey.hasStarterKit)) {
					player.sendMessage("<red>You have already claimed your starter kit!".mm())
					return@handler
				}
				val chosen = classes.toMutableList()
				chosen.shuffle(Random(player.uniqueId.mostSignificantBits))
				SimpleWindow(
					player,
					"Choose your class:",
					createGui(chosen.take(3))
				).show()
			}
		}
	}

	private fun createGui(classes: List<ItemStack>): GUI {
		return GUIBuilder(GUIType.NORMAL)
			.setStructure(
				"1 - - - - - - - 2",
				"| # # # # # # # |",
				"| # a # b # c # |",
				"| # # # # # # # |",
				"3 - - - - - - - 4"
			)
			.addIngredient('a', ClassItem(classes[0]))
			.addIngredient('b', ClassItem(classes[1]))
			.addIngredient('c', ClassItem(classes[2]))
			.build()
	}

	private class ClassItem(val item: ItemStack) : BaseItem() {
		override fun getItemProvider(): ItemProvider {
			return ItemWrapper(item)
		}

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			if (player.persistentDataContainer.has(EstKey.hasStarterKit)) return
			player.sendMessage("<green>You have claimed the <yellow><0></yellow> kit!".mm(item.displayName()))
			player.persistentDataContainer.set(EstKey.hasStarterKit, true);
			player.inventory.giveItems(listOf(item))
			player.closeInventory()
		}
	}
}