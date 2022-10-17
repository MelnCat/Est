package dev.melncat.est.command

import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.bukkit.argument.NamespacedKeyArgument
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.util.ARMOR_EFFECT_KEY
import dev.melncat.est.util.get
import dev.melncat.est.util.has
import dev.melncat.est.util.meta
import dev.melncat.est.util.pd
import dev.melncat.est.util.set
import dev.melncat.est.util.usageError
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import org.bukkit.Registry
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffect

@RegisterCommand
object ItemEffectCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("ief") {
			permission = "est.command.ief"
			senderType<Player>()
			argument(NamespacedKeyArgument.builder<CommandSender>("effect")
				.withSuggestionsProvider { _, _ -> Registry.POTION_EFFECT_TYPE.map { it.key.toString() } })
			argument(
				IntegerArgument.newBuilder<CommandSender>("amplifier")
					.withMin(0).withMax(255).asOptionalWithDefault(0)
			)
			argument(
				IntegerArgument.newBuilder<CommandSender>("time")
					.withMin(0).asOptionalWithDefault(10)
			)
			handler { ctx ->
				val player = ctx.sender as Player
				val item = player.inventory.itemInMainHand
				if (item.amount <= 0) {
					player.usageError("You must be holding an item.")
					return@handler
				}
				val type = Registry.POTION_EFFECT_TYPE.get(ctx.get("effect"))!!
				val amplifier = ctx.get<Int>("amplifier")
				val time = ctx.get<Int>("time")
				item.meta<ItemMeta> {
					if (!pd.has<Array<PotionEffect>>(ARMOR_EFFECT_KEY)) pd.set<Array<PotionEffect>>(
						ARMOR_EFFECT_KEY,
						arrayOf()
					)
					val effects = pd.get<Array<PotionEffect>>(ARMOR_EFFECT_KEY)!!
						.filter { it.type !== type }.toTypedArray()
					if (amplifier == -1) pd.set(ARMOR_EFFECT_KEY, effects)
					else pd.set(
						ARMOR_EFFECT_KEY,
						effects + PotionEffect(type, time, amplifier, true)
					)
				}
			}
		}
	}
}