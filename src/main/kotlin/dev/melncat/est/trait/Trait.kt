package dev.melncat.est.trait

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.get
import dev.melncat.est.util.isAir
import dev.melncat.est.util.itemKey
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import xyz.xenondevs.nova.material.NovaMaterialRegistry
import java.util.UUID

open class Trait<T>(
	val id: String,
	val displayName: String,
	val description: (effect: T) -> String,
	val calculate: (level: Int) -> T,
	val maxLevel: Int = 1,
) {
	var onDamage: ((event: EntityDamageByEntityEvent, player: Player, value: T) -> Unit)? = null
	var onKill: ((event: EntityDeathEvent, player: Player, value: T) -> Unit)? = null
	var onKnockback: ((event: EntityKnockbackByEntityEvent, player: Player, value: T) -> Unit)? = null

	override fun hashCode() = id.hashCode()
	override fun equals(other: Any?) = other is Trait<*> && other.id == id

	fun setDamageCallback(cb: (event: EntityDamageByEntityEvent, player: Player, value: T) -> Unit): Trait<T> {
		onDamage = cb
		return this
	}
	fun setKillCallback(cb: (event: EntityDeathEvent, player: Player, value: T) -> Unit): Trait<T> {
		onKill = cb
		return this
	}
	fun setKnockbackCallback(cb: (event: EntityKnockbackByEntityEvent, player: Player, value: T) -> Unit): Trait<T> {
		onKnockback = cb
		return this
	}
}

class SingleTrait(
	id: String, displayName: String, description: String
): Trait<Unit>(id, displayName, { description }, { }, 1)

class AttributeTrait(
	id: String,
	displayName: String,
	description: (effect: Double) -> String,
	calculate: (level: Int) -> Double,
	maxLevel: Int = 1,
	val attributeType: Attribute,
	val operation: AttributeModifier.Operation,
	val condition: (player: Player, item: ItemStack) -> Boolean,
	val attributeId: UUID = UUID.nameUUIDFromBytes(id.toByteArray())
) : Trait<Double>(
	id, displayName,
	description, calculate, maxLevel
) {
	private fun AttributeMap.getOrCreateInstance(attribute: Attribute) =
		getInstance(attribute) ?: registerAttribute(attribute).let { getInstance(attribute)!! }
	private val attributeCache = mutableMapOf<Int, AttributeModifier>()
	fun getAttribute(level: Int) =
		attributeCache[level] ?: AttributeModifier(attributeId, id, calculate(level), operation).also { attributeCache[level] = it }
	fun checkPlayer(player: Player, item: ItemStack) {
		val attributes = (player as CraftPlayer).handle.attributes
		val traits = getEffectiveTraits(item)
		if (traits.containsKey(this)) {
			if (condition(player, item)) {
				val modifier = getAttribute(traits[this]!!)
				val inst = attributes.getOrCreateInstance(attributeType)
				if (inst.getModifier(attributeId) == null) inst.addTransientModifier(modifier)
			} else {
				val inst = attributes.getInstance(attributeType)
				if (inst?.getModifier(attributeId) != null) inst.removeModifier(attributeId)
			}
		} else {
			val inst = attributes.getInstance(attributeType)
			if (inst?.getModifier(attributeId) != null) inst.removeModifier(attributeId)
		}
	}
}

fun getEffectiveTraits(item: ItemStack): Map<Trait<*>, Int> {
	if (item.persistentDataContainer.has(EstKey.traitOverride)) {
		val container = item.persistentDataContainer.get<PersistentDataContainer>(EstKey.traitOverride)!!
		return container.keys.associate { Traits.traitRegistry[it.key]!! to container.get(it)!! }
	}
	val key = item.itemKey
	return WeaponTraits.registry[key] ?: mapOf()
}

fun getShownTraits(item: ItemStack) = item.itemKey.let { k ->
	val eff = getEffectiveTraits(item)
	val hidden = WeaponTraits.hidden[k]
	eff.filter { hidden == null || !hidden.contains(it.key) }
}