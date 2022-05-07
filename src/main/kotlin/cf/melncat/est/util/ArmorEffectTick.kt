package cf.melncat.est.util

import cf.melncat.est.plugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect

const val ARMOR_EFFECT_KEY = "armor_effects"

fun armorEffectTick() {
	for (player in plugin.server.onlinePlayers) {
		arrayOf(player.inventory.itemInMainHand, player.inventory.itemInOffHand).forEach {
			applyArmorEffect(
				player,
				it
			)
		}
		player.inventory.armorContents?.forEach {
			if (it !== null) applyArmorEffect(
				player,
				it, true
			)
		}
	}
}

private fun applyArmorEffect(player: Player, item: ItemStack, armor: Boolean = false) {
	if (!item.hasItemMeta()) return
	if (!item.itemMeta.pd.has<Array<PDC>>(ARMOR_EFFECT_KEY)) return
	if (item.type.isEdible || item.type === Material.MILK_BUCKET || item.type == Material.POTION) return
	if (item.type.isArmor && !armor) return
	val effects = item.persistentDataContainer.get<Array<PotionEffect>>(ARMOR_EFFECT_KEY) ?: return
	for (effect in effects) player.addPotionEffect(effect)
}