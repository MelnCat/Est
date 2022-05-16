package cf.melncat.est.listener

import cf.melncat.est.util.config
import cf.melncat.est.util.get
import cf.melncat.est.util.pd
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.World
import org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.data.type.Dispenser
import org.bukkit.entity.EntityType.FIREBALL
import org.bukkit.entity.Fireball
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.lang.Thread.yield


object HealthListener : Listener {
	private val modifier = AttributeModifier("est default max health", -10.0, AttributeModifier.Operation.ADD_NUMBER)
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