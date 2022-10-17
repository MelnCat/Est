package dev.melncat.est.listener

import dev.melncat.est.util.ARMOR_EFFECT_KEY
import dev.melncat.est.util.get
import dev.melncat.est.util.has
import dev.melncat.est.util.pd
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.TD
import dev.melncat.furcation.util.component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.potion.PotionEffect
import org.purpurmc.purpur.event.packet.NetworkItemSerializeEvent
import org.purpurmc.purpur.language.Language

@RegisterListener
object ItemEffectListener : FListener {
	@EventHandler
	fun onSerialize(event: NetworkItemSerializeEvent) {
		val item = event.itemStack
		if (!item.hasItemMeta() || !item.itemMeta.pd.has<Array<PotionEffect>>(ARMOR_EFFECT_KEY)) return
		val effects = item.itemMeta.pd.get<Array<PotionEffect>>(ARMOR_EFFECT_KEY) ?: return
		val newLore =
			effects.map {
				"${Language.getLanguage().getOrDefault(it.type)} ${roman(it.amplifier + 1)}".component(TextColor.color(it.type.color.asRGB()))
					.decoration(TD.ITALIC, false)
			}
		if (item.hasLore()) item.lore(item.lore()!! + newLore)
		else item.lore(newLore)
		event.setItemStack(item)
	}
}

private val numbers = linkedMapOf(
	1000 to "M",
	900 to "CM",
	500 to "D",
	400 to "CD",
	100 to "C",
	90 to "XC",
	50 to "L",
	40 to "XL",
	10 to "X",
	9 to "IX",
	5 to "V",
	4 to "IV",
	1 to "I"
)

private fun roman(number: Int): String {
	for (i in numbers.keys) {
		if (number >= i) {
			return numbers[i] + roman(number - i)
		}
	}
	return ""
}