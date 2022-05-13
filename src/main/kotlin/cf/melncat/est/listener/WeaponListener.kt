package cf.melncat.est.listener

import cf.melncat.est.util.NTC
import cf.melncat.est.util.div
import cf.melncat.est.util.mm
import cf.melncat.est.weaponarts.ActiveWeaponArt
import cf.melncat.est.weaponarts.WeaponArtType.Entity
import cf.melncat.est.weaponarts.activeWeaponArts
import cf.melncat.est.weaponarts.ibWeaponArtMap
import cf.melncat.est.weaponarts.weaponArtMap
import com.jojodmo.itembridge.ItemBridge
import org.apache.commons.lang3.tuple.MutablePair
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID
import kotlin.math.floor


private data class CooldownData(var current: Int, val max: Int)

private val cooldownBars = mutableMapOf<Player, CooldownData>()
private val cooldowns = mutableMapOf<UUID, Long>()

object WeaponListener : Listener {

	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
		val item = event.item
		if (!event.player.isSneaking || !event.action.isRightClick || item == null) return
		val ibKey = ItemBridge.getItemKey(item)
		val art = weaponArtMap[item.type.key] ?: ibWeaponArtMap[ItemBridge.getItemKey(item)] ?: return
		val target = event.player.getTargetEntity(5) as? LivingEntity
		if (event.player.uniqueId in cooldowns && cooldowns[event.player.uniqueId]!! > System.currentTimeMillis())
			return
		if (art.type == Entity && target == null) return
		activeWeaponArts.add(
			ActiveWeaponArt(
				art,
				target?.location ?: event.player.location,
				event.player,
				target ?: event.player,
				item
			)
		)
		cooldowns[event.player.uniqueId] = System.currentTimeMillis() + art.cooldown
		cooldownBars[event.player] = CooldownData(art.cooldown / 20, art.cooldown / 20)
	}
}

const val bars = 50

fun tickWeaponArtCooldowns() {
	val iterator = cooldownBars.iterator()
	while (iterator.hasNext()) {
		val (player, cd) = iterator.next()
		if (player.isDead || cd.current < 0) {
			player.sendActionBar("" / NTC.WHITE)
			iterator.remove()
			continue
		}
		val percentage = cd.current.toDouble() / cd.max
		val filled = (percentage * bars).toInt()
		player.sendActionBar(
			("Cooldown <#000000>[<transition:green:#ffff00:#ff0000:$percentage><filled></transition><dark_gray><unfilled></dark_gray>]").mm(
				"filled" to "|".repeat(filled),
				"unfilled" to "|".repeat(bars - filled),
			)
		)
		cd.current--
	}
}

fun resetCooldown(player: Player) {
	cooldowns.remove(player.uniqueId)
	cooldownBars.remove(player)
}