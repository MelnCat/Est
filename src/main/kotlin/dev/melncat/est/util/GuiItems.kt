package dev.melncat.est.util

import de.studiocode.invui.gui.impl.PagedGUI
import de.studiocode.invui.gui.impl.ScrollGUI
import de.studiocode.invui.item.ItemProvider
import de.studiocode.invui.item.ItemWrapper
import de.studiocode.invui.item.impl.BaseItem
import de.studiocode.invui.item.impl.CommandItem
import de.studiocode.invui.item.impl.SimpleItem
import de.studiocode.invui.item.impl.controlitem.PageItem
import de.studiocode.invui.item.impl.controlitem.ScrollItem
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.nova.material.CoreGUIMaterial

object GuiItems {
	private val blank = ItemWrapper(ItemStack(BLACK_STAINED_GLASS_PANE).withName(Component.empty()))
	class ScrollUp : ScrollItem(-1) {
		override fun getItemProvider(gui: ScrollGUI): ItemProvider =
			if (gui.canScroll(-1)) ItemWrapper(ItemStack(YELLOW_STAINED_GLASS_PANE).also {
				it.editMeta { m ->
					m.displayName("<!i><yellow>Scroll Up".mm())
				}
			}) else blank
	}
	class ScrollDown : ScrollItem(1) {
		override fun getItemProvider(gui: ScrollGUI): ItemProvider =
			if (gui.canScroll(1)) ItemWrapper(ItemStack(YELLOW_STAINED_GLASS_PANE).also {
				it.editMeta { m ->
					m.displayName("<!i><yellow>Scroll Down".mm())
				}
			}) else blank
	}
	class ScrollLeft : ScrollItem(-1) {
		override fun getItemProvider(gui: ScrollGUI): ItemProvider =
			if (gui.canScroll(-1)) ItemWrapper(ItemStack(YELLOW_STAINED_GLASS_PANE).also {
				it.editMeta { m ->
					m.displayName("<!i><yellow>Scroll Left".mm())
				}
			}) else blank
	}
	class ScrollRight : ScrollItem(1) {
		override fun getItemProvider(gui: ScrollGUI): ItemProvider =
			if (gui.canScroll(1)) ItemWrapper(ItemStack(YELLOW_STAINED_GLASS_PANE).also {
				it.editMeta { m ->
					m.displayName("<!i><yellow>Scroll Right".mm())
				}
			}) else blank
	}
	class PreviousPage : PageItem(false) {
		override fun getItemProvider(gui: PagedGUI): ItemProvider =
			if (gui.hasPageBefore()) ItemWrapper(ItemStack(YELLOW_STAINED_GLASS_PANE).also {
				it.editMeta { m ->
					m.displayName("<!i><yellow>Previous Page".mm())
				}
			}) else blank
	}
	class NextPage : PageItem(true) {
		override fun getItemProvider(gui: PagedGUI): ItemProvider =
			if (gui.hasNextPage()) ItemWrapper(ItemStack(YELLOW_STAINED_GLASS_PANE).also {
				it.editMeta { m ->
					m.displayName("<!i><yellow>Next Page".mm())
				}
			}) else blank
	}
	val Blank = SimpleItem(blank)
	class CloseCommandItem(itemProvider: ItemProvider, command: String) : CommandItem(itemProvider, command) {
		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			player.closeInventory()
			super.handleClick(clickType, player, event)
		}
	}
	class Back(val cb: (clickType: ClickType, player: Player, event: InventoryClickEvent) -> Unit) : BaseItem() {
		constructor(command: String) : this({ _, p, _ ->
			p.performCommand(command)
		})
		override fun getItemProvider() = ItemWrapper(ItemStack(RED_STAINED_GLASS_PANE).withName("<!i><yellow>Back".mm()))

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			cb(clickType, player, event)
		}
	}
}