package dev.melncat.est.listener

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
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
import org.bukkit.entity.Item
import org.bukkit.event.Event.Result.DENY
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGH
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
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
			event.blockList().clear()
	}

	@EventHandler
	fun onDamage(event: EntityDamageEvent) {
		if (event.entity is Item && (event.cause == ENTITY_EXPLOSION || event.cause == BLOCK_EXPLOSION))
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
				"coin_1000" -> 1000
				else -> null
			} else null

	@EventHandler
	fun coinCreate(event: EntityAddToWorldEvent) {
		val entity = event.entity as? Item ?: return
		val worth = getCoinValue(entity.itemStack) ?: return
		entity.isImmuneToCactus = true
		entity.isImmuneToExplosion = true
		entity.isImmuneToFire = true
		entity.isImmuneToLightning = true
	}

	@EventHandler
	fun pickupCoins(event: PlayerAttemptPickupItemEvent) {
		if (event.item.thrower != null) return
		val worth = getCoinValue(event.item.itemStack) ?: return
		val count = event.item.itemStack.amount
		eco.depositPlayer(event.player, (worth * count).toDouble())
		event.player.sendMessage("<gray>You picked up <yellow>$<0></yellow>!".mm(worth * count))
		event.isCancelled = true
		event.player.playPickupItemAnimation(event.item)
		event.item.remove()
	}

	@EventHandler
	fun redeemCoins(event: PlayerInteractEvent) {
		if (!event.action.isRightClick) return
		val item = event.item ?: return
		val worth = getCoinValue(item) ?: return
		eco.depositPlayer(event.player, worth.toDouble())
		item.subtract()
		event.setUseInteractedBlock(DENY)
		event.player.sendMessage("<gray>You got <yellow>$<0></yellow>!".mm(worth))
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