package dev.melncat.est.weaponarts

import com.destroystokyo.paper.ParticleBuilder
import dev.melncat.est.util.move
import dev.melncat.est.util.spawnDefault
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

fun WeaponArtRegistry.registerGeneric() {
	register<MutableList<Location>>("sky_strike") {
		name("Sky Strike")
		cooldown(60.seconds)
		duration(5.seconds)
		defaultState { wp -> MutableList(4) { wp.position.clone().add(
			Random.nextDouble(-5.0, 5.0),
			10.0,
			Random.nextDouble(-5.0, 5.0)
		).setDirection(Vector(0.0, -1.0, 0.0)) } }
		val particles = listOf(
			ParticleBuilder(Particle.CRIT_MAGIC)
				.count(2)
				.extra(0.0),
			ParticleBuilder(Particle.REDSTONE)
				.count(2)
				.extra(0.0)
				.data(DustOptions(Color.fromRGB(0xffffff), 1f))
		)
		executor { wp ->
			if (wp.state.isEmpty()) wp.end()
			wp.state.removeIf { l ->
				l.move(0.2)
				particles.forEach { it.location(l).spawnDefault() }
				val trace = l.world.rayTrace(
					l, l.toVector(), 0.5, FluidCollisionMode.NEVER, true,  0.5, null
				)
				if (trace != null) {
					(trace.hitEntity as? LivingEntity)?.damage(6.0)
					if (trace.hitBlock != null || trace.hitEntity != null) return@removeIf true
				}
				return@removeIf false
			}
		}
	}
}