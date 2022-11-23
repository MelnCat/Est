package dev.melncat.est.command

import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.util.isAir
import dev.melncat.est.util.meta
import dev.melncat.est.util.usageError
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.BundleMeta

@RegisterCommand
object ToBundleCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("tobundle") {
			permission = "est.command.tobundle"
			senderType<Player>()
			handler { ctx ->
				val player = ctx.sender as Player
				val item = player.inventory.itemInMainHand
				if (item.amount <= 0) {
					player.usageError("You must be holding an item.")
					return@handler
				}
				val meta = item.itemMeta
				if (meta !is BlockStateMeta || meta.blockState !is Container) {
					player.usageError("The item you are holding must be a container.")
					return@handler
				}
				val bundle = ItemStack(Material.BUNDLE).meta<BundleMeta> {
					val contents = (meta.blockState as? Container)?.inventory?.contents?.asList()
					if (contents != null) setItems(contents.filter { !it.isAir })
				}
				player.inventory.setItemInMainHand(bundle)
			}
		}
	}
}