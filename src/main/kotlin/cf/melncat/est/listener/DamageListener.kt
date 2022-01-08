package cf.melncat.est.listener

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

object DamageListener : Listener {
	@EventHandler
	fun onDamage(event: EntityDamageByEntityEvent) {
		if (event.damager !is Player || event.entity !is Player) return
		while (event.finalDamage > 19) event.damage -= 1
	}

}