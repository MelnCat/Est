package dev.melncat.est.listener

import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemDamageEvent

@RegisterListener
object DurabilityListener : FListener {
	@EventHandler
	fun onItemDamage(event: PlayerItemDamageEvent) {
		val remaining = event.item.type.maxDurability - event.damage
		if (remaining <= 10)
			event.player.sendMessage("<red>Warning! Your <yellow><0></yellow> only has <yellow><1></yellow> durability left!"
				.mm(Component.translatable(event.item), remaining))
	}
}