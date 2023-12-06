package dev.melncat.est.listener

import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import org.bukkit.entity.EntityType.SNOWBALL
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_EXPLOSION

@RegisterListener
object DamageListener : FListener {
	@EventHandler
	fun onDamage(event: EntityDamageByEntityEvent) {
		if (event.damager is Player && event.entity is Player)
			if (event.damage > 40) event.damage = 40.0
		if (event.damager.type == SNOWBALL && event.cause == ENTITY_EXPLOSION)
			if (event.damage > 4) event.damage = 4.0
	}

	@EventHandler
	fun disableInvFrames(event: EntityDamageByEntityEvent) {
		val entity = event.entity
		if (entity !is Player) return
		entity.noDamageTicks = 0
	}
}