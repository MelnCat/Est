package dev.melncat.est.weaponarts

import com.destroystokyo.paper.ParticleBuilder
import dev.melncat.est.util.attackWith
import dev.melncat.est.util.move
import dev.melncat.est.weaponarts.WeaponArtActivation.InteractEntity
import net.kyori.adventure.util.TriState
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material.*
import org.bukkit.Particle
import org.bukkit.Particle.DustTransition
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

fun WeaponArtRegistry.registerVanilla() {
	register("home_run") {
		name("Home Run")
		cooldown(10.seconds)
		activation(InteractEntity)
		executor { wp ->
			wp.entity.world.spawnParticle(
				Particle.FIREWORKS_SPARK,
				wp.entity.location,
				30,
				0.0,
				0.0,
				0.0,
				0.13
			)
			wp.player.attackWith(wp.entity, 8.0, 0.0, 1.2, true)
		}
	}.item(WOODEN_SWORD)
	register("hurl_rock") {
		name("Hurl Rock")
		cooldown(5.seconds)
		executor { wp ->
			wp.player.launchProjectile(Snowball::class.java, null) {
				it.item = ItemStack(COBBLESTONE)
			}
		}
	}.item(STONE_SWORD)
	register<List<Location>>("ferrous_strike") {
		name("Ferrous Strike")
		duration(2.seconds)
		cooldown(30.seconds)
		val projectileCount = 5
		defaultState { wp ->
			List(projectileCount) { i ->
				wp.position.clone()
					.add(0.0, 0.5, 0.0)
					.setDirection(
						wp.position.direction.setY(0).rotateAroundY(
							(2 * Math.PI) / (projectileCount * 4) * (i - (projectileCount - 1) / 2.0)
						)
					)
			}
		}
		val particle = ParticleBuilder(Particle.ELECTRIC_SPARK)
			.count(5)
			.offset(0.05, 0.05, 0.05)
			.extra(0.0)
		val trail = ParticleBuilder(Particle.FIREWORKS_SPARK)
			.count(1)
			.extra(0.0)
		executor { wp ->
			wp.state.forEach {
				it.move(0.3)
				particle.location(it).receivers(particleViewDistance).spawn()
				trail.location(it).receivers(particleViewDistance).spawn()
				it.getNearbyLivingEntities(0.2).forEach { e ->
					if (e != wp.player) {
						e.damage(4.0)
						e.knockback(0.2, it.x - e.location.x, it.z - e.location.z)
					}
				}
			}
		}
	}.item(IRON_SWORD)
	register<Pair<Collection<Block>, Collection<Player>>>("midas_touch") {
		name("Midas Touch")
		cooldown(30.seconds)
		duration(5.seconds)
		val particle = ParticleBuilder(Particle.WAX_ON)
			.count(555)
			.offset(5.0, 2.0, 5.0)
			.extra(3.0)
		val effect = PotionEffect(PotionEffectType.POISON, 20 * 5, 0, true)
		defaultState {
			val blocks = mutableListOf<Block>()
			for (i in -5..5)
				for (j in -2..2)
					for (k in -5..5) {
						val block = it.position.clone().add(i.toDouble(), j.toDouble(), k.toDouble()).block
						if (block.isSolid) blocks.add(block)
					}
			blocks to it.position.getNearbyPlayers(5.0, 2.0, 5.0)
		}
		executor { wp ->
			if (wp.firstTick) {
				particle.location(wp.position).receivers(particleViewDistance).spawn()
				wp.position.getNearbyLivingEntities(5.0, 2.0, 5.0).forEach { e ->
					if (e != wp.player) {
						e.damage(2.0)
						e.addPotionEffect(effect)
					}
				}
				wp.state.second.forEach { p ->
					p.sendMultiBlockChange(wp.state.first.associate { it.location to GOLD_BLOCK.createBlockData() })
				}
			}
			if (wp.lastTick) {
				wp.state.second.forEach { p ->
					p.sendMultiBlockChange(wp.state.first.associate { it.location to it.blockData })
				}
			}
		}
	}.item(GOLDEN_SWORD)
	register("wave_of_radiance") {
		name("Wave of Radiance")
		cooldown(25.seconds)
		duration(8.seconds)
		val totalAngle = Math.PI / 3 * 2
		executor {
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
						if (e is Projectile && e.hasLeftShooter()) {
							e.velocity = e.velocity.multiply(-2)
							e.setHasLeftShooter(false)
						}
						if (e is LivingEntity) it.player.attackWith(e, 2.0, knockback = 1.5)
					}
				}
			}
		}
	}.item(DIAMOND_SWORD)
	register("pillar_of_fire") {
		name("Pillar of Fire")
		cooldown(25.seconds)
		duration(4.seconds)
		executor {
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
	}.item(NETHERITE_SWORD)
}