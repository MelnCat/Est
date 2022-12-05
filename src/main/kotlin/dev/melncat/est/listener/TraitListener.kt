package dev.melncat.est.listener

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import dev.melncat.est.trait.AttributeTrait
import dev.melncat.est.trait.Trait
import dev.melncat.est.trait.Traits
import dev.melncat.est.trait.getEffectiveTraits
import dev.melncat.est.trait.getShownTraits
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.isAir
import dev.melncat.est.util.set
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.TD
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL
import net.minecraft.world.entity.ai.attributes.Attributes
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGH
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.purpurmc.purpur.event.packet.NetworkItemSerializeEvent
import xyz.xenondevs.nova.util.plus
import java.util.UUID
import kotlin.reflect.full.memberFunctions

val attacksSinceMiss = mutableMapOf<UUID, Int>()

@RegisterListener
object TraitListener : FListener {
	@EventHandler
	fun onItemSerialize(event: NetworkItemSerializeEvent) {
		val traits = getShownTraits(event.itemStack)
		if (traits.isNotEmpty()) {
			event.itemStack.lore(
				traits.map {
					Component.text(
						"${it.key.displayName}${
							if (it.key.maxLevel > 1) " ${roman(it.value)}" else ""
						}"
					).color(TextColor.color(0x777766)).decoration(TD.ITALIC, false)
				} + (event.itemStack.lore() ?: listOf())
			)
		}
	}

	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
		if (!event.action.isRightClick) return
		val item = event.item ?: return
		if (item.isAir) return
		val player = event.player
		val traits = getEffectiveTraits(player.inventory.itemInMainHand)
		if (traits.containsKey(Traits.THROWABLE)) {
			player.launchProjectile(Snowball::class.java, player.location.direction.multiply(1.6)) {
				it.item = item.asOne()
				item.subtract()
				it.persistentDataContainer.set(EstKey.thrownWeapon, true)
			}
		}
	}

	@EventHandler
	fun onLeftClick(event: PlayerInteractEvent) {
		if (!event.action.isLeftClick) return
		attacksSinceMiss[event.player.uniqueId] = 0
	}

	@EventHandler(ignoreCancelled = true, priority = HIGH)
	fun onDamage(event: EntityDamageByEntityEvent) {
		val player = event.damager as? Player ?: return
		attacksSinceMiss[player.uniqueId] = (attacksSinceMiss[player.uniqueId] ?: 0) + 1
		val item = player.inventory.itemInMainHand
		if (item.isAir) return
		@Suppress("UNCHECKED_CAST")
		val traits = getEffectiveTraits(item) as Map<Trait<Any>, Int>
		println(event.cause + ": " + player.name)
		for (trait in traits)
			if (event.cause == ENTITY_ATTACK || (trait.key == Traits.SWEEPING && event.cause == ENTITY_SWEEP_ATTACK))
				trait.key.onDamage?.let { it(event, player, trait.key.calculate(trait.value)) }
	}

	@EventHandler(ignoreCancelled = true, priority = HIGH)
	fun onKill(event: EntityDeathEvent) {
		val player = event.entity.killer ?: return
		val item = player.inventory.itemInMainHand
		if (item.isAir) return
		@Suppress("UNCHECKED_CAST")
		val traits = getEffectiveTraits(item) as Map<Trait<Any>, Int>
		for (trait in traits) trait.key.onKill?.let { it(event, player, trait.key.calculate(trait.value)) }
	}

	@EventHandler(ignoreCancelled = true, priority = HIGH)
	fun onKnockback(event: EntityKnockbackByEntityEvent) {
		val player = event.hitBy as? Player ?: return
		val item = player.inventory.itemInMainHand
		if (item.isAir) return
		@Suppress("UNCHECKED_CAST")
		val traits = getEffectiveTraits(item) as Map<Trait<Any>, Int>
		for (trait in traits) trait.key.onKnockback?.let { it(event, player, trait.key.calculate(trait.value)) }
	}
}

private object TraitAttributes {
	val twoHandedId: UUID = UUID.fromString("081eec5a-19c4-4518-ac08-26d6ab6b4a01")
	val twoHanded = (1..Traits.TWO_HANDED.maxLevel).associateWith {
		AttributeModifier(twoHandedId, "Two Handed", Traits.TWO_HANDED.calculate(it), MULTIPLY_TOTAL)
	}
}


fun tickTraits() {
	for (player in Bukkit.getOnlinePlayers()) {
		val item = player.inventory.itemInMainHand
		if (item.isAir) continue
		for (trait in Traits.traitRegistry.values)
			if (trait is AttributeTrait) trait.checkPlayer(player, item)
	}
}