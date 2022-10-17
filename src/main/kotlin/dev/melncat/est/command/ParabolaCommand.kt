package dev.melncat.est.command

import cloud.commandframework.bukkit.arguments.selector.MultipleEntitySelector
import cloud.commandframework.bukkit.parsers.location.LocationArgument
import cloud.commandframework.bukkit.parsers.selector.MultipleEntitySelectorArgument
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

private data class LaunchData(
	val entity: Entity,
	val end: Int,
	val path: Vector,
	val vehicle: ArmorStand,
	val initial: Vector,
	var current: Int = 0
)

private const val blocksPerSecond = 8
private const val bounceHeight = 4
private val launches = mutableListOf<LaunchData>()

@RegisterCommand
object ParabolaCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("parabola") {
			permission = "est.command.parabola"
			argument(MultipleEntitySelectorArgument.of("target"))
			argument(LocationArgument.of("destination"))
			handler { ctx ->
				val entities = ctx.get<MultipleEntitySelector>("target").entities.filter { e ->
					!launches.any { it.entity == e }
				}
				val destination = ctx.get<Location>("destination")
				for (target in entities) {
					val armorStand = target.location.world.spawn(target.location, ArmorStand::class.java) {
						it.isMarker = true
						it.isSmall = true
						it.isVisible = false
						it.setGravity(false)
						it.addPassenger(target)
					}
					if (armorStand.canTick()) throw IllegalStateException("Armor stands cannot tick")
					val path = destination.toVector().subtract(target.location.toVector())
					launches.add(
						LaunchData(
							target, (path.length() / blocksPerSecond * 20).toInt(), path, armorStand, target.location.toVector()
						)
					)
				}
			}
		}
	}
}

fun tickParabolas() {
	val iterator = launches.listIterator()
	while (iterator.hasNext()) {
		val l = iterator.next()
		if (l.current > l.end || l.vehicle.isDead || l.entity.isDead || l.vehicle.passengers.isEmpty()) {
			iterator.remove()
			if (!l.vehicle.isDead) {
				l.vehicle.passengers.clear()
				l.vehicle.remove()
			}
			continue
		}
		val parabola = -4.0 * (bounceHeight / (l.end * l.end).toDouble()) * l.current * (l.current - l.end)
		val loc = if (l.current == l.end) l.initial.clone().add(l.path).toLocation(l.entity.world)
		else l.initial.toLocation(l.entity.world)
			.add(l.path.clone().divide(Vector(l.end, l.end, l.end)).multiply(l.current))
			.add(0.0, parabola, 0.0)
		@Suppress("UnstableApiUsage")
		l.vehicle.teleport(loc, true)
		l.current++
	}
}