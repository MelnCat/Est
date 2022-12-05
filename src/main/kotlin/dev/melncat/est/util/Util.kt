package dev.melncat.est.util

import com.destroystokyo.paper.ParticleBuilder
import dev.melncat.est.plugin
import dev.melncat.est.weaponart.particleViewDistance
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.component
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.ServicesManager
import org.bukkit.scheduler.BukkitScheduler
import xyz.xenondevs.nova.material.NovaMaterialRegistry


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

fun Block.lowest()
	= if (isPassable) getRelative(DOWN) else this

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

inline fun <reified T> ServicesManager.getRegistration()
	= getRegistration(T::class.java)
infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
	require(start.isFinite())
	require(endInclusive.isFinite())
	require(step > 0.0) { "Step must be positive, was: $step." }
	val sequence = generateSequence(start) { previous ->
		if (previous == Double.POSITIVE_INFINITY) return@generateSequence null
		val next = previous + step
		if (next >= endInclusive) null else next
	}
	return sequence.asIterable()
}

fun Location.move(delta: Double = 0.1) {
	add(direction.multiply(delta))
}

infix fun String.toKey(key: String) = NamespacedKey(this, key)

fun key(key: String) = "est" toKey key

fun ParticleBuilder.spawnDefault()
	= receivers(particleViewDistance).spawn()

val ItemStack.itemKey
	get() = NovaMaterialRegistry.getOrNull(this)?.id?.toNamespacedKey() ?: type.key()