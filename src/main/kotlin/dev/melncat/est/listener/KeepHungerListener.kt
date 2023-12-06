package dev.melncat.est.listener

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.component
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.LOW
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.UUID
import kotlin.math.max

private data class FoodSnapshot(val foodLevel: Int, val saturation: Float, val exhaustion: Float)

@RegisterListener
object KeepHungerListener : FListener {
	private val cache = mutableMapOf<UUID, FoodSnapshot>()

	@EventHandler(ignoreCancelled = true)
	fun onDeath(event: PlayerDeathEvent) {
		cache[event.player.uniqueId] =
			FoodSnapshot(event.player.foodLevel, event.player.saturation, event.player.exhaustion)
	}

	@EventHandler(ignoreCancelled = true)
	fun onDeath(event: PlayerPostRespawnEvent) {
		val snapshot = cache.remove(event.player.uniqueId) ?: return
		event.player.foodLevel = max(snapshot.foodLevel, 2)
		event.player.saturation = snapshot.saturation
		event.player.exhaustion = snapshot.exhaustion
	}

	@EventHandler(ignoreCancelled = true)
	fun onJoin(event: PlayerJoinEvent) {
		if (event.player.isDead && event.player.uniqueId !in cache)
			cache[event.player.uniqueId] =
				FoodSnapshot(event.player.foodLevel, event.player.saturation, event.player.exhaustion)
	}
}