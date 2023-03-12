package dev.melncat.est.listener

import com.destroystokyo.paper.MaterialTags
import dev.melncat.est.util.config
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import org.bukkit.Material.*
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.VillagerAcquireTradeEvent
import org.bukkit.event.world.ChunkLoadEvent

@RegisterListener
object VillagerListener : FListener {
	@EventHandler
	fun onAcquireTrade(event: VillagerAcquireTradeEvent) {
		if (event.recipe.result.type == ENCHANTED_BOOK
			|| MaterialTags.DIAMOND_TOOLS.isTagged(event.recipe.result)
			|| event.recipe.result.type == DIAMOND_HELMET
			|| event.recipe.result.type == DIAMOND_CHESTPLATE
			|| event.recipe.result.type == DIAMOND_LEGGINGS
			|| event.recipe.result.type == DIAMOND_BOOTS
		)
			event.isCancelled = true
	}
}