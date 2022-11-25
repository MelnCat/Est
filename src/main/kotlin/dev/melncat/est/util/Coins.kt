package dev.melncat.est.util

import org.bukkit.inventory.ItemStack
import xyz.xenondevs.nova.material.ItemNovaMaterial
import xyz.xenondevs.nova.material.NovaMaterialRegistry

val coins = listOf(
	1000, 100, 10, 1
).associateWith { NovaMaterialRegistry.get("est:coin_$it") }

fun coinsFromValue(value: Int): List<ItemStack> {
	val needed = mutableMapOf<Int, Int>()
	var remaining = value
	while (remaining > 0) {
		val largest = coins.entries.find { it.key <= remaining } ?: break
		remaining -= largest.key
		if (largest.key !in needed) needed[largest.key] = 1
		else needed[largest.key] = needed[largest.key]!! + 1
	}
	return needed.flatMap { getNovaItems(coins[it.key]!!, it.value) }
}

private fun getNovaItems(item: ItemNovaMaterial, count: Int): List<ItemStack> {
	var n = count;
	val list = mutableListOf<ItemStack>()
	while (n > 0) {
		if (n >= item.maxStackSize) {
			list.add(item.createItemStack(item.maxStackSize))
			n -= item.maxStackSize
		} else {
			list.add(item.createItemStack(n))
			n = 0
		}
	}
	return list
}