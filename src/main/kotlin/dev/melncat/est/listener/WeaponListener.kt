package dev.melncat.est.listener

import dev.melncat.est.util.EstKey
import dev.melncat.est.util.get
import dev.melncat.est.weaponart.ActiveWeaponArt
import dev.melncat.est.weaponart.ActiveWeaponArts
import dev.melncat.est.weaponart.ActiveWeaponArts.cooldowns
import dev.melncat.est.weaponart.ActiveWeaponArts.resetCooldown
import dev.melncat.est.weaponart.WeaponArtRegistry
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE
import org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER
import org.bukkit.attribute.AttributeModifier.Operation.ADD_SCALAR
import org.bukkit.attribute.AttributeModifier.Operation.MULTIPLY_SCALAR_1
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGH
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.Damageable
import xyz.xenondevs.nova.item.behavior.Tool
import xyz.xenondevs.nova.material.NovaMaterialRegistry
import xyz.xenondevs.nova.util.item.DamageableUtils

@RegisterListener
object WeaponListener : Listener {
	@EventHandler
	fun onDeath(event: PlayerDeathEvent) {
		resetCooldown(event.player)
	}

	@EventHandler
	fun onRespawn(event: PlayerRespawnEvent) {
		event.player.sendActionBar(Component.empty())
	}

	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
		val item = event.item
		if (!event.player.isSneaking || !event.action.isRightClick || item == null) return
		val art = WeaponArtRegistry.fromItem(item) ?: return
		if (ActiveWeaponArts.inCooldown(event.player)) return
		if (!ActiveWeaponArts.executeWeaponArt(art, event)) return

		if (item.persistentDataContainer.get<Boolean>(EstKey.weaponArtDestroyOnUse) == true) {
			if (event.hand != null) event.player.broadcastItemBreak(event.hand!!)
			item.subtract()
		}
	}

	@EventHandler(ignoreCancelled = true, priority = HIGH)
	fun onAttack(event: EntityDamageByEntityEvent) {
		val damager = event.damager as? Player ?: return
		@Suppress("UNCHECKED_CAST") val arts =
			ActiveWeaponArts.active.filter { it.player == damager } as List<ActiveWeaponArt<Any>>
		arts.forEach {
			it.art.onAttack(event, it)
		}
	}

	@EventHandler
	fun onProjectileHit(event: ProjectileHitEvent) {
		val entity = event.entity
		val hit = event.hitEntity as? LivingEntity
		if (entity !is Snowball) return
		if (entity.item.type == Material.COBBLESTONE) {
			hit?.damage(2.0, entity.shooter as? Entity ?: entity)
		}
		if (entity.persistentDataContainer.get<Boolean>(EstKey.thrownWeapon) == true) {
			if (hit != null) {
				var damage =
					NovaMaterialRegistry.getOrNull(entity.item)?.novaItem?.getBehavior(Tool::class)?.options?.attackDamage
						?: 1.0
				for (modifier in entity.item.type.getDefaultAttributeModifiers(EquipmentSlot.HAND)
					.get(GENERIC_ATTACK_DAMAGE)) {
					damage = when (modifier.operation) {
						ADD_NUMBER, ADD_SCALAR -> damage + modifier.amount
						MULTIPLY_SCALAR_1 -> (damage + 1) * modifier.amount
					}
				}
				val original = damage
				for (modifier in entity.item.getAttributeModifiers(EquipmentSlot.HAND).get(GENERIC_ATTACK_DAMAGE)) {
					damage = when (modifier.operation) {
						ADD_NUMBER -> damage + modifier.amount
						ADD_SCALAR -> damage + modifier.amount * original
						MULTIPLY_SCALAR_1 -> (damage + 1) * modifier.amount
					}
				}
				hit.damage(damage, entity.shooter as? Entity ?: entity)
			}
			val item = entity.item
			DamageableUtils.setDamage(item, DamageableUtils.getDamage(item) + 14)
			val meta = item.itemMeta as? Damageable
			if (meta == null || DamageableUtils.getDamage(item) <= DamageableUtils.getMaxDurability(item))
				entity.world.dropItem(entity.location, item)
		}
	}
}

const val bars = 50

fun tickWeaponArtCooldowns() {
	val iterator = cooldowns.iterator()
	val now = System.currentTimeMillis()
	while (iterator.hasNext()) {
		val (uuid, cd) = iterator.next()
		val player = Bukkit.getPlayer(uuid)
		if (player == null || player.isDead || now > cd.end) {
			player?.sendActionBar(Component.text(""))
			iterator.remove()
			continue
		}
		val percentage = 1 - (now - cd.start).toDouble() / (cd.end - cd.start)
		val filled = (percentage * bars).toInt()
		player.sendActionBar(
			("Cooldown <#000000>[<transition:green:#ffff00:#ff0000:$percentage><filled></transition><dark_gray><unfilled></dark_gray>]").mm(
				"filled" to "|".repeat(filled),
				"unfilled" to "|".repeat(bars - filled),
			)
		)
	}
}
