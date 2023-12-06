package dev.melncat.est.command

import cloud.commandframework.arguments.standard.BooleanArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.kotlin.extension.commandBuilder
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.isAir
import dev.melncat.est.util.set
import dev.melncat.est.weaponart.WeaponArtRegistry
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeMap
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_19_R2.attribute.CraftAttributeInstance
import org.bukkit.entity.Player

@RegisterCommand
object WeaponArtCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		val base = manager.commandBuilder("weaponart", aliases = arrayOf("wa")) {
			permission = "est.command.weaponart"
			senderType<Player>()
		}
		base.registerCopy("override") {
			permission += ".override"
			argument(StringArgument.builder<CommandSender>("art").withSuggestionsProvider { _, _ ->
				WeaponArtRegistry.ids.toList()
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
					?: return@handler player.sendMessage("<red>Invalid weapon art specified.".mm())
				item.editMeta {
					it.persistentDataContainer.set(EstKey.weaponArtOverride, artId)
					it.persistentDataContainer.set(EstKey.weaponArtDestroyOnUse, ctx.get<Boolean>("destroyOnUse"))
				}
			}
		}
	}
}