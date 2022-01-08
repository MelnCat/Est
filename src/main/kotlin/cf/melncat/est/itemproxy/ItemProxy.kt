package cf.melncat.est.itemproxy

import cf.melncat.est.plugin
import cf.melncat.est.util.compose
import cf.melncat.est.util.isAir
import cf.melncat.est.util.reflections
import com.comphenix.protocol.PacketType.Play.Client
import com.comphenix.protocol.PacketType.Play.Server
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority.NORMAL
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import de.tr7zw.nbtapi.NBTItem
import net.minecraft.world.entity.EntityType
import org.bukkit.Material
import org.bukkit.entity.EntityType.DROPPED_ITEM
import org.bukkit.entity.EntityType.GLOW_ITEM_FRAME
import org.bukkit.entity.EntityType.ITEM_FRAME
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.reflections.scanners.Scanners.TypesAnnotated

annotation class RegisterItemProxy

typealias ItemProxy = (ItemStack) -> ItemStack

private const val itemProxyOriginal = "__EST_ITEM_PROXY_ORIGINAL"
private var itemProxy: ItemProxy = { it }

private val proxy: ItemProxy = { item ->
	if (item.isAir) item
	else itemProxy(item.clone()).let {
		if (it == item) it
		else NBTItem(it).apply { setItemStack(itemProxyOriginal, item) }.item
	}
}


fun addItemProxy(proxy: ItemProxy) {
	itemProxy = compose(proxy, itemProxy)
}

fun setupItemProxy() {
	val manager = ProtocolLibrary.getProtocolManager()
	manager.addPacketListener(object : PacketAdapter(plugin, NORMAL, Server.SET_SLOT) {
		override fun onPacketSending(event: PacketEvent) {
			event.packet.itemModifier.modify(0, proxy)
		}
	})
	manager.addPacketListener(object : PacketAdapter(plugin, NORMAL, Server.WINDOW_ITEMS) {
		override fun onPacketSending(event: PacketEvent) {
			event.packet.itemListModifier.modify(0) { it.map(proxy) }
			event.packet.itemModifier.modify(0, proxy)
		}
	})
	manager.addPacketListener(object : PacketAdapter(plugin, NORMAL, Server.ENTITY_METADATA) {
		override fun onPacketSending(event: PacketEvent) {
			val entity = event.packet.getEntityModifier(event).readSafely(0) ?: return
			when (entity.type) {
				DROPPED_ITEM, ITEM_FRAME, GLOW_ITEM_FRAME -> {
					val obj = event.packet.watchableCollectionModifier.read(0).find { it.index == 8 } ?: return
					val item = obj.value as? ItemStack ?: return
					obj.value = proxy(item)
				}
				else -> return
			}
		}
	})
	manager.addPacketListener(object : PacketAdapter(plugin, NORMAL, Client.SET_CREATIVE_SLOT) {
		override fun onPacketReceiving(event: PacketEvent) {
			event.packet.itemModifier.modify(0) { item ->
				if (item.isAir) item
				else NBTItem(item).let { if (it.hasKey(itemProxyOriginal)) it.getItemStack(itemProxyOriginal) else item }
			}
		}
	})
	reflections.get(
		TypesAnnotated.with(RegisterItemProxy::class.java).asClass<Any>()
	).also { println(it) }
		.forEach {
			@Suppress("UNCHECKED_CAST")
			addItemProxy(it.kotlin.objectInstance as ItemProxy)
		}
}