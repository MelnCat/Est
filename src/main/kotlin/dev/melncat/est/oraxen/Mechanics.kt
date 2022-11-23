package dev.melncat.est.oraxen

import dev.melncat.est.plugin
import dev.melncat.est.util.ARMOR_EFFECT_KEY
import dev.melncat.est.util.CUSTOM_ATTRIBUTE_KEY
import dev.melncat.est.util.CustomAttributeModifier
import dev.melncat.est.util.CustomAttributeOperation
import dev.melncat.est.util.CustomAttributeRegistry
import dev.melncat.est.util.matchPDataType
import io.th0rgal.oraxen.items.ItemBuilder
import io.th0rgal.oraxen.mechanics.Mechanic
import io.th0rgal.oraxen.mechanics.MechanicFactory
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.function.Function


class AttributeMechanic(
	mechanicFactory: MechanicFactory,
	section: ConfigurationSection
) :
	Mechanic(mechanicFactory, section, { item ->
		val modifiers = section.getMapList("modifiers")
			.map {
				CustomAttributeModifier(
					CustomAttributeRegistry[it["type"]]!!,
					CustomAttributeOperation.valueOf(it["operation"] as String),
					(it["amount"] as Number).toDouble()
				)
			}.toTypedArray()
		item.addCustomTag(NamespacedKey(plugin, CUSTOM_ATTRIBUTE_KEY), matchPDataType(Array<CustomAttributeModifier>::class), modifiers)
		modifiers.map {
			it.type.add(item, it)
		}
		item
	})

class AttributeMechanicFactory(section: ConfigurationSection) : MechanicFactory(section) {
	override fun parse(config: ConfigurationSection): Mechanic {
		val mechanic = AttributeMechanic(this, config)
		addToImplemented(mechanic)
		return mechanic
	}
}

class ItemEffectMechanic(
	mechanicFactory: MechanicFactory,
	section: ConfigurationSection
) :
	Mechanic(mechanicFactory, section, { item ->
		val effects = section.getMapList("effects")
			.map {
				PotionEffect(PotionEffectType.getByName(it["type"] as String)!!,
					if (it.containsKey("duration")) (it["duration"] as Number).toInt() else 10,
					if (it.containsKey("amplifier")) (it["amplifier"] as Number).toInt() else 0
				)
			}.toTypedArray()
		item.addCustomTag(NamespacedKey(plugin, ARMOR_EFFECT_KEY), matchPDataType(Array<PotionEffect>::class), effects)
		item
	})

class ItemEffectMechanicFactory(section: ConfigurationSection) : MechanicFactory(section) {
	override fun parse(config: ConfigurationSection): Mechanic {
		val mechanic = ItemEffectMechanic(this, config)
		addToImplemented(mechanic)
		return mechanic
	}
}