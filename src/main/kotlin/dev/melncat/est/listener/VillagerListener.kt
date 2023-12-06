package dev.melncat.est.listener

import com.destroystokyo.paper.MaterialTags
import dev.melncat.est.util.config
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import net.minecraft.world.inventory.AnvilMenu
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.VillagerAcquireTradeEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.Random.Default
import kotlin.random.asJavaRandom

@RegisterListener
object VillagerListener : FListener {
	@EventHandler
	fun onAcquireTrade(event: VillagerAcquireTradeEvent) {
		if (event.recipe.result.type == ENCHANTED_BOOK) {
			val copy = event.recipe.ingredients
			copy.find { it.type == BOOK }!!.type = WRITABLE_BOOK
			copy.find { it.type == EMERALD }!!.apply {
				type = DIAMOND
				amount = min(amount + 2, 64)
			}
			event.recipe.ingredients = copy
		}
		if (MaterialTags.DIAMOND_TOOLS.isTagged(event.recipe.result)
			|| event.recipe.result.type == DIAMOND_HELMET
			|| event.recipe.result.type == DIAMOND_CHESTPLATE
			|| event.recipe.result.type == DIAMOND_LEGGINGS
			|| event.recipe.result.type == DIAMOND_BOOTS
		) {
			val copy = event.recipe.ingredients
			copy.find { it.type == EMERALD }!!.apply {
				type = DIAMOND
				amount = when (event.recipe.result.type) {
					DIAMOND_PICKAXE -> 3
					DIAMOND_AXE -> 3
					DIAMOND_SHOVEL -> 1
					DIAMOND_HOE -> 2
					DIAMOND_SWORD -> 2
					DIAMOND_HELMET -> 5
					DIAMOND_CHESTPLATE -> 8
					DIAMOND_LEGGINGS -> 7
					DIAMOND_BOOTS -> 4
					else -> 3
				}
			}
			if (copy.size <= 1) copy.add(ItemStack(LAPIS_LAZULI, (6..64).random()))
			event.recipe.ingredients = copy
			event.recipe.result.enchantWithLevels(50, true, Random.asJavaRandom())
		}
	}
}