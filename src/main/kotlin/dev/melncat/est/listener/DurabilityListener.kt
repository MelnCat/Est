package dev.melncat.est.listener

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material.*
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot
import xyz.xenondevs.nova.util.item.DamageableUtils
import java.util.UUID

@RegisterListener
object DurabilityListener : FListener {
	@EventHandler
	fun onItemDamage(event: PlayerItemDamageEvent) {
		val remaining = DamageableUtils.getMaxDurability(event.item) - event.damage
		if (remaining <= 10)
			event.player.sendMessage("<red>Warning! Your <yellow><0></yellow> only has <yellow><1></yellow> durability left!"
				.mm(Component.translatable(event.item), remaining))
	}

	private val armorDurabilityMap = HashBasedTable.create<UUID, EquipmentSlot, Int>()
	@EventHandler
	fun onArmorDamage(event: PlayerItemDamageEvent) {
		val curTick = Bukkit.getServer().currentTick
		if (!event.item.type.isArmor) return
		val slot = event.item.type.equipmentSlot
		event.damage = 1
		val map = armorDurabilityMap[event.player.uniqueId, slot]
		if (map == null || curTick >= map) {
			armorDurabilityMap.put(event.player.uniqueId, slot, curTick + 30)
		} else if (curTick < map) {
			event.isCancelled = true
			event.damage = 0
		}
	}
}