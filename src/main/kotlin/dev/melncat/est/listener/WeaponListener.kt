package dev.melncat.est.listener

import com.jojodmo.itembridge.ItemBridge
import dev.melncat.est.weaponarts.ActiveWeaponArt
import dev.melncat.est.weaponarts.WeaponArtType.Entity
import dev.melncat.est.weaponarts.activeWeaponArts
import dev.melncat.est.weaponarts.ibWeaponArtMap
import dev.melncat.est.weaponarts.weaponArtMap
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID


private data class CooldownData(val start: Long, val max: Long)

private val cooldownBars = mutableMapOf<Player, CooldownData>()
private val cooldowns = mutableMapOf<UUID, Long>()

@RegisterListener
object WeaponListener : Listener {

	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
		val item = event.item
		if (!event.player.isSneaking || !event.action.isRightClick || item == null) return
		val art = ibWeaponArtMap[ItemBridge.getItemKey(item).let { it?.namespace to it?.item }] ?: weaponArtMap[item.type.key] ?: return
		val target = event.player.getTargetEntity(5) as? LivingEntity
		if (event.player.uniqueId in cooldowns && cooldowns[event.player.uniqueId]!! > System.currentTimeMillis())
			return
		if (art.type == Entity && target == null) return
		val entity = when (art.type) {
			Entity -> target!!
			else -> event.player
		}
		activeWeaponArts.add(
			ActiveWeaponArt(
				art,
				entity.location,
				event.player,
				entity,
				item
			)
		)
		val end = System.currentTimeMillis() + art.cooldown
		cooldowns[event.player.uniqueId] = end
		cooldownBars[event.player] = CooldownData(System.currentTimeMillis(), end)
	}
}

const val bars = 50

fun tickWeaponArtCooldowns() {
	val iterator = cooldownBars.iterator()
	val now = System.currentTimeMillis()
	while (iterator.hasNext()) {
		val (player, cd) = iterator.next()
		if (player.isDead || now > cd.max) {
			player.sendActionBar(Component.text(""))
			iterator.remove()
			continue
		}
		val percentage = 1 - (now - cd.start).toDouble() / (cd.max - cd.start)
		val filled = (percentage * bars).toInt()
		player.sendActionBar(
			("Cooldown <#000000>[<transition:green:#ffff00:#ff0000:$percentage><filled></transition><dark_gray><unfilled></dark_gray>]").mm(
				"filled" to "|".repeat(filled),
				"unfilled" to "|".repeat(bars - filled),
			)
		)
	}
}

fun resetCooldown(player: Player) {
	cooldowns.remove(player.uniqueId)
	cooldownBars.remove(player)
}