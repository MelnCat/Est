package dev.melncat.est.weaponart

import com.destroystokyo.paper.ParticleBuilder
import dev.melncat.est.util.attackWith
import dev.melncat.est.util.move
import dev.melncat.est.util.spawnDefault
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.component
import org.bukkit.Color
import org.bukkit.EntityEffect
import org.bukkit.EntityEffect.HURT
import org.bukkit.GameMode.SURVIVAL
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Particle.DustTransition
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

fun WeaponArtRegistry.registerCustom() {
	register("fight_or_flight") {
		name("Fight or Flight")
		cooldown(30.seconds)
		val effects = listOf(
			PotionEffect(PotionEffectType.SPEED, 200, 1),
			PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1)
		)
		executor { wp ->
			wp.player.world.spawnParticle(
				Particle.CLOUD,
				wp.player.location.add(0.0, 1.0, 0.0),
				60,
				0.0,
				0.0,
				0.0,
				0.4
			)
			wp.player.addPotionEffects(effects)
		}
	}

	register("sacrificial_blow") {
		name("Sacrificial Blow")
		cooldown(40.seconds)
		val effects = listOf(
			PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 3),
			PotionEffect(PotionEffectType.FAST_DIGGING, 200, 1),
			PotionEffect(PotionEffectType.WITHER, 20 * 30, 4)
		)
		executor { wp ->
			wp.player.world.spawnParticle(
				Particle.REDSTONE,
				wp.player.location.add(0.0, 1.0, 0.0),
				60,
				2.0,
				2.0,
				2.0,
				DustOptions(Color.fromRGB(0xff0000), 2f)
			)
			wp.player.addPotionEffects(effects)
			wp.player.health /= 4
			wp.player.playEffect(EntityEffect.HURT)
		}
	}

	register("venom_spray") {
		name("Venom Spray")
		cooldown(25.seconds)
		duration(15.seconds)
		val color = DustTransition(Color.fromRGB(0x29d91c), Color.fromRGB(0x6f2091), 1.7f)
		val effects = PotionEffect(PotionEffectType.POISON, 100, 5)
		executor { wp ->
			val target = wp.position.clone().add(0.0, 1.0, 0.0).add(wp.position.direction.multiply(wp.time / 2.0))
			wp.position.world.spawnParticle(
				Particle.DUST_COLOR_TRANSITION, target, 2, 0.0, 0.0, 0.0, color
			)
			val entities = target.clone().add(0.0, 1.5, 0.0).getNearbyEntities(0.5, 1.5, 0.5)
			for (entity in entities) {
				if (entity !is LivingEntity || entity == wp.player) continue
				entity.addPotionEffect(effects)
				wp.player.attackWith(entity, 8.0)
			}
		}
	}

	register("dash") {
		name("Dash")
		cooldown(15.seconds)

		executor { wp ->
			wp.player.world.spawnParticle(
				Particle.CLOUD,
				wp.player.location,
				60,
				0.0,
				0.0,
				0.0,
				0.4
			)
			wp.player.velocity = wp.player.velocity.add(wp.position.direction.multiply(1.2))
		}
	}

	register("frostfire_spiral") {
		name("Frostfire Spiral")
		cooldown(25.seconds)
		duration(1.5.seconds)
		val effects = listOf(
			PotionEffect(PotionEffectType.SLOW, 200, 1)
		)

		executor { wp ->
			val target = wp.position.clone().add(0.0, 1.0, 0.0).add(wp.position.direction.multiply(wp.time / 3.0))
			var h = 0.0
			while (h < 1.7) {
				val loc = target.clone()
					.add(Vector(0.0, h - 0.85, 0.0).rotateAroundAxis(wp.position.direction, wp.time * 0.2))
				val loc2 = target.clone()
					.add(Vector(0.0, h - 0.85, 0.0).rotateAroundAxis(wp.position.direction, wp.time * 0.2 + Math.PI / 2))
				wp.position.world.spawnParticle(
					Particle.ENCHANTMENT_TABLE, loc, 1, 0.0, 0.0, 0.0, 0.01
				)
				wp.position.world.spawnParticle(
					Particle.ENCHANTMENT_TABLE, loc2, 1, 0.0, 0.0, 0.0, 0.01
				)
				wp.position.world.spawnParticle(
					Particle.SOUL_FIRE_FLAME, loc, 1, 0.0, 0.0, 0.0, 0.01
				)
				wp.position.world.spawnParticle(
					Particle.FLAME, loc2, 1, 0.0, 0.0, 0.0, 0.01
				)
				h += 0.1
			}
			val entities = target.clone().add(0.0, 1.5, 0.0).getNearbyEntities(0.5, 1.5, 0.5)
			for (entity in entities) {
				if (entity !is LivingEntity || entity == wp.player) continue
				entity.damage(12.0, wp.player)
				entity.fireTicks = 200
				entity.freezeTicks = 200
				entity.addPotionEffects(effects)
			}
		}
	}.custom("est:frostfire_straightsword")

	register("social_credit") {
		name("Social Credit")
		cooldown(20.seconds)
		duration(1.seconds)

		executor { wp ->
			if (wp.time % 4 == 0) {
				(wp.player.location.getNearbyPlayers(3.0, 3.0, 3.0) + wp.player).forEach {
						p -> p.sendMessage("+1000 SOCIAL CREDIT".component(NTC.GREEN))
				}
			}
		}
	}

	register("instant_severance") {
		name("Instant Severance")
		cooldown(20.seconds)
		duration(1.5.seconds)

		executor {
			val target = it.position.clone().add(it.position.direction.multiply(it.time / 2.0))
			var h = 0.0
			while (h < 3) {
				it.position.world.spawnParticle(
					Particle.CLOUD, target.clone().add(0.0, h, 0.0), 2, 0.05, 0.0, 0.05, 0.01
				)
				h += 0.15
			}
			val entities = target.clone().add(0.0, 1.5, 0.0).getNearbyEntities(0.5, 1.5, 0.5)
			for (entity in entities) {
				if (entity !is LivingEntity || entity == it.player) continue
				if (entity.noDamageTicks > 0) entity.noDamageTicks = 0
				it.player.attackWith(entity, 1.0, 0.0, 1.2)
			}
		}
	}

	register("cloudkill") {
		name("Cloudkill")
		cooldown(60.seconds)
		duration(4.seconds)
		val dust =
			ParticleBuilder(Particle.REDSTONE)
				.count(512)
				.extra(0.0)
				.data(DustOptions(Color.fromRGB(0x111111), 16f))
		val flame =
			ParticleBuilder(Particle.FLAME)
				.count(376)
				.extra(0.0)

		executor {
			if (it.firstTick) it.duration += Random.nextInt(40)
			val radius = 5.0 + (it.duration - 4 * 20) / 20
			if (it.time % 5 == 0) {
				dust
					.location(it.player.location)
					.offset(radius, 4.0, radius)
					.receivers(particleViewDistance)
					.spawn()
				flame
					.location(it.player.location)
					.offset(radius, 4.0, radius)
					.receivers(particleViewDistance)
					.spawn()
				it.player.location.world.getNearbyLivingEntities(it.player.location, radius, 4.0, radius).forEach { e ->
					if (e == it.player || e.isInvulnerable || (e is Player && e.gameMode != SURVIVAL)) return@forEach
					e.damage(0.4, it.player)
					e.fireTicks = 20
				}

			}
		}
	}

	register("crystal_sweep") {
		name("Crystal Sweep")
		cooldown(60.seconds)
		duration(4.seconds)
		val particle =
			ParticleBuilder(Particle.DUST_COLOR_TRANSITION)
				.count(2)
				.data(DustTransition(Color.fromRGB(0xC58EED), Color.fromRGB(0x63469C), 2.0f))

		executor {
			val angle = Math.PI / 2
			val offset = it.time / 2.0
			val direction = it.position.direction.apply { y = 0.0 }
			val right = direction.clone().rotateAroundY(angle)
			for (i in -10..10) {
				val loc = it.position.clone().add(direction.clone().multiply(offset - abs(i / 10.0)))
					.add(right.clone().multiply(i / 10.0))
				particle
					.location(loc)
					.receivers(particleViewDistance)
					.spawn()
				it.position.world.getNearbyLivingEntities(loc, 0.25).forEach {
						e -> it.player.attackWith(e, damage = 5.0, knockback = 1.5)
				}
			}
		}
	}

	register<Double>("peer_into_the_deep") {
		name("Peer Into The Deep")
		cooldown(60.seconds)
		duration(20.seconds)
		defaultState { 0.0 }
		val particle = ParticleBuilder(Particle.REDSTONE)
			.count(32)
			.offset(1.0, 0.5, 1.0)
			.extra(0.0)

		executor { wp ->
			if (wp.time % 5 == 0) particle
				.location(wp.player.location)
				.receivers(particleViewDistance)
				.data(DustOptions(Color.fromRGB((0x010000 * (wp.state * 20).toInt().coerceAtMost(0xff))), 1.5f))
				.spawn()
		}
		onAttack { event, wp ->
			wp.state += 0.5
			event.damage += wp.state
		}
	}
	register("berserk") {
		name("Berserk")
		cooldown(40.seconds)
		val effects = listOf(
			PotionEffect(PotionEffectType.SPEED, 200, 0),
			PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0),
			PotionEffect(PotionEffectType.JUMP, 200, 0)
		)
		executor { wp ->
			wp.player.world.spawnParticle(
				Particle.REDSTONE,
				wp.player.location.add(0.0, 1.0, 0.0),
				60,
				0.0,
				2.0,
				2.0,
				2.0,
				DustOptions(Color.fromRGB(0xff0000), 3f)
			)
			wp.player.addPotionEffects(effects)
		}
	}
	register<Location>("soul_missile") {
		name("Soul Missile")
		duration(3.seconds)
		cooldown(20.seconds)
		defaultState { it.player.location.add(0.0, 1.0, 0.0) }
		val particle = ParticleBuilder(Particle.DUST_COLOR_TRANSITION)
			.data(DustTransition(Color.fromRGB(0x5cb0d1), Color.fromRGB(0x315082), 1.0f))
			.offset(0.0, 0.0, 0.0)
			.count(1)
			.extra(0.0)

		executor { wp ->
			particle.location(wp.state).spawnDefault()
			wp.state.getNearbyLivingEntities(0.5).forEach {
				if (it != wp.player) {
					it.health = max(0.0, it.health - 3)
					it.noDamageTicks = 20
					it.playEffect(HURT)
					wp.end()
				}
			}
			wp.state.move(0.4)
		}
	}
}