package dev.melncat.est.util

import com.jojodmo.itembridge.ItemBridge
import dev.melncat.est.plugin
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.component
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.ServicesManager
import org.bukkit.scheduler.BukkitScheduler


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
	= sendMessage(message.component(NTC.RED)).let { false }

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

fun BukkitScheduler.runTaskTimer(delay: Long, period: Long, cb: () -> Unit)
		= runTaskTimer(plugin, cb, delay, period)

fun BukkitScheduler.runTaskTimer(delay: Long, period: Long, cb: suspend () -> Unit)
		= runTaskTimer(plugin, { -> runBlocking { launch { cb() } } }, delay, period)

fun BukkitScheduler.runTaskLater(delay: Long, cb: () -> Unit)
		= runTaskLater(plugin, cb, delay)

//fun BukkitScheduler.runTaskLater(delay: Long, cb: suspend () -> Unit)
//		= runTaskLater(plugin, { -> runBlocking { launch { cb() } } }, delay)

fun customItem(str: String): ItemStack = ItemBridge.getItemStack(config.itemBridgeNamespace, str)

inline fun <reified T> ServicesManager.getRegistration()
	= getRegistration(T::class.java)
