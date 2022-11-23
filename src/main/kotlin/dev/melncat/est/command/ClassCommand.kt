package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.Pane.Priority
import dev.melncat.est.plugin
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.giveItems
import dev.melncat.est.util.itemFromString
import dev.melncat.est.util.set
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.random.Random


@RegisterCommand
object ClassCommand : FCommand {
	private val yaml = Yaml()
	private val classes = yaml.load<List<String>>(File(plugin.dataFolder, "classes.yml").readText()).map(::itemFromString)
	private val backgroundPane = OutlinePane(0, 0, 9, 3, Priority.LOWEST).apply {
		addItem(GuiItem(ItemStack(BLACK_STAINED_GLASS_PANE)))
		setRepeat(true)
	}
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
				createGui(chosen.take(3))
					.show(player)
			}
		}
	}

	private fun createGui(classes: List<ItemStack>): ChestGui {
		val gui = ChestGui(3, "Choose your class:")
		val pane = OutlinePane(0, 1, 9, 3)
		gui.setOnGlobalClick { it.isCancelled = true }
		pane.gap = 1
		pane.align(OutlinePane.Alignment.CENTER)
		for (clazz in classes) pane.addItem(GuiItem(clazz) {
			if (it.whoClicked.persistentDataContainer.has(EstKey.hasStarterKit)) return@GuiItem
			it.whoClicked.sendMessage("<green>You have claimed the <yellow><0></yellow> kit!".mm(clazz.displayName()))
			it.whoClicked.persistentDataContainer.set(EstKey.hasStarterKit, true);
			it.whoClicked.inventory.giveItems(listOf(clazz))
			it.clickedInventory?.close()
		})
		gui.addPane(backgroundPane)
		gui.addPane(pane)
		return gui
	}
}