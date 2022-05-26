package cf.melncat.est.listener

import cf.melncat.est.eco
import cf.melncat.est.util.ARMOR_EFFECT_KEY
import cf.melncat.est.util.PDC
import cf.melncat.est.util.config
import cf.melncat.est.util.get
import cf.melncat.est.util.has
import cf.melncat.est.util.isAir
import cf.melncat.est.util.meta
import cf.melncat.est.util.mm
import cf.melncat.est.util.pd
import cf.melncat.furcation.plugin.loaders.RegisterListener
import com.comphenix.protocol.PacketType.Play
import com.comphenix.protocol.events.PacketContainer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth.clamp
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGH
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.potion.PotionEffect
import java.util.UUID

@RegisterListener
object ItemListener : Listener {
	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
	}

	private val milkPending = mutableMapOf<UUID, List<PotionEffect>>()

	private val teleportPending = mutableSetOf<UUID>()

	@EventHandler
	fun onConsume(event: PlayerItemConsumeEvent) {
		val item = event.item
		val player = event.player
		if (item.hasLore()) {
			val lore = item.lore()
			if (lore !== null && lore.isNotEmpty() && LegacyComponentSerializer.legacyAmpersand()
					.serialize(lore.first()).contains("spicy", true)
			) player.fireTicks = 100
		}
		if (item.hasDisplayName() && item.type === Material.CHORUS_FRUIT) teleportPending.add(player.uniqueId)
		if (!item.hasItemMeta()) return
		if (!item.itemMeta.pd.has<Array<PDC>>(ARMOR_EFFECT_KEY)) return
		val effects = item.persistentDataContainer.get<Array<PotionEffect>>(ARMOR_EFFECT_KEY) ?: return
		if (item.type === Material.MILK_BUCKET) milkPending[player.uniqueId] = effects.asList()
		for (effect in effects) player.addPotionEffect(effect)
	}

	@EventHandler
	fun onPotionEffect(event: EntityPotionEffectEvent) {
		if (event.cause !== EntityPotionEffectEvent.Cause.MILK) return
		val pending = milkPending.remove(event.entity.uniqueId) ?: return
		event.isCancelled = true
		for (effect in pending) {
			(event.entity as Player).addPotionEffect(effect)
		}
	}

	@EventHandler
	fun onTeleport(event: PlayerTeleportEvent) {
		if (event.cause === PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT && teleportPending.remove(event.player.uniqueId))
			event.isCancelled = true
	}

	@EventHandler
	fun onExplode(event: EntityExplodeEvent) {
		if (event.entityType === EntityType.CREEPER || event.entityType === EntityType.WITHER_SKULL || event.entityType === EntityType.WITHER)
			event.isCancelled = true
	}

	@EventHandler
	fun onItemDamage(event: PlayerItemDamageEvent) {
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
		val packet = PacketContainer(Play.Server.COLLECT).apply {

		}
		event.item.remove()
	}

	fun test() {
		println(ServerPlayer.DATA_ARROW_COUNT_ID)
	}
}