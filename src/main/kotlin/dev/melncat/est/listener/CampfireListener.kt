package dev.melncat.est.listener

import com.destroystokyo.paper.ParticleBuilder
import dev.geco.gsit.GSitMain
import dev.geco.gsit.api.event.PlayerGetUpPlayerSitEvent
import dev.geco.gsit.objects.GetUpReason.GET_UP
import dev.melncat.est.util.lowest
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.mm
import io.papermc.paper.command.PaperCommand
import net.minecraft.server.commands.GiveCommand
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.Particle
import org.bukkit.Particle.DustTransition
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.EquipmentSlot.HAND
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.meta.Damageable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

private enum class CampfireType(val material: Material, val particle: ParticleBuilder, val effect: PotionEffect) {
	Normal(
		CAMPFIRE,
		ParticleBuilder(Particle.DUST_COLOR_TRANSITION)
			.data(DustTransition(Color.fromRGB(0xebd152), Color.fromRGB(0xb8a444), 4f))
			.offset(0.5, 0.5, 0.5)
			.count(2)
			.extra(1.0),
		PotionEffect(PotionEffectType.REGENERATION, 100, 0, true)
	),
	Soul(
		SOUL_CAMPFIRE,
		ParticleBuilder(Particle.DUST_COLOR_TRANSITION)
			.data(DustTransition(Color.fromRGB(0x13ADB1), Color.fromRGB(0x76DCDD), 4f))
			.offset(0.5, 0.5, 0.5)
			.count(2)
			.extra(1.0),
		PotionEffect(PotionEffectType.REGENERATION, 100, 2, true)
	)
}

private data class RestData(val type: CampfireType, val block: Block, var ticks: Int = 0)

private val resting: MutableMap<Player, RestData> = hashMapOf()

private const val particleRadius = 5

@RegisterListener
object CampfireListener : FListener {
	private val smeltable: List<RecipeChoice> by lazy {
		Bukkit.recipeIterator().asSequence().filterIsInstance<CampfireRecipe>().map { it.inputChoice }.toList()
	}

	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		if (event.hand != HAND || event.action != Action.RIGHT_CLICK_BLOCK || event.player.isSneaking) return
		if (resting.containsKey(event.player)) return
		val block = event.clickedBlock
		if (block?.type != CAMPFIRE && block?.type != SOUL_CAMPFIRE) return
		if (event.hasItem() && smeltable.any { it.test(event.item!!) }) return
		val seat =
			GSitMain.getInstance().sitManager.createSeat(
				event.player.location.block.lowest(), event.player
			)
		if (seat == null) {
			event.player.sendMessage("<red>You must be on flat terrain to rest at a campfire.".mm())
			return
		}
		val campfireType = when (block.type) {
			CAMPFIRE -> CampfireType.Normal
			SOUL_CAMPFIRE -> CampfireType.Soul
			else -> null
		}
		if (campfireType == null) {
			event.player.sendMessage("<red>This message should never appear.".mm())
			GSitMain.getInstance().sitManager.removeSeat(event.player, GET_UP)
			return
		}
		resting[event.player] = RestData(campfireType, block)
		event.player.swingMainHand()
	}

	@EventHandler
	fun onPlayerSitUp(event: PlayerGetUpPlayerSitEvent) {
		if (resting.containsKey(event.player))
			resting.remove(event.player)
	}

	@EventHandler
	fun onPlayerDamaged(event: EntityDamageEvent) {
		val entity = event.entity as? Player ?: return
		if (resting.remove(entity) != null) {
			GSitMain.getInstance().sitManager.removeSeat(entity, GET_UP)
			entity.sendMessage("<red>Your resting was interrupted.".mm())
		}
	}
}

fun tickCampfireResting() {
	val iterator = resting.iterator()
	while (iterator.hasNext()) {
		val (player, data) = iterator.next()
		if (data.ticks == 0) {
			player.setBedSpawnLocation(player.location, true)
			player.sendActionBar("<yellow>Your spawn point has been set to this campfire.".mm())
		}
		if (data.type == CampfireType.Soul || (data.type == CampfireType.Normal && data.ticks % 5 == 0))
			for (item in player.inventory) {
				if (item == null) continue
				val meta = item.itemMeta
				if (meta !is Damageable || meta.damage == 0) continue
				when (data.type) {
					CampfireType.Normal -> meta.damage--
					CampfireType.Soul -> meta.damage = 0
				}
				item.itemMeta = meta
			}
		if (data.ticks % 10 == 0) {
			for (i in 0 until 10) {
				val angle = Math.PI * 2 / 10 * i
				for (j in -2..10) {
					val dy = j / 2.0
					val radius = particleRadius * cos(asin(j / 10.0))
					val pos = player.location.add(cos(angle) * radius, dy, sin(angle) * radius)
					data.type.particle.location(pos).receivers(player).spawn()
				}
			}
		}
		if (data.ticks % 5 == 0) player.addPotionEffect(data.type.effect)
		if (data.block.type != data.type.material) iterator.remove()
		data.ticks++
	}
}