

package dev.melncat.est.listener

import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.component
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.world.inventory.AnvilMenu
import org.bukkit.GameMode.CREATIVE
import org.bukkit.Material.*
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftInventoryView
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftMetaArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.LOW
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import xyz.xenondevs.nova.util.item.unhandledTags

@RegisterListener
object ProtectBlockListener : FListener {
	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		if (event.player.gameMode != CREATIVE && event.block.type == SPAWNER) event.isCancelled = true
	}
	@EventHandler
	fun on(event: BlockExplodeEvent) {
		event.blockList().removeIf { it.type == SPAWNER }
	}
	@EventHandler
	fun on(event: EntityExplodeEvent) {
		event.blockList().removeIf { it.type == SPAWNER }
	}
}