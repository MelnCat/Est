package cf.melncat.est.listener

import cf.melncat.est.plugin
import cf.melncat.est.util.ARMOR_EFFECT_KEY
import cf.melncat.est.util.PDC
import cf.melncat.est.util.get
import cf.melncat.est.util.has
import cf.melncat.est.util.pd
import com.google.gson.Gson
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

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
}