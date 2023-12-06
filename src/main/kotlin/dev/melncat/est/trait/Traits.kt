@file:Suppress("DEPRECATION")

package dev.melncat.est.trait

import dev.melncat.est.listener.attacksSinceMiss
import dev.melncat.est.util.isAir
import dev.melncat.est.util.itemKey
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL
import net.minecraft.world.entity.ai.attributes.Attributes
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.inventory.ItemStack
import org.bukkit.attribute.Attributable
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.Attribute.GENERIC_ARMOR
import org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH
import org.bukkit.entity.EntityCategory
import org.bukkit.entity.EntityType.CREEPER
import org.bukkit.entity.EntityType.ENDER_DRAGON
import org.bukkit.entity.EntityType.PLAYER
import org.bukkit.entity.EntityType.SKELETON
import org.bukkit.entity.EntityType.WITHER
import org.bukkit.entity.EntityType.WITHER_SKELETON
import org.bukkit.entity.EntityType.ZOMBIE
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.ARMOR
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BLOCKING
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.xenondevs.nova.util.item.DamageableUtils
import xyz.xenondevs.nova.util.plus
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object Traits {
	val traitRegistry = mutableMapOf<String, Trait<*>>()

	fun <T> Trait<T>.register() = also { traitRegistry[id] = this }

	val TWO_HANDED = AttributeTrait(
		"two_handed", "Two-Handed",
		{ "Your attack speed is decreased by ${-it * 100}% while you have any item in your offhand." },
		{
			when (it) {
				1 -> -0.3
				2 -> -0.5
				3 -> -0.8
				4 -> -1.0
				else -> 0.0
			}
		}, 4,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ p, _ -> !p.inventory.itemInOffHand.isAir }
	).register()

	val DUELIST = Trait(
		"duelist", "Duelist",
		{ "You deal +${it * 100}% damage when you have no armor." },
		{
			when (it) {
				1 -> 0.5
				2 -> 0.75
				3 -> 1.0
				4 -> 1.5
				else -> 0.0
			}
		}, 4
	).setDamageCallback { event, player, value ->
		if ((player.getAttribute(GENERIC_ARMOR)?.value ?: 0.0) <= 0)
			event.damage *= (value + 1)
	}.register()

	val RECKLESS = SingleTrait(
		"reckless", "Reckless",
		"You deal +100% damage when you have less than 10% health."
	).setDamageCallback { event, player, _ ->
		val maxHealth = player.getAttribute(GENERIC_MAX_HEALTH)?.value ?: 20.0
		if (player.health <= (maxHealth / 10))
			event.damage *= 2
	}.register()

	val SHIELD_BREACH = Trait(
		"shield_breach", "Shield Breach",
		{ "You deal +${it * 100}% damage through shields." },
		{
			when (it) {
				1 -> 0.25
				2 -> 0.5
				3 -> 0.75
				4 -> 1.0
				else -> 0.0
			}
		}, 4
	).setDamageCallback { event, _, l ->
		if (event.getDamage(BLOCKING) != 0.0) event.setDamage(BLOCKING, event.getDamage(BLOCKING) * l)
	}.register()

	val DISHONORABLE = SingleTrait(
		"dishonorable", "Dishonorable",
		"You deal +100% damage to unarmored enemies."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is Attributable && (entity.getAttribute(GENERIC_ARMOR)?.value ?: 0.0) == 0.0)
			event.damage *= 2
	}.register()

	val CHEST_STRIKE = SingleTrait(
		"chest_strike", "Chest Strike",
		"You deal +50% damage to enemies without a chestplate."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is LivingEntity && entity.equipment?.chestplate.isAir)
			event.damage *= 1.5
	}.register()

	val COMBO_FIEND = SingleTrait(
		"combo_fiend", "Combo Fiend",
		"You deal +1 damage with every attack until a miss, capped at +6."
	).setDamageCallback { event, player, _ ->
		val increase = min(6, attacksSinceMiss[player.uniqueId] ?: 0)
		if (increase > 0) event.damage += increase
	}.register()

	val MOMENTUM = AttributeTrait(
		"momentum", "Momentum",
		{ "Your attack speed is increased by ${it * 100}% after the first attack." },
		{
			when (it) {
				1 -> 0.1
				2 -> 0.25
				else -> 0.0
			}
		}, 2,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ p, _ -> (attacksSinceMiss[p.uniqueId] ?: 0) > 0 }
	).register()

	val TWINNED = AttributeTrait(
		"twinned", "Twinned",
		{ "You gain +${it * 100}% attack speed when wielding the same weapon in both your main and offhand." },
		{
			when (it) {
				1 -> 0.35
				2 -> 0.5
				3 -> 0.75
				else -> 0.0
			}
		}, 3,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ p, i -> p.inventory.itemInOffHand.itemKey == i.itemKey }
	).register()

	val SERRATED = Trait(
		"serrated", "Serrated",
		{ "You inflict Wither II for ${it.duration / 20} seconds when hitting enemies with incomplete armor." },
		{
			PotionEffect(
				PotionEffectType.WITHER, when (it) {
					1 -> 3 * 20
					2 -> 6 * 20
					3 -> 10 * 20
					else -> 0
				}, 1, true, true, false)
		}, 4
	).setDamageCallback { event, _, value ->
		val entity = event.entity
		if (entity is LivingEntity && (entity.equipment == null || entity.equipment!!.armorContents.any { it.isAir }))
			entity.addPotionEffect(value)
	}.register()

	val SHIELD_BREAK = SingleTrait(
		"shield_break", "Shield Break",
		"Your sword can break shields."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is LivingEntity) entity.playEffect(EntityEffect.SHIELD_BREAK)
		if (entity is HumanEntity) entity.setCooldown(Material.SHIELD, 5 * 20)
	}.register()

	val THRUST = SingleTrait(
		"thrust", "Thrust",
		"Your attacks send you forward based on the knockback the enemy takes."
	).setKnockbackCallback { event, player, _ ->
		player.velocity = player.velocity.add(event.acceleration).apply { y *= 0.2 }
	}.register()

	val SWEEPING = Trait(
		"sweeping", "Sweeping",
		{ "Your attacks deal ${it * 100}% damage in a sweeping motion." },
		{
			when (it) {
				1 -> 0.2
				2 -> 0.4
				3 -> 0.6
				4 -> 0.8
				else -> 0.0
			}
		}, 10
	).setDamageCallback { event, player, value ->
		if (event.cause == ENTITY_SWEEP_ATTACK) {
			event.damage += (player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0) * value - 1
		}
	}.register()

	val MOUNTED = Trait(
		"mounted", "Mounted",
		{ "You deal +${it * 100}% damage when mounted." },
		{
			when (it) {
				1 -> 0.3
				2 -> 0.6
				else -> 0.0
			}
		}, 2
	).setDamageCallback { event, player, value ->
		if (player.vehicle != null)
			event.damage *= (value + 1)
	}.register()

	val THROWABLE = SingleTrait(
		"throwable", "Throwable",
		"You can throw your weapon by right clicking, dealing the same damage as a normal attack."
	).register()

	val FLIMSY = Trait(
		"flimsy", "Flimsy",
		{ "You deal ${-it * 100}% less damage to armored enemies." },
		{
			when (it) {
				1 -> -0.25
				2 -> -0.5
				3 -> -0.75
				4 -> -1.0
				else -> 0.0
			}
		}, 4
	).setDamageCallback { event, _, value ->
		val entity = event.entity
		if (entity is Attributable && (entity.getAttribute(GENERIC_ARMOR)?.value ?: 0.0) > 0.0)
			event.damage *= (value + 1)
	}.register()

	val HOOKING = SingleTrait(
		"hooking", "Hooking",
		"Your attacks pull enemies towards you."
	).setKnockbackCallback { event, _, _ ->
		event.acceleration.multiply(-1)
	}.register()

	val PIERCING = Trait(
		"piercing", "Piercing",
		{ "Your attacks deal ${it * 100}% damage through armor." },
		{
			when (it) {
				1 -> 0.2
				2 -> 0.3
				3 -> 0.4
				4 -> 0.5
				else -> 0.0
			}
		}, 4
	).setDamageCallback { event, _, value ->
		event.setDamage(ARMOR, event.getDamage(ARMOR) * (1 - value))
	}.register()

	val DECAPITATION = SingleTrait(
		"decapitation", "Decapitation",
		"Your attacks deal 65% more damage to enemies without helmets, and have a chance to behead enemies."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is LivingEntity && entity.equipment?.helmet.isAir)
			event.damage *= 1.65
	}.setKillCallback { event, _, _ ->
		if (Random.nextInt(
				if (event.entity.type == WITHER_SKELETON || event.entity.type == WITHER) 7
				else 75
		) == 0) {
			val item = when (event.entity.type) {
				SKELETON -> ItemStack(SKELETON_SKULL)
				WITHER, WITHER_SKELETON -> ItemStack(WITHER_SKELETON_SKULL)
				ZOMBIE -> ItemStack(ZOMBIE_HEAD)
				CREEPER -> ItemStack(CREEPER_HEAD)
				ENDER_DRAGON -> ItemStack(DRAGON_HEAD)
				PLAYER -> ItemStack(PLAYER_HEAD).apply {
					editMeta(SkullMeta::class.java) {
						it.owningPlayer = event.entity as? Player
					}
				}
				else -> null
			}
			event.drops.add(item)
		}
	}.register()

	val SPLINTERING = SingleTrait(
		"splintering", "Splintering",
		"Your attacks have a 25% chance to give Wither I."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is LivingEntity && Random.nextInt(4) == 0)
			entity.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 30, 0, true, true, false))
	}.register()

	val SOFT = SingleTrait(
		"soft", "Soft",
		"You deal 25% less damage half the time."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is LivingEntity && Random.nextInt(2) == 0)
			event.damage *= 0.75;
	}.register()

	val HEAVY = AttributeTrait(
		"heavy", "Heavy",
		{ "You deal 25% more knockback, but attack 10% slower." },
		{ -0.1 },
		1,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ _, _ -> true }
	).setKnockbackCallback { event, _, _ ->
		event.acceleration.multiply(1.25)
	}.register()

	val POISONOUS = SingleTrait(
		"poisonous", "Poisonous",
		"Your attacks have a 50% chance to give Poison I."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is LivingEntity && Random.nextBoolean())
			entity.addPotionEffect(PotionEffect(PotionEffectType.POISON, 50, 0, true, true, false))
	}.register()

	val FRACTURING = SingleTrait(
		"fracturing", "Fracturing",
		"Your attacks deal more damage when the weapon has less durability."
	).setDamageCallback { event, player, _ ->
		val item = player.inventory.itemInMainHand
		val modifier = 0.35 * (DamageableUtils.getDamage(item) / DamageableUtils.getMaxDurability(item))
		event.damage *= (modifier + 1)
	}.register()

	val CONDUCTIVE = SingleTrait(
		"conductive", "Conductive",
		"Your attacks have a 20% chance to stun enemies."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is LivingEntity && Random.nextInt(5) == 0)
			entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 20, 2, true, true, false))
	}.register()

	val BALANCED = AttributeTrait(
		"balanced", "Balanced",
		{ "You attack 5% faster and have 15% more knockback." },
		{ 0.15 },
		1,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ _, _ -> true }
	).setKnockbackCallback { event, _, _ ->
		event.acceleration.multiply(1.15)
	}.register()

	val RUSTING = AttributeTrait(
		"rusting", "Rusting",
		{ "You attack 10% faster, but deal 25% less damage when raining." },
		{ 0.15 },
		1,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ _, _ -> true }
	).setDamageCallback { event, _, _ ->
		if (event.entity.world.hasStorm()) event.damage *= 0.75;
	}.register()

	val PRECISE = AttributeTrait(
		"precise", "Precise",
		{ "You attack 15% slower, but deal 20% more damage." },
		{ -0.15 },
		1,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ _, _ -> true }
	).setDamageCallback { event, _, _ ->
		event.damage *= 1.2
	}.register()

	val ELECTROCUTION = SingleTrait(
		"electrocution", "Electrocution",
		"Your attacks have a 0.5% chance to strike lightning."
	).setDamageCallback { event, _, _ ->
		if (event.entity is LivingEntity && Random.nextInt(1000) < 5) {
			event.entity.world.strikeLightning(event.entity.location)
			event.damage *= 4
		}
	}.register()

	val PURIFYING = SingleTrait(
		"purifying", "Purifying",
		"Your attacks heal you by an eigth of a heart."
	).setDamageCallback { event, player, _ ->
		if (event.entity is LivingEntity) {
			player.health += 0.25
		}
	}.register()

	val HOLY = SingleTrait(
		"holy", "Holy",
		"You deal double damage against undead."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is LivingEntity && entity.category == EntityCategory.UNDEAD) {
			event.damage *= 2
			entity.fireTicks = 100
		}
	}.register()

	val WEALTHY = SingleTrait(
		"wealthy", "Wealthy",
		"Mobs have a 5% chance to drop an emerald when killed."
	).setKillCallback { event, _, _ ->
		if (Random.nextInt(20) == 0) {
			event.drops.add(ItemStack(EMERALD))
		}
	}.register()

	val IMPERVIOUS = SingleTrait(
		"impervious", "Impervious",
		"Recovers durability when attacking enemies."
	).setDamageCallback { event, player, _ ->
		val entity = event.entity
		val item = player.inventory.itemInMainHand
		if (entity is LivingEntity) {
			DamageableUtils.setDamage(item, max(0, (DamageableUtils.getDamage(item) - event.damage / 4).toInt()))
		}
	}.register()

	val TEMPERED = SingleTrait(
		"tempered", "Tempered",
		"Deals 10% damage through armor."
	).setDamageCallback { event, _, _ ->
		if (event.getDamage(ARMOR) != 0.0) event.setDamage(ARMOR, event.getDamage(ARMOR) * 0.9)
	}.register()

	val SWIFT = AttributeTrait(
		"swift", "Swift",
		{ "You move 10% faster." },
		{ 0.1 },
		1,
		Attributes.MOVEMENT_SPEED,
		MULTIPLY_TOTAL,
		{ _, _ -> true }
	).register()

	val PRISMATIC = SingleTrait(
		"prismatic", "Prismatic",
		"Attacks have a 10% chance to blind the enemy."
	).setDamageCallback { event, _, _ ->
		val entity = event.entity
		if (entity is LivingEntity && Random.nextInt(10) == 0)
			entity.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 200, 0, true, true, false))
	}.register()

	val LIGHTWEIGHT = AttributeTrait(
		"lightweight", "Lightweight",
		{ "You attack 10% faster." },
		{ 0.1 },
		1,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ _, _ -> true }
	).register()

	val ANCIENT = AttributeTrait(
		"ancient", "Ancient",
		{ "You attack 5% faster and deal 5% more damage." },
		{ 0.05 },
		1,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ _, _ -> true }
	).setDamageCallback { event, _, _ ->
		event.damage *= 1.05
	}.register()

	val FEATHERWEIGHT = AttributeTrait(
		"featherweight", "Featherweight",
		{ "You attack 15% faster." },
		{ 0.15 },
		1,
		Attributes.ATTACK_SPEED,
		MULTIPLY_TOTAL,
		{ _, _ -> true }
	).register()


}