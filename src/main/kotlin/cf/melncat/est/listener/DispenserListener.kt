package cf.melncat.est.listener

import cf.melncat.est.util.config
import cf.melncat.est.util.get
import cf.melncat.est.util.pd
import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.data.type.Dispenser
import org.bukkit.entity.EntityType.FIREBALL
import org.bukkit.entity.Fireball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.persistence.PersistentDataType
import java.lang.Thread.yield


object DispenserListener : Listener {
	@EventHandler
	fun onInteract(event: BlockPreDispenseEvent) {
		val fireballYield = config.fireballs.materials[event.itemStack.type]
			?: config.fireballs.customItems[event.itemStack.itemMeta.pd.get(
				NamespacedKey.fromString(config.customItemTag)!!,
				PersistentDataType.STRING
			)]
			?: return
		val dispenser = event.block.blockData as? Dispenser ?: return
		event.isCancelled = true
		event.block.world.spawn(
			event.block.location.add(dispenser.facing.direction).add(0.5, 0.0, 0.5),
			Fireball::class.java,
			CUSTOM
		) {
			it.direction = dispenser.facing.direction
			it.setIsIncendiary(false)
			it.yield = fireballYield
		}
		event.itemStack.subtract()
	}

	@EventHandler
	fun onEntityExplode(event: EntityExplodeEvent) {
		event.yield = 100f
	}
}