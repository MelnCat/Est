package dev.melncat.est.util

import io.th0rgal.oraxen.items.OraxenItems
import org.bukkit.inventory.ItemStack

val coins = listOf(
	1000, 100, 10, 1
).associateWith { OraxenItems.getItemById("coin_$it") }

fun coinsFromValue(value: Int): List<ItemStack> {
	val needed = mutableMapOf<Int, Int>()
	var remaining = value
	while (remaining > 0) {
		val largest = coins.entries.find { it.key <= remaining } ?: break
		remaining -= largest.key
		if (largest.key !in needed) needed[largest.key] = 1
		else needed[largest.key] = needed[largest.key]!! + 1
	}
	return needed.flatMap { coins[it.key]!!.buildArray(it.value).toList() }
}