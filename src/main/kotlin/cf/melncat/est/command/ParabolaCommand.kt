package cf.melncat.est.command

import cf.melncat.est.util.NTC
import cf.melncat.est.util.div
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
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
object ParabolaCommand : BaseCommand(
	"parabola",
	"Teleports to in a parabolic shape.",
	"/<command> <x> <y> <z>"
) {
	override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
		if (sender !is Player) {
			sender.sendMessage("You must be a player to use this." / NTC.RED)
			return true
		}
		val target = sender.getTargetEntity(5) ?: sender
		if (launches.any { target == sender }) {
			sender.sendMessage("You are already parabolaing." / NTC.RED)
			return true
		}
		if (args.size != 3 || args.any { it.toIntOrNull() == null }) {
			sender.sendMessage("Invalid arguments provided." / NTC.RED)
			return false
		}

		val dest = Vector(args[0].toInt(), args[1].toInt(), args[2].toInt())
		val armorStand = target.location.world.spawn(target.location, ArmorStand::class.java) {
			it.isMarker = true
			it.isSmall = true
			it.isVisible = false
			it.setGravity(false)
			it.addPassenger(target)
		}
		if (armorStand.canTick()) throw IllegalStateException("Armor stands cannot tick")
		val path = dest.clone().subtract(target.location.toVector())
		launches.add(
			LaunchData(
				target, (path.length() / blocksPerSecond * 20).toInt(), path, armorStand, target.location.toVector()
			)
		)
		println((path.length() / blocksPerSecond * 20).toInt())
		return true
	}
}

fun tickParabolas() {
	val iterator = launches.listIterator()
	while (iterator.hasNext()) {
		val l = iterator.next()
		if (l.current > l.end || l.vehicle.isDead || l.entity.isDead) {
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
		(l.vehicle as CraftEntity).handle.setPos(loc.x, loc.y, loc.z)
		println(loc)
		l.current++
	}
}