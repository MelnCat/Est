package dev.melncat.est.listener

import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

@RegisterListener
object DamageListener : FListener {
	@EventHandler
	fun onDamage(event: EntityDamageByEntityEvent) {
		if (event.damager !is Player || event.entity !is Player) return
		if (event.damage > 40) event.damage = 40.0
	}

	@EventHandler
	fun disableInvFrames(event: EntityDamageByEntityEvent) {
		val entity = event.entity
		if (entity !is Player) return
		entity.noDamageTicks = 0
	}
}