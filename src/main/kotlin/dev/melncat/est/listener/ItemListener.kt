package dev.melncat.est.listener

import dev.melncat.est.eco
import dev.melncat.est.util.config
import dev.melncat.est.util.get
import dev.melncat.est.util.has
import dev.melncat.est.util.isAir
import dev.melncat.est.util.meta
import dev.melncat.est.util.pd
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.mm
import net.minecraft.util.Mth.clamp
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGH
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

@RegisterListener
object ItemListener : FListener {
	@EventHandler
	fun onExplode(event: EntityExplodeEvent) {
		if (event.entityType === EntityType.CREEPER || event.entityType === EntityType.WITHER_SKULL || event.entityType === EntityType.WITHER)
			event.isCancelled = true
	}

	@EventHandler(priority = HIGH)
	fun onAnvilPrepare(event: PrepareAnvilEvent) {
		val first = event.inventory.firstItem ?: return
		val second = event.inventory.secondItem ?: return
		if (!second.hasItemMeta() || second.itemMeta.pd.get<String>(config.customItemTag) != "mystical_tome") return
		if (first.type !== Material.ENCHANTED_BOOK) return
		val res = first.clone().meta<EnchantmentStorageMeta> {
			val enchant = storedEnchants.entries.firstOrNull() ?: return
			addStoredEnchant(enchant.key, enchant.value + 1, true)
			event.inventory.repairCost = clamp(enchant.value, 1, 6)
		}
		event.result = res
	}
	private fun getCoinValue(item: ItemStack)
		= if (!item.isAir && item.hasItemMeta() && item.itemMeta.pd.has<String>(config.customItemTag))
			when (item.itemMeta.pd.get<String>(config.customItemTag)) {
				"coin_1" -> 1
				"coin_10" -> 10
				"coin_100" -> 100
				else -> null
			} else null

	@EventHandler
	fun pickupCoins(event: PlayerAttemptPickupItemEvent) {
		val worth = getCoinValue(event.item.itemStack) ?: return
		val count = event.item.itemStack.amount
		eco.depositPlayer(event.player, (worth * count).toDouble())
		event.player.sendMessage("<gray>You got <yellow>$<0></yellow>!".mm((worth * count).toString()))
		event.isCancelled = true
		event.player.playPickupItemAnimation(event.item)
		event.item.remove()
	}

	@EventHandler
	fun enderChestUse(event: PlayerInteractEvent) {
		if (!config.disableEnderChests || event.action != RIGHT_CLICK_BLOCK) return
		if (event.clickedBlock?.type == Material.ENDER_CHEST)
			event.isCancelled = true
	}

	fun test() {
	}
}