package cf.melncat.est.weaponarts

import cf.melncat.est.util.NTC
import cf.melncat.est.util.attackEntity
import cf.melncat.est.util.div
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Particle.DustTransition
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class WeaponArtType {
	Interact, Entity
}

data class ActiveWeaponArt(
	val art: WeaponArt,
	val position: Location,
	val player: Player,
	val entity: LivingEntity,
	val item: ItemStack,
	var time: Int = 0,
	var state: Any = art.defaultState()
) {
	fun end() {
		time = -1
	}
}

data class WeaponArt(
	val name: String,
	val duration: Int,
	val cooldown: Int,
	val type: WeaponArtType = WeaponArtType.Entity,
	val defaultState: () -> Any = { 0 },
	val cb: (ActiveWeaponArt) -> Unit,
)

val weaponArtMap = mutableMapOf<NamespacedKey, WeaponArt>()
val ibWeaponArtMap = mutableMapOf<Pair<String, String>, WeaponArt>()
val activeWeaponArts = mutableListOf<ActiveWeaponArt>()

fun weaponArtTick() {
	val iterator = activeWeaponArts.listIterator()
	while (iterator.hasNext()) {
		val art = iterator.next()
		art.art.cb(art)
		if (art.time == -1 || art.time > art.art.duration - 1) {
			iterator.remove()
			continue
		}
		art.time++

	}
}

