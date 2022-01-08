package cf.melncat.est.util

import cf.melncat.est.plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass


fun <T : Entity> CommandSender.matchEntity(str: String) = Bukkit.getServer().selectEntities(this, str)

inline fun <reified T : Entity> CommandSender.matchEntityType(str: String) =
	Bukkit.getServer().selectEntities(this, str).filterIsInstance<T>()

inline fun <reified T : Entity> CommandSender.matchEntityTypeOrThrow(text: String): List<T>? {
	val matched = try {
		matchEntityType<T>(text)
	} catch (e: IllegalArgumentException) {
		usageError(e.localizedMessage)
		return null
	}
	if (matched.isEmpty()) {
		usageError("No player(s) were found.")
		return null
	}
	return matched
}

fun CommandSender.usageError(message: String = "Invalid arguments provided.")
	= sendMessage(message / NTC.RED).let { false }

fun CommandSender.error(message: String)
	= usageError(message).let { true }

inline fun <reified T : ItemMeta> ItemStack.meta(cb: T.() -> Unit): ItemStack {
	val m = itemMeta as T
	m.cb()
	itemMeta = m
	return this
}

fun defaultSelectors() = Bukkit.getOnlinePlayers().map { it.name } + listOf("@p", "@r", "@a", "@e", "@s")

fun PlayerInventory.giveItems(items: Array<ItemStack>) {
	addItem(*items).values.forEach { holder?.world?.dropItem(holder!!.location, it) }
}
fun PlayerInventory.giveItems(items: Collection<ItemStack>) {
	giveItems(items.toTypedArray())
}

fun <T, U, V> compose(f: (T) -> U, g: (U) -> V): (T) -> V
	= { g(f(it)) }

val ItemStack?.isAir: Boolean get() = this === null || this.type === Material.AIR