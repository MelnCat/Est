package dev.melncat.est.listener

import com.destroystokyo.paper.MaterialTags
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.get
import dev.melncat.est.util.set
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.TD
import dev.melncat.furcation.util.component
import dev.melncat.furcation.util.mm
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material.*
import org.bukkit.OfflinePlayer
import org.bukkit.craftbukkit.v1_19_R2.CraftOfflinePlayer
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGHEST
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.purpurmc.purpur.event.packet.NetworkItemSerializeEvent
import xyz.xenondevs.nova.util.item.unhandledTags
import java.util.UUID

@RegisterListener
object SkullRecipeListener : FListener {
	@EventHandler
	fun on(event: NetworkItemSerializeEvent) {
		if (!event.itemStack.persistentDataContainer.has(EstKey.customItem)) return
		val customItem = event.itemStack.persistentDataContainer.get<String>(EstKey.customItem)
		if (customItem == "player_spawn_manipulator") {
			event.itemStack.editMeta {
				it.displayName("Player Spawn Manipulator".component(NTC.YELLOW).decoration(TD.ITALIC, false))
				val player = if (it.persistentDataContainer.has(EstKey.data))
					Bukkit.getOfflinePlayer(
						UUID.fromString(it.persistentDataContainer.get(EstKey.data))
					) else null
				it.lore(
					listOf(
						"Right click on a block to set the".component(NTC.DARK_GRAY).decoration(TD.ITALIC, false),
						"target player's spawnpoint.".component(NTC.DARK_GRAY).decoration(TD.ITALIC, false)
					)
				)
				if (player != null) it.lore(
					listOf(
						"<!i><gray>Configured Player<dark_gray>: <green><player>".mm(
							"player" to (player.name ?: player.uniqueId)
						)
					) + (it.lore() ?: listOf())
				)
			}
		} else if (customItem == "cobblestone_dynamite") {
			event.itemStack.editMeta {
				it.displayName("Cobblestone Dynamite".component(NTC.YELLOW).decoration(TD.ITALIC, false))
				it.lore(
					listOf(
						"Only destroys cobblestone, right click to use.".component(NTC.DARK_GRAY)
							.decoration(TD.ITALIC, false)
					)
				)
			}
		} else if (customItem == "gun") {
			event.itemStack.editMeta {
				it.displayName("<!i><red>A Fucking Gun".mm())
				it.lore(
					listOf(
						"<!i><dark_gray>Right click to use. Does not need ammo.".mm()
					)
				)
			}
		}
	}

	@EventHandler
	fun on(event: EntityExplodeEvent) {
		val entity = event.entity as? Snowball ?: return
		if (entity.item.persistentDataContainer.get<String>(EstKey.customItem) == "cobblestone_dynamite") {
			event.blockList().removeIf { it.type != COBBLESTONE }
		}
	}

	@EventHandler(priority = HIGHEST)
	fun on(event: PrepareItemCraftEvent) {
		val result = event.inventory.result ?: return
		if (result.persistentDataContainer.get<String>(EstKey.customItem) == "player_spawn_manipulator") {
			val head = event.inventory.matrix?.find { it?.type == PLAYER_HEAD } ?: return
			val player =
				if (head.itemMeta.unhandledTags.containsKey("HeadDrops_Owner")) head.itemMeta.unhandledTags["HeadDrops_Owner"]?.asString else
					(head.itemMeta as? SkullMeta)?.owningPlayer?.uniqueId.toString()
			player ?: return
			result.editMeta {
				it.persistentDataContainer.set(EstKey.data, player)
			}
		}
	}

	@EventHandler
	fun on(event: PlayerInteractEvent) {
		val item = event.item ?: return
		if (!item.persistentDataContainer.has(EstKey.customItem)) return
		val customItem = item.persistentDataContainer.get<String>(EstKey.customItem)
		if (customItem == "player_spawn_manipulator") {
			if (event.action != RIGHT_CLICK_BLOCK) return
			val block = event.clickedBlock ?: return
			if (!MaterialTags.BEDS.isTagged(block.type)) {
				event.player.sendMessage("You must use this item on a bed.".component(NTC.RED))
				return
			}
			if (!block.world.isBedWorks) {
				event.player.sendMessage("You must use this item in the overworld.".component(NTC.RED))
				return
			}
			val player = if (item.persistentDataContainer.has(EstKey.data))
				Bukkit.getOfflinePlayer(
					UUID.fromString(item.persistentDataContainer.get(EstKey.data))
				) else null ?: return
			if (player is Player) player.bedSpawnLocation = block.location;
			else {
				val data = CraftOfflinePlayer::class.java.getDeclaredMethod("getData").let {
					it.isAccessible = true
					it.invoke(player)
				} as CompoundTag? ?: return
				data.putInt("SpawnX", block.x)
				data.putInt("SpawnY", block.y)
				data.putInt("SpawnZ", block.z)
				data.putString("SpawnDimension", "minecraft:overworld")
				CraftOfflinePlayer::class.java.getDeclaredMethod("save", CompoundTag::class.java).let {
					it.isAccessible = true
					it.invoke(player, data)
				}
			}
			event.isCancelled = true
			event.player.sendMessage("${player.name}'s spawn point has been set.".component(NTC.GREEN))
			item.subtract()
		} else if (customItem == "cobblestone_dynamite") {
			if (!event.action.isRightClick) return
			val clone = item.asOne();
			item.subtract()
			event.player.launchProjectile(Snowball::class.java, null) {
				it.setGravity(false)
				it.item = clone
			}
		} else if (customItem == "gun") {
			if (!event.action.isRightClick) return
			event.player.launchProjectile(Snowball::class.java, event.player.location.direction.multiply(2)) {
				it.setGravity(false)
				it.item = ItemStack(IRON_NUGGET)
			}
		}
	}
}


object CustomItems {
	fun createItem(id: String): ItemStack {
		val item = ItemStack(
			when (id) {
				"player_spawn_manipulator" -> CLOCK
				"cobblestone_dynamite" -> RED_CANDLE
				"gun" -> NETHERITE_HOE
				else -> COBBLESTONE
			}
		)
		item.editMeta {
			it.persistentDataContainer.set(EstKey.customItem, id)
		}
		return item;
	}
}