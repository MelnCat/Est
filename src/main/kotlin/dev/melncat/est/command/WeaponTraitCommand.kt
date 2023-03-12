package dev.melncat.est.command

import cloud.commandframework.arguments.standard.BooleanArgument
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.kotlin.extension.commandBuilder
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.plugin
import dev.melncat.est.trait.Traits
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.get
import dev.melncat.est.util.isAir
import dev.melncat.est.util.set
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer

@RegisterCommand
object WeaponTraitCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		val base = manager.commandBuilder("weapontrait", aliases = arrayOf("wt")) {
			permission = "est.command.weapontrait"
			senderType<Player>()
		}
		base.registerCopy("override") {
			permission += ".override"
			argument(StringArgument.newBuilder<CommandSender>("trait").withSuggestionsProvider { _, _ ->
				Traits.traitRegistry.keys.toList()
			})
			argument(IntegerArgument.newBuilder<CommandSender>("level").withSuggestionsProvider { ctx, _ ->
				Traits.traitRegistry[ctx.getOrDefault("trait", "")].let { t ->
					if (t != null) (1..t.maxLevel).map { it.toString() }.toList() else emptyList()
				}
			}.asOptionalWithDefault("1"))
			argument(BooleanArgument.optional("destroyOnUse", false))
			handler { ctx ->
				val player = ctx.sender as Player
				val traitId = ctx.get<String>("trait")
				val item = player.inventory.itemInMainHand
				if (item.isAir) {
					player.sendMessage("<red>You must be holding an item.".mm())
					return@handler
				}
				val trait = Traits.traitRegistry[traitId]
					?: return@handler player.sendMessage("<red>Invalid trait specified.".mm())
				val level = ctx.get<Int>("level")
				if (level < 0 || level > trait.maxLevel)
					return@handler player.sendMessage("<red>Invalid level.".mm())
				item.editMeta {
					if (!it.persistentDataContainer.has(EstKey.traitOverride))
						it.persistentDataContainer.set(
							EstKey.traitOverride,
							it.persistentDataContainer.adapterContext.newPersistentDataContainer()
						)
					val container = it.persistentDataContainer.get<PersistentDataContainer>(EstKey.traitOverride)!!
					if (level > 0) container.set(NamespacedKey(plugin, trait.id), level)
					else container.remove(NamespacedKey(plugin, trait.id))
					it.persistentDataContainer.set(EstKey.traitOverride, container)
				}
			}
		}
	}
}