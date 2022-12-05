package dev.melncat.est.weaponart


import dev.melncat.est.util.EstKey
import dev.melncat.est.util.get
import dev.melncat.furcation.util.mm
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.nova.material.NovaMaterialRegistry
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.DurationUnit.MILLISECONDS

enum class WeaponArtActivation {
	Interact, InteractEntity
}

data class ActiveWeaponArt<T>(
	val art: WeaponArt<T>,
	val position: Location,
	val player: Player,
	val entity: LivingEntity,
	val item: ItemStack,
	var time: Int = 0,
	var duration: Int = art.duration
) {
	var state: T = art.defaultState(this)
	val firstTick: Boolean
		get() = time == 0
	val lastTick: Boolean
		get() = time == duration - 1

	fun end() {
		time = -1
	}
}

data class WeaponArt<T>(
	val id: String,
	val name: Component,
	val duration: Int,
	val cooldown: Int,
	val activation: WeaponArtActivation = WeaponArtActivation.Interact,
	val defaultState: (ActiveWeaponArt<*>) -> T,
	val cb: (ActiveWeaponArt<T>) -> Unit,
	val onAttack: (EntityDamageByEntityEvent, ActiveWeaponArt<T>) -> Unit
)

data class WeaponArtBuilder<T>(val id: String, var defaultState: (ActiveWeaponArt<*>) -> T) {
	private var name: Component? = null
	private var duration: Int = 1
	private var cooldown: Int? = null
	private var activation: WeaponArtActivation = WeaponArtActivation.Interact
	private var executor: (ActiveWeaponArt<T>) -> Unit = {}
	private var onAttack: (EntityDamageByEntityEvent, ActiveWeaponArt<T>) -> Unit = { _, _ -> }

	fun name(str: String) {
		name(str.mm())
	}

	fun name(component: Component) {
		name = component
	}

	fun duration(dur: Duration) {
		duration(dur.toInt(MILLISECONDS) / 50)
	}

	fun duration(ticks: Int) {
		duration = ticks
	}

	fun cooldown(dur: Duration) {
		cooldown(dur.toInt(MILLISECONDS))
	}

	fun cooldown(ms: Int) {
		cooldown = ms
	}

	fun activation(act: WeaponArtActivation) {
		activation = act
	}

	fun executor(exec: (ActiveWeaponArt<T>) -> Unit) {
		executor = exec
	}

	fun onAttack(exec: (event: EntityDamageByEntityEvent, wp: ActiveWeaponArt<T>) -> Unit) {
		onAttack = exec
	}

	fun defaultState(state: (ActiveWeaponArt<*>) -> T) {
		defaultState = state
	}

	fun build() = WeaponArt(id, name!!, duration, cooldown!!, activation, defaultState, executor, onAttack)
}

const val particleViewDistance = 32

@Suppress("UNCHECKED_CAST")
object WeaponArtRegistry {
	private val registered = mutableMapOf<String, WeaponArt<*>>()
	private val lookup = mutableMapOf<Key, WeaponArt<*>>()

	private operator fun Map<Key, WeaponArt<*>>.get(key: Pair<String, String>) = this[Key.key(key.first, key.second)]
	private operator fun MutableMap<Key, WeaponArt<*>>.set(key: Pair<String, String>, value: WeaponArt<*>) =
		put(Key.key(key.first, key.second), value)

	private operator fun Map<Key, WeaponArt<*>>.get(key: Material) = this[key.key]
	private operator fun MutableMap<Key, WeaponArt<*>>.set(key: Material, value: WeaponArt<*>) = put(key.key, value)

	val ids: Set<String>
		get() = registered.keys
	val weaponArts: Collection<WeaponArt<*>>
		get() = registered.values

	fun fromId(id: String) = registered[id]


	fun <T> register(
		id: String,
		defaultState: (ActiveWeaponArt<*>) -> T = { Unit as T },
		cb: WeaponArtBuilder<T>.() -> Unit
	) = WeaponArtBuilder(id, defaultState).apply(cb).build().also { registered[it.id] = it }

	fun register(id: String, cb: WeaponArtBuilder<Unit>.() -> Unit) =
		WeaponArtBuilder(id) {}.apply(cb).build().also { registered[it.id] = it }

	fun fromItem(item: ItemStack) =
		item.persistentDataContainer.get<String>(EstKey.weaponArtOverride).let { registered[it] }
		?: NovaMaterialRegistry.getOrNull(item)?.let { lookup[it.id.toNamespacedKey()] }
		?: lookup[item.type.key()]

	fun WeaponArt<*>.item(material: Material) = also { lookup[material.key] = this }
	fun WeaponArt<*>.item(key: Pair<String, String>) = also { lookup[key] = this }
	fun WeaponArt<*>.item(key: Key) = also { lookup[key] = this }
	fun WeaponArt<*>.custom(key: String) = item(Key.key(key))

}

object ActiveWeaponArts {
	val active = mutableSetOf<ActiveWeaponArt<*>>()
	data class CooldownData(val start: Long, val end: Long)

	val cooldowns = mutableMapOf<UUID, CooldownData>()

	fun executeWeaponArt(art: WeaponArt<*>, player: Player, item: ItemStack, entity: LivingEntity = player): Boolean {
		active.add(
			ActiveWeaponArt(
				art,
				player.location,
				player,
				entity,
				item
			)
		)
		val end = System.currentTimeMillis() + art.cooldown
		cooldowns[player.uniqueId] =
			CooldownData(System.currentTimeMillis(), end)
		return true
	}
	fun executeWeaponArt(art: WeaponArt<*>, event: PlayerInteractEvent): Boolean {
		val entity = event.player.getTargetEntity(5) as? LivingEntity
		if (art.activation == WeaponArtActivation.InteractEntity && entity == null) return false
		return executeWeaponArt(art, event.player, event.item!!, entity ?: event.player)
	}

	fun hasCooldown(player: UUID) = cooldowns.containsKey(player)
	fun hasCooldown(player: Player) = hasCooldown(player.uniqueId)
	fun inCooldown(player: UUID)
		= hasCooldown(player) && cooldowns[player]!!.end > System.currentTimeMillis()
	fun inCooldown(player: Player) = inCooldown(player.uniqueId)
	fun resetCooldown(player: UUID) = cooldowns.remove(player)
	fun resetCooldown(player: Player) = resetCooldown(player.uniqueId)
}


@Suppress("UNCHECKED_CAST")
fun tickWeaponArts() {
	val iterator = ActiveWeaponArts.active.iterator()
	while (iterator.hasNext()) {
		val art = iterator.next() as ActiveWeaponArt<Any>
		art.art.cb(art)
		if (art.time == -1 || art.time >= art.duration - 1) {
			iterator.remove()
			continue
		}
		art.time++

	}
}

fun registerDefaultWeaponArts() {
	WeaponArtRegistry.registerVanilla()
	WeaponArtRegistry.registerCustom()
	WeaponArtRegistry.registerGeneric()
}