fun defaultWeaponArts() {
	weaponArtMap[Material.WOODEN_SWORD.key] = WeaponArt(
		"Home Run", 1, 10000
	) {
		it.entity.world.spawnParticle(
			Particle.FIREWORKS_SPARK,
			it.entity.location,
			30,
			0.0,
			0.0,
			0.0,
			0.13
		)
		attackEntity(it.player, it.entity, 8.0, 0.0, 1.2)
	}
	val stoneSwordEffects = listOf(
		PotionEffect(PotionEffectType.SLOW, 200, 9),
		PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 3)
	)
	weaponArtMap[Material.STONE_SWORD.key] = WeaponArt(
		"human boulder", 1, 60000, WeaponArtType.Interact
	) {
		it.player.world.spawnParticle(
			Particle.TOTEM,
			it.player.location.add(0.0, 1.0, 0.0),
			120,
			0.0,
			0.0,
			0.0,
			0.4
		)
		it.player.addPotionEffects(stoneSwordEffects)
	}
	val ironSwordEffects = listOf(
		PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0),
		PotionEffect(PotionEffectType.SLOW, 200, 0),
		PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 0)
	)
	weaponArtMap[Material.IRON_SWORD.key] = WeaponArt(
		"ferrous form", 1, 120000, WeaponArtType.Interact
	) {
		it.player.world.spawnParticle(
			Particle.TOTEM,
			it.player.location.add(0.0, 1.0, 0.0),
			120,
			0.0,
			0.0,
			0.0,
			0.4
		)
		it.player.addPotionEffects(ironSwordEffects)
	}
	weaponArtMap[Material.GOLDEN_SWORD.key] = WeaponArt(
		"made in astria", 1, 30000
	) {
		it.player.world.spawnParticle(
			Particle.CRIT_MAGIC,
			it.entity.location,
			22,
			0.0,
			0.0,
			0.0,
			0.4
		)
		attackEntity(it.player, it.entity, 10.0)
		it.item.subtract()
	}
	val totalAngle = Math.PI / 3 * 2
	weaponArtMap[Material.DIAMOND_SWORD.key] = WeaponArt(
		"wave of radiance", 8 * 5, 5000, WeaponArtType.Interact
	) {
		val time = it.time % 8
		var angle = totalAngle / 8 * time
		val endAngle = totalAngle / 8 * (time + 1)
		while (angle < endAngle) {
			val x = it.position.x + sin(-Math.PI / 3 + angle - it.position.yaw * (Math.PI / 180)) * 2
			val z = it.position.z + cos(-Math.PI / 3 + angle - it.position.yaw * (Math.PI / 180)) * 2
			for (h in 5..20) {
				it.position.world.spawnParticle(
					Particle.DUST_COLOR_TRANSITION,
					it.position.x + sin(-Math.PI / 3 + angle - it.position.yaw * (Math.PI / 180)) * 2,
					it.position.y + h / 10.0,
					it.position.z + cos(-Math.PI / 3 + angle - it.position.yaw * (Math.PI / 180)) * 2,
					1,
					0.0,
					0.0,
					0.0,
					DustTransition(
						Color.fromRGB(Random.nextInt(0x1000000)),
						Color.fromRGB(Random.nextInt(0x1000000)),
						1.5f
					)
				)
			}
			angle += 0.1
			val entities = Location(it.position.world, x, it.position.y + 1, z).getNearbyEntities(0.5, 1.5, 0.5)
			if (entities.isNotEmpty()) {
				entities.map { e ->
					if (e is AbstractArrow) e.velocity = e.velocity.multiply(1)
					if (e is LivingEntity) e.damage(2.0, it.player)
				}
			}
		}
	}
	weaponArtMap[Material.NETHERITE_SWORD.key] = WeaponArt(
		"pillar of fire", 4 * 20, 10000, WeaponArtType.Interact
	) {
		val target = it.position.clone().add(it.position.direction.multiply(it.time / 2.0))
		var h = 0.0
		while (h < 3) {
			it.position.world.spawnParticle(
				Particle.FLAME, target.clone().add(0.0, h, 0.0), 2, 0.05, 0.0, 0.05, 0.01
			)
			it.position.world.spawnParticle(
				Particle.PORTAL, target.clone().add(0.0, h, 0.0), 2, 0.25, 0.25, 0.25, 0.01
			)
			h += 0.15
		}
		val entities = target.clone().add(0.0, 1.5, 0.0).getNearbyEntities(0.5, 1.5, 0.5)
		for (entity in entities) {
			if (entity !is LivingEntity || entity == it.player) continue
			if (entity.noDamageTicks > 0) entity.noDamageTicks = 0
			entity.damage(5.0, it.player)
			entity.fireTicks = 200
		}
	}
	val shortswordEffects = listOf(
		PotionEffect(PotionEffectType.SPEED, 200, 1),
		PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1)
	)
	ibWeaponArtMap["oraxen" to "shortsword"] = WeaponArt(
		"fight or flight", 0, 30000, WeaponArtType.Interact
	) {
		it.player.world.spawnParticle(
			Particle.CLOUD,
			it.player.location.add(0.0, 1.0, 0.0),
			60,
			0.0,
			0.0,
			0.0,
			0.4
		)
		it.player.addPotionEffect(shortswordEffects[Random.nextInt(shortswordEffects.size)])
	}
	val bloodguardSaberEffects = listOf(
		PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 2),
		PotionEffect(PotionEffectType.WITHER, 600, 2)
	)
	val bloodguardColor = DustOptions(Color.fromRGB(0xff0000), 2f)
	ibWeaponArtMap["oraxen" to "bloodguard_saber"] = WeaponArt(
		"bloodguard saber thing idk", 0, 40000, WeaponArtType.Interact
	) {
		it.player.world.spawnParticle(
			Particle.REDSTONE,
			it.player.location.add(0.0, 1.0, 0.0),
			60,
			2.0,
			2.0,
			2.0,
			bloodguardColor
		)
		it.player.addPotionEffects(bloodguardSaberEffects)
	}
	val serpentFangColor = DustTransition(Color.fromRGB(0x29d91c), Color.fromRGB(0x6f2091), 1.7f)
	val serpentFangEffects = PotionEffect(PotionEffectType.POISON, 100, 5)
	ibWeaponArtMap["oraxen" to "serpent_fang"] = WeaponArt(
		"serpent fang", 15, 20000, WeaponArtType.Interact
	) {
		val target = it.position.clone().add(0.0, 1.0, 0.0).add(it.position.direction.multiply(it.time / 2.0))
		it.position.world.spawnParticle(
			Particle.DUST_COLOR_TRANSITION, target, 2, 0.0, 0.0, 0.0, serpentFangColor
		)
		val entities = target.clone().add(0.0, 1.5, 0.0).getNearbyEntities(0.5, 1.5, 0.5)
		for (entity in entities) {
			if (entity !is LivingEntity || entity == it.player) continue
			entity.addPotionEffect(serpentFangEffects)
			entity.damage(8.0, it.player)
		}
	}
	ibWeaponArtMap["oraxen" to "dagger"] = WeaponArt(
		"quickstep", 1, 5000, WeaponArtType.Interact
	) {
		it.player.world.spawnParticle(
			Particle.CLOUD,
			it.player.location,
			60,
			0.0,
			0.0,
			0.0,
			0.4
		)
		it.player.velocity = it.player.velocity.add(it.position.direction.multiply(1.2))
	}
	val frostfireStraightswordEffects = listOf(
		PotionEffect(PotionEffectType.SLOW, 200, 1)
	)
	ibWeaponArtMap["oraxen" to "frostfire_straightsword"] = WeaponArt(
		"frostfire", 30, 20000, WeaponArtType.Interact
	) {
		val target = it.position.clone().add(0.0, 1.0, 0.0).add(it.position.direction.multiply(it.time / 3.0))
		var h = 0.0
		while (h < 1.7) {
			val loc = target.clone()
				.add(Vector(0.0, h - 0.85, 0.0).rotateAroundAxis(it.position.direction, it.time * 0.2))
			val loc2 = target.clone()
				.add(Vector(0.0, h - 0.85, 0.0).rotateAroundAxis(it.position.direction, it.time * 0.2 + Math.PI / 2))
			it.position.world.spawnParticle(
				Particle.ENCHANTMENT_TABLE, loc, 1, 0.0, 0.0, 0.0, 0.01
			)
			it.position.world.spawnParticle(
				Particle.ENCHANTMENT_TABLE, loc2, 1, 0.0, 0.0, 0.0, 0.01
			)
			it.position.world.spawnParticle(
				Particle.SOUL_FIRE_FLAME, loc, 1, 0.0, 0.0, 0.0, 0.01
			)
			it.position.world.spawnParticle(
				Particle.FLAME, loc2, 1, 0.0, 0.0, 0.0, 0.01
			)
			h += 0.1
		}
		val entities = target.clone().add(0.0, 1.5, 0.0).getNearbyEntities(0.5, 1.5, 0.5)
		for (entity in entities) {
			if (entity !is LivingEntity || entity == it.player) continue
			entity.damage(12.0, it.player)
			entity.fireTicks = 200
			entity.freezeTicks = 200
			entity.addPotionEffects(frostfireStraightswordEffects)
		}
	}
	ibWeaponArtMap["oraxen" to "maozesword"] = WeaponArt(
		"mao", 20, 20000, WeaponArtType.Interact
	) {
		if (it.time % 4 == 0) {
			(it.player.getNearbyEntities(3.0, 3.0, 3.0) + it.player).forEach {
				p -> if (p is Player) p.sendMessage("+1000 SOCIAL CREDIT" / NTC.GREEN)
			}
		}
	}
}