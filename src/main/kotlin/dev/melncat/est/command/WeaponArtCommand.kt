package dev.melncat.est.command

import cloud.commandframework.arguments.standard.BooleanArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.kotlin.extension.commandBuilder
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.isAir
import dev.melncat.est.util.meta
import dev.melncat.est.util.set
import dev.melncat.est.util.usageError
import dev.melncat.est.weaponarts.ActiveWeaponArts
import dev.melncat.est.weaponarts.WeaponArtRegistry
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.BundleMeta

@RegisterCommand
object WeaponArtCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		val base = manager.commandBuilder("weaponart", aliases = arrayOf("wa")) {
			permission = "est.command.weaponart"
			senderType<Player>()
		}
		base.registerCopy("override") {
			permission += ".override"
			argument(StringArgument.newBuilder<CommandSender>("art").withSuggestionsProvider {
				_, _ -> WeaponArtRegistry.ids.toList()
			})
			argument(BooleanArgument.optional("destroyOnUse", false))
			handler { ctx ->
				val player = ctx.sender as Player
				val artId = ctx.get<String>("art")
				val item = player.inventory.itemInMainHand
				if (item.isAir) {
					player.sendMessage("<red>You must be holding an item.".mm())
					return@handler
				}
				val art = WeaponArtRegistry.fromId(artId)
				if (art == null) {
					player.sendMessage("<red>Invalid weapon art specified.".mm())
				}
				item.editMeta {
					it.persistentDataContainer.set(EstKey.weaponArtOverride, artId)
					it.persistentDataContainer.set(EstKey.weaponArtDestroyOnUse, ctx.get<Boolean>("destroyOnUse"))
				}
			}
		}
	}
}