package dev.melncat.est.trait

import dev.melncat.est.util.toKey
import net.kyori.adventure.key.Key
import net.minecraft.world.item.Items.SHIELD
import org.bukkit.Material
import xyz.xenondevs.nova.material.NovaMaterialRegistry
import java.math.BigInteger.TWO

object WeaponTraits {
	val registry = mutableMapOf<Key, List<TraitInstance<*>>>()
	val hidden = mutableMapOf<Key, List<Trait<*>>>()
	val materials = mutableMapOf<String, Trait<*>>()

	private fun addWeaponType(weaponType: String, traits: Map<Trait<*>, Int>, hidden: List<Trait<*>> = listOf()) {
		val weapons = NovaMaterialRegistry.values.filter { it.id.name.endsWith("_$weaponType") }
		weapons.forEach { registry[it.id.toKey()] = traits.map { v -> TraitInstance(v.key, v.value) } }
		val vanilla = Material.values().filter { it.key().value().endsWith("_$weaponType") }
		vanilla.forEach { registry[it.key()] = traits.map { v -> TraitInstance(v.key, v.value) } }
	}

	private fun addMaterialType(materialType: String, trait: Trait<*>) {
		materials[materialType] = trait
	}

	init {
		with(Traits) {
			addWeaponType(
				"sword", mapOf(
					SWEEPING to 1,
					MOMENTUM to 1
				)
			)

			addWeaponType(
				"axe", mapOf(
					RECKLESS to 1,
					MOMENTUM to 1
				)
			)

			addWeaponType(
				"dagger", mapOf(
					CHEST_STRIKE to 1,
					TWINNED to 3,
					DISHONORABLE to 1
				)
			)

			addWeaponType(
				"gladius", mapOf(
					RECKLESS to 1,
					SWEEPING to 1,
					MOMENTUM to 1
				)
			)

			addWeaponType(
				"khopesh", mapOf(
					SHIELD_BREAK to 1,
					SWEEPING to 1,
					HOOKING to 1,
					DECAPITATION to 1
				), listOf(DECAPITATION)
			)

			addWeaponType(
				"rapier", mapOf(
					THRUST to 1,
					DUELIST to 2,
					FLIMSY to 2
				)
			)

			addWeaponType(
				"spear", mapOf(
					THRUST to 1,
					CHEST_STRIKE to 1,
					TWINNED to 1
				), listOf(TWINNED)
			)

			addWeaponType(
				"saber", mapOf(
					MOUNTED to 1,
					DUELIST to 1,
					CHEST_STRIKE to 1,
					DECAPITATION to 1,
					DISHONORABLE to 1
				), listOf(DISHONORABLE)
			)

			addWeaponType(
				"katana", mapOf(
					SWEEPING to 3,
					DECAPITATION to 1,
					TWO_HANDED to 1,
					FLIMSY to 1,
					COMBO_FIEND to 1
				), listOf(COMBO_FIEND)
			)

			addWeaponType(
				"scimitar", mapOf(
					SWEEPING to 2,
					TWINNED to 1,
					DECAPITATION to 1
				)
			)

			addWeaponType(
				"cutlass", mapOf(
					DISHONORABLE to 1,
					CHEST_STRIKE to 1,
					MOUNTED to 2
				), listOf(MOUNTED)
			)

			addWeaponType(
				"kukri", mapOf(
					THROWABLE to 1,
					SWEEPING to 1
				)
			)

			addWeaponType(
				"longsword", mapOf(
					MOMENTUM to 1,
					SWEEPING to 1,
					TWO_HANDED to 2
				)
			)

			addWeaponType(
				"trident", mapOf(
					THRUST to 1,
					CHEST_STRIKE to 1,
					HOOKING to 1,
					PIERCING to 1,
					MOUNTED to 1
				), listOf(MOUNTED)
			)

			addWeaponType(
				"scythe", mapOf(
					SWEEPING to 4,
					MOMENTUM to 1,
					TWO_HANDED to 1,
					DECAPITATION to 1
				), listOf(DECAPITATION)
			)

			addWeaponType(
				"glaive", mapOf(
					MOMENTUM to 1,
					SWEEPING to 2,
					TWO_HANDED to 1,
					THRUST to 1
				), listOf(THRUST)
			)

			addWeaponType(
				"sverd", mapOf(
					RECKLESS to 1,
					DISHONORABLE to 1,
					FLIMSY to 2
				)
			)

			addWeaponType(
				"hook_sword", mapOf(
					HOOKING to 1,
					DISHONORABLE to 1,
					FLIMSY to 1
				)
			)

			addWeaponType(
				"kris_dagger", mapOf(
					SERRATED to 3,
					FLIMSY to 4
				)
			)

			addWeaponType(
				"elvish_sword", mapOf(
					COMBO_FIEND to 1,
					MOMENTUM to 2,
					SWEEPING to 1
				)
			)

			addWeaponType(
				"shashka", mapOf(
					CHEST_STRIKE to 1,
					SWEEPING to 1,
					RECKLESS to 1
				)
			)

			addWeaponType(
				"greatsword", mapOf(
					PIERCING to 1,
					DISHONORABLE to 1,
					TWO_HANDED to 3,
					DECAPITATION to 1
				), listOf(DECAPITATION)
			)

			addWeaponType(
				"sai", mapOf(
					HOOKING to 1,
					PIERCING to 1,
					SHIELD_BREAK to 1
				)
			)

			addWeaponType(
				"battleaxe", mapOf(
					SHIELD_BREACH to 1,
					SHIELD_BREAK to 1,
					SWEEPING to 1
				)
			)

			addWeaponType(
				"greataxe", mapOf(
					SHIELD_BREACH to 1,
					SHIELD_BREAK to 1,
					PIERCING to 1,
					TWO_HANDED to 3
				)
			)

			addWeaponType(
				"mace", mapOf(
					SHIELD_BREAK to 1,
					MOMENTUM to 1,
					CHEST_STRIKE to 1
				)
			)

			addWeaponType(
				"warhammer", mapOf(
					SHIELD_BREAK to 1,
					MOMENTUM to 2,
					TWO_HANDED to 4,
					RECKLESS to 1
				), listOf(RECKLESS)
			)

			addWeaponType(
				"halberd", mapOf(
					PIERCING to 2,
					SWEEPING to 4,
					TWO_HANDED to 4
				)
			)

			addWeaponType(
				"naginata", mapOf(
					SWEEPING to 1,
					RECKLESS to 1,
					THRUST to 1,
					TWO_HANDED to 1
				)
			)

			addWeaponType(
				"fascina", mapOf(
					PIERCING to 1,
					RECKLESS to 1,
					THRUST to 1,
					HOOKING to 1
				), listOf(HOOKING)
			)

			addWeaponType(
				"dwarven_greataxe", mapOf(
					SHIELD_BREAK to 1,
					RECKLESS to 1,
					DECAPITATION to 1,
					TWO_HANDED to 3
				)
			)

			addWeaponType(
				"goblin_axe", mapOf(
					SHIELD_BREAK to 1,
					SWEEPING to 1,
					RECKLESS to 1
				)
			)

			addWeaponType(
				"shortsword", mapOf(
					CHEST_STRIKE to 1,
					TWINNED to 2,
					RECKLESS to 1
				)
			)

			addMaterialType("wooden", SPLINTERING)
			addMaterialType("golden", SOFT)
			addMaterialType("stone", HEAVY)
			addMaterialType("nickel", POISONOUS)
			addMaterialType("tin", FRACTURING)
			addMaterialType("copper", CONDUCTIVE)
			addMaterialType("bronze", BALANCED)
			addMaterialType("iron", RUSTING)
			addMaterialType("invar", PRECISE)
			addMaterialType("electrum", ELECTROCUTION)
			addMaterialType("amethyst", PURIFYING)
			addMaterialType("silver", HOLY)
			addMaterialType("emerald", WEALTHY)
			addMaterialType("platinum", IMPERVIOUS)
			addMaterialType("steel", TEMPERED)
			addMaterialType("cobalt", SWIFT)
			addMaterialType("diamond", PRISMATIC)
			addMaterialType("hard carbon", LIGHTWEIGHT)
			addMaterialType("netherite", ANCIENT)
			addMaterialType("mithril", FEATHERWEIGHT)
		}
	}
}