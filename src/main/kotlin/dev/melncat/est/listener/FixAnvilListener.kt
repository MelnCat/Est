

package dev.melncat.est.listener

import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.component
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.world.inventory.AnvilMenu
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftInventoryView
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftMetaArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.LOW
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.purpurmc.purpur.event.inventory.AnvilUpdateResultEvent
import xyz.xenondevs.nova.util.item.unhandledTags

@Suppress("SENSELESS_COMPARISON")
@RegisterListener
object FixAnvilListener : FListener {
	@EventHandler
	fun onPrepareAnvil(event: PrepareAnvilEvent) {
		val menu = ((event.view as CraftInventoryView).handle as AnvilMenu)
		if (menu.itemName == null) menu.itemName = ""
	}
}