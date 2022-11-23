package dev.melncat.est.listener

import dev.melncat.est.util.get
import dev.melncat.est.util.isAir
import dev.melncat.est.util.key
import dev.melncat.est.util.set
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.LOWEST
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.inventory.InventoryCreativeEvent
import org.bukkit.inventory.ItemStack
import org.purpurmc.purpur.event.packet.NetworkItemSerializeEvent

private val oldItemKey = key("old_item")
private val oldItemCache = mutableMapOf<NetworkItemSerializeEvent, ItemStack>()

@RegisterListener
object ItemProxyWorkaround : FListener {

	@EventHandler(priority = LOWEST)
	fun beforeProxy(event: NetworkItemSerializeEvent) {
		if (event.itemStack.isAir) return
		oldItemCache[event] = event.itemStack.clone()
	}

	@EventHandler(priority = MONITOR)
	fun afterProxy(event: NetworkItemSerializeEvent) {
		if (event.itemStack.isAir) return
		val cached = oldItemCache[event] ?: return
		if (cached != event.itemStack) {
			oldItemCache.remove(event)
			event.itemStack.editMeta {
				it.persistentDataContainer.set(oldItemKey, cached.serializeAsBytes())
			}
		}
	}
}