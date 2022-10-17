package dev.melncat.est.listener

import dev.melncat.furcation.plugin.loaders.RegisterListener
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import dev.melncat.furcation.plugin.loaders.FListener
import org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@RegisterListener
object HealthListener : FListener {
	private val modifier = AttributeModifier("est default max health", 0.0, AttributeModifier.Operation.ADD_NUMBER)
	private fun updateMaxHealth(player: Player) {
		val attr = player.getAttribute(GENERIC_MAX_HEALTH)!!
		val old = attr.modifiers.find { it.name == modifier.name }
		if (old != null && (old.amount != modifier.amount || old.operation != modifier.operation)) attr.removeModifier(old)
		if (old == null) attr.addModifier(modifier)
	}
	@EventHandler
	fun onPlayerRespawn(event: PlayerPostRespawnEvent) {
		updateMaxHealth(event.player)
		event.player.health = event.player.getAttribute(GENERIC_MAX_HEALTH)!!.value
	}
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		updateMaxHealth(event.player)
	}
}