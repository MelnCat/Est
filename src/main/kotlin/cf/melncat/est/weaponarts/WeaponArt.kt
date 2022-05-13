package cf.melncat.est.weaponarts

import cf.melncat.est.util.attackEntity
import com.jojodmo.itembridge.ItemBridgeKey
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustTransition
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
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
	var time: Int = 0
)

data class WeaponArt(
	val name: String,
	val duration: Int,
	val cooldown: Int,
	val type: WeaponArtType = WeaponArtType.Entity,
	val cb: (ActiveWeaponArt) -> Unit
)

val weaponArtMap = mutableMapOf<NamespacedKey, WeaponArt>()
val ibWeaponArtMap = mutableMapOf<ItemBridgeKey, WeaponArt>()
val activeWeaponArts = mutableListOf<ActiveWeaponArt>()

fun weaponArtTick() {
	val iterator = activeWeaponArts.listIterator()
	while (iterator.hasNext()) {
		val art = iterator.next()
		art.art.cb(art)
		if (art.time > art.art.duration) {
			iterator.remove()
			continue
		}
		art.time++

	}
}

fun defaultWeaponArts() {
	weaponArtMap[Material.WOODEN_SWORD.key] = WeaponArt(
		"Home Run", 0, 10000
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
		"human boulder", 0, 60000, WeaponArtType.Interact
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
		"ferrous form", 0, 120000, WeaponArtType.Interact
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
		"made in astria", 0, 30000
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
		"wave of radiance", 10, 5000, WeaponArtType.Interact
	) {
		var angle = totalAngle / 11 * it.time
		val endAngle = totalAngle / 11 * (it.time + 1)
		while (angle < endAngle) {
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
		}
	}
	val shortswordEffects = listOf(
		PotionEffect(PotionEffectType.SPEED, 200, 1),
		PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1)
	)
	ibWeaponArtMap[ItemBridgeKey("oraxen", "shortsword")] = WeaponArt(
		"fight or flight", 0, 40000, WeaponArtType.Interact
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
}