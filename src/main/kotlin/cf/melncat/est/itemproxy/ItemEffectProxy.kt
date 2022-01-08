package cf.melncat.est.itemproxy

import cf.melncat.est.util.ARMOR_EFFECT_KEY
import cf.melncat.est.util.TD
import cf.melncat.est.util.component
import cf.melncat.est.util.get
import cf.melncat.est.util.has
import cf.melncat.est.util.pd
import net.kyori.adventure.text.format.TextColor
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.Locale

@RegisterItemProxy
object ItemEffectProxy : ItemProxy {
	override fun invoke(item: ItemStack): ItemStack {
		if (!item.hasItemMeta() || !item.itemMeta.pd.has<Array<PotionEffect>>(ARMOR_EFFECT_KEY)) return item
		val effects = item.itemMeta.pd.get<Array<PotionEffect>>(ARMOR_EFFECT_KEY) ?: return item
		val newLore =
			effects.map {
				"${effectName(it.type)} ${roman(it.amplifier + 1)}".component(TextColor.color(it.type.color.asRGB()))
					.decoration(TD.ITALIC, false)
			}
		if (item.hasLore()) item.lore(item.lore()!! + newLore)
		else item.lore(newLore)
		return item
	}
}

private fun effectName(effectType: PotionEffectType) = when (effectType) {
	PotionEffectType.SPEED -> "Speed"
	PotionEffectType.DAMAGE_RESISTANCE -> "Resistance"
	PotionEffectType.DOLPHINS_GRACE -> "Dolphin's Grace"
	PotionEffectType.FAST_DIGGING -> "Haste"
	PotionEffectType.HARM -> "Instant Damage"
	PotionEffectType.HEAL -> "Instant Health"
	PotionEffectType.INCREASE_DAMAGE -> "Strength"
	PotionEffectType.JUMP -> "Jump Boost"
	PotionEffectType.CONFUSION -> "Nausea"
	PotionEffectType.SLOW -> "Slowness"
	PotionEffectType.SLOW_DIGGING -> "Mining Fatigue"
	else -> effectType.name.split("_")
		.joinToString(" ") { s -> s.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) } }
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