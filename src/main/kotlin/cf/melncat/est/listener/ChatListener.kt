package cf.melncat.est.listener

import cf.melncat.est.util.config
import cf.melncat.est.util.get
import cf.melncat.est.util.pd
import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.World
import org.bukkit.block.data.type.Dispenser
import org.bukkit.entity.EntityType.FIREBALL
import org.bukkit.entity.Fireball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.lang.Thread.yield


object ChatListener : Listener {
	@EventHandler
	fun onCommandPreprocess(event: PlayerCommandPreprocessEvent) {
		if (event.message.contains("e rename ", true)) event.message =
			event.message.replace("e rename ", "e rename &f", true)
	}
}