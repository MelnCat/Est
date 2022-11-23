package dev.melncat.est.listener

import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

val chances = mapOf(
	EntityType.ZOMBIE to mapOf(
		ItemStack(Material.BONE) to 20
	)
)

@RegisterListener
object MobDropListener : FListener {
	@EventHandler(ignoreCancelled = true)
	fun onDeath(event: EntityDeathEvent) {

	}
}