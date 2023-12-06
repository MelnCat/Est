package dev.melncat.est.listener

import com.destroystokyo.paper.MaterialTags
import de.studiocode.invui.gui.GUI
import de.studiocode.invui.gui.builder.GUIBuilder
import de.studiocode.invui.gui.builder.guitype.GUIType
import de.studiocode.invui.gui.impl.BaseGUI
import de.studiocode.invui.gui.structure.Markers
import de.studiocode.invui.item.ItemProvider
import de.studiocode.invui.item.ItemWrapper
import de.studiocode.invui.item.impl.BaseItem
import de.studiocode.invui.item.impl.SimpleItem
import de.studiocode.invui.item.impl.controlitem.ControlItem
import de.studiocode.invui.virtualinventory.VirtualInventory
import de.studiocode.invui.window.impl.single.AnvilWindow
import de.studiocode.invui.window.impl.single.SimpleWindow
import dev.melncat.est.command.PlayerGroupCommand
import dev.melncat.est.playergroup.PlayerGroup
import dev.melncat.est.playergroup.PlayerGroup.Permission
import dev.melncat.est.playergroup.PlayerGroup.Permission.Interact
import dev.melncat.est.playergroup.PlayerGroup.Rank
import dev.melncat.est.playergroup.playerGroups
import dev.melncat.est.util.EstKey
import dev.melncat.est.util.GuiItems
import dev.melncat.est.util.GuiItems.ScrollDown
import dev.melncat.est.util.GuiItems.ScrollUp
import dev.melncat.est.util.get
import dev.melncat.est.util.minifiedGson
import dev.melncat.est.util.notItalic
import dev.melncat.est.util.set
import dev.melncat.est.util.withLore
import dev.melncat.est.util.withName
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.component
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.OfflinePlayer
import org.bukkit.block.Container
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Event.Result.DENY
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGH
import org.bukkit.event.EventPriority.HIGHEST
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS
import org.bukkit.inventory.ItemStack
import org.purpurmc.purpur.event.packet.NetworkItemSerializeEvent
import xyz.xenondevs.nova.util.dropItem
import xyz.xenondevs.nova.util.hardness
import java.util.EnumMap
import java.util.UUID
import kotlin.math.max

data class LodestoneData(
	var groupName: String?,
	var power: Long,
	val permissionOverrides: EnumMap<Rank, EnumMap<Permission, Boolean>> =
		EnumMap(Rank.values().associateWith { EnumMap<Permission, Boolean>(Permission::class.java) })
) {
	val group: PlayerGroup?
		get() {
			val g = groupName?.let { playerGroups[groupName] }
			if (g == null) groupName = null
			return g
		}

	fun hasPermission(player: UUID, permission: Permission) = group.let { g ->
		g == null || (permissionOverrides[g.rankOf(player)]?.get(permission) ?: g.hasPermission(player, permission))
	}

	fun hasPermission(player: OfflinePlayer, permission: Permission) = hasPermission(player.uniqueId, permission)
}

data class ChunkLodestoneData(
	val x: Int,
	val y: Int,
	val z: Int,
	val data: LodestoneData
)


@RegisterListener
object LodestoneListener : FListener {
	@EventHandler
	fun onCommandPreprocess(event: PlayerCommandPreprocessEvent) {
		if (event.message.contains("ie rename", true)) event.message =
			event.message.replace("ie rename", "rename", true)
	}

	@EventHandler
	fun on(event: ChunkLoadEvent) {
		val chunk = event.chunk
		chunk.getLodestone()
	}

	@EventHandler
	fun on(event: NetworkItemSerializeEvent) {
		if (event.itemStack.type == LODESTONE && event.itemStack.persistentDataContainer.has(EstKey.lodestone)) {
			val data = event.itemStack.persistentDataContainer.get<String>(EstKey.lodestone) ?: return
			val parsed = minifiedGson.fromJson(
				event.itemStack.persistentDataContainer.get<String>(EstKey.lodestone),
				LodestoneData::class.java
			) ?: return
			event.itemStack.lore(
				listOf(
					"<!i><green>Power<gray>: <yellow><0>".mm(parsed.power)
				) + (event.itemStack.lore() ?: listOf())
			)

		}
	}

	@EventHandler(ignoreCancelled = true, priority = HIGHEST)
	fun onLodestone(event: BlockPlaceEvent) {
		val block = event.block
		val chunk = block.chunk
		if (!event.canBuild()) return
		if (block.type == LODESTONE) {
			if (block.world.name.contains("the_end")) {
				event.setBuild(false)
				event.player.sendMessage("<red>You cannot use lodestones in the end.".mm())
				return
			}
			if (chunk.getLodestone() != null) {
				event.setBuild(false)
				event.player.sendMessage("<red>There is already a lodestone in this chunk.".mm())
				return
			}
			val data = if (event.itemInHand.persistentDataContainer.has(EstKey.lodestone))
				minifiedGson.fromJson(
					event.itemInHand.persistentDataContainer.get<String>(EstKey.lodestone),
					LodestoneData::class.java
				)
			else LodestoneData(null, 500L)
			val group = data.group
			if (group != null && !group.hasPermission(event.player, Permission.CreateLodestone))
				data.groupName = null
			chunk.persistentDataContainer.set<String>(
				EstKey.lodestone,
				minifiedGson.toJson(ChunkLodestoneData(block.x and 0xF, block.y, block.z and 0xF, data))
			)
			event.player.sendMessage(
				("<green>You have successfully set up a lodestone.${
					if (data.group == null)
						" Right click it to set a group to allow block protection."
					else ""
				}").mm()
			)
		}
	}

	@EventHandler(ignoreCancelled = true, priority = HIGH)
	fun on(event: BlockBreakEvent) {
		val block = event.block
		val chunk = block.chunk
		if (block.type == LODESTONE) {
			val data = chunk.getLodestone(false)
			data?.data?.group?.broadcast(
				"<yellow><0><gold> broke a lodestone with <yellow><1><gold> power at <yellow><2><gold>.".mm(
					event.player.name, data.data.power, "${block.x} ${block.y} ${block.z}"
				)
			)
			event.isDropItems = false
			event.block.location.dropItem(ItemStack(LODESTONE).also {
				it.editMeta { m ->
					if (data != null) m.persistentDataContainer.set(EstKey.lodestone, minifiedGson.toJson(data.data))
				}
			})
			return
		}
		val lodestone = chunk.getPoweredLodestone() ?: return
		if (lodestone.data.hasPermission(event.player, Permission.Break)) return
		if (event.player.hasPermission("est.bypasslodestones")) return
		event.isCancelled = true
		val loss = when {
			event.block.hardness < 1 -> 0
			event.block.hardness < 1.5 -> if (lodestone.data.power > 1000) 1 else 0
			else -> 1
		}
		lodestone.data.power = max(0, lodestone.data.power - loss)
		chunk.saveLodestone(lodestone)
		event.player.sendActionBar(
			"<red>Chunk protected by <gold><0></gold>. <green>Power<gray>: <yellow><1> <gray>(<green><2></green>)".mm(
				lodestone.data.groupName ?: "??", lodestone.data.power, loss,
			)
		)
		event.player.sendBlockHighlight(event.block.location, 5000, "Power: ${lodestone.data.power}", 0)
	}

	@EventHandler(ignoreCancelled = true, priority = HIGH)
	fun on(event: BlockPlaceEvent) {
		val block = event.block
		val chunk = block.chunk
		if (!event.canBuild()) return
		val lodestone = chunk.getPoweredLodestone() ?: return
		if (lodestone.data.hasPermission(event.player, Permission.Place)) return
		if (event.player.hasPermission("est.bypasslodestones")) return
		lodestone.sendMessage(event.player)
		event.setBuild(false)
	}


	@EventHandler(ignoreCancelled = true, priority = HIGH)
	fun on(event: BlockExplodeEvent) {
		val block = event.block
		val chunk = block.chunk
		val lodestone = chunk.getPoweredLodestone() ?: return
		lodestone.data.power = max(0, lodestone.data.power - 75)
		chunk.saveLodestone(lodestone)
	}

	@EventHandler(ignoreCancelled = true, priority = HIGH)
	fun on(event: EntityExplodeEvent) {
		val entity = event.entity
		val chunk = entity.chunk
		val lodestone = chunk.getPoweredLodestone() ?: return
		lodestone.data.power = max(0, lodestone.data.power - event.blockList().size)
		chunk.saveLodestone(lodestone)
	}

	@EventHandler(ignoreCancelled = true, priority = HIGH)
	fun on(event: PlayerInteractEvent) {
		if (event.action != RIGHT_CLICK_BLOCK) return
		val block = event.clickedBlock ?: return
		val chunk = block.chunk
		if (block.type == LODESTONE) {
			val lodestone = chunk.getLodestone() ?: return
			if (lodestone.x != block.x and 0xF || lodestone.y != block.y || lodestone.z != block.z and 0xF) return
			if (!lodestone.data.hasPermission(event.player, Permission.EditLodestone) && !lodestone.data.hasPermission(
					event.player,
					Permission.PowerLodestone
				)
			) {
				event.player.sendMessage(
					"<red>You cannot edit lodestones from the group <gold><0></gold>. If you are trying to destroy it, break it instead.".mm(
						lodestone.data.groupName ?: "??"
					)
				)
				event.setUseInteractedBlock(DENY)
				return
			}
			SimpleWindow(event.player, "Lodestone Settings", lodestoneGui(event.player, chunk)).show()
			return
		}
		val lodestone = chunk.getPoweredLodestone() ?: return
		if (block.state is Container) {
			if (lodestone.data.hasPermission(event.player, Permission.Storage)) return
		} else if (MaterialTags.DOORS.isTagged(block) || MaterialTags.TRAPDOORS.isTagged(block)) {
			if (lodestone.data.hasPermission(event.player, Permission.Doors)) return
		} else if (lodestone.data.hasPermission(event.player, Interact)) return
		if (event.player.hasPermission("est.bypasslodestones")) return
		lodestone.sendMessage(event.player)
		event.setUseInteractedBlock(DENY)
	}

	private fun lodestoneGui(player: Player, chunk: Chunk): GUI? {
		val data = chunk.getLodestone() ?: return null
		val builder = GUIBuilder(GUIType.NORMAL)
			.setStructure(
				"# . . . i . . . #",
				"# . . . . . . . #",
				"# . p . g . r . #",
				"# . . . . . . . #",
				"# . . . . . . . #",
			)
			.addIngredient('#', GuiItems.Blank)
			.addIngredient('i', ItemWrapper(
				ItemStack(PAPER).withName("<!i><yellow>Lodestone".mm())
					.withLore(listOf(
						"<!i><green>Player Group<gray>: <yellow><0>".mm(data.data.groupName ?: "Unset"),
						"<!i><green>Power<gray>: <yellow><0>".mm(data.data.power)
					))
			))
		if (data.data.hasPermission(player, Permission.EditLodestone))
			builder.addIngredient('r', object : BaseItem() {
				override fun getItemProvider() = ItemWrapper(
					ItemStack(IRON_DOOR).withName("<!i><yellow>Permission Overrides".mm())
				)

				override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
					SimpleWindow(player, "Select Rank To Edit", selectRankPermissionsGui(player, chunk)).show()
				}
			})
		if (data.data.hasPermission(player, Permission.EditLodestone))
			builder.addIngredient('g', object : BaseItem() {
				override fun getItemProvider() = ItemWrapper(
					ItemStack(CHEST).withName("<!i><yellow>Set Player Group".mm())
				)

				override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
					lodestoneGroupGui(player, chunk)?.show()
				}
			})
		if (data.data.hasPermission(player, Permission.PowerLodestone))
			builder.addIngredient('p', object : BaseItem() {
				override fun getItemProvider() = ItemWrapper(
					ItemStack(IRON_DOOR).withName("<!i><yellow>Add Power".mm())
				)

				override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
					SimpleWindow(player, "Add Lodestone Power", lodestonePowerGui(player, chunk)).show()
				}
			})
		return builder.build()
	}

	private val materialPowerMap = EnumMap(
		mapOf(
			COBBLESTONE to 1,
			COPPER_INGOT to 5,
			IRON_INGOT to 15,
			DIAMOND to 115,
			NETHERITE_INGOT to 555
		)
	)

	private fun lodestonePowerGui(player: Player, chunk: Chunk): GUI? {
		val data = chunk.getLodestone() ?: return null
		val displayItem = object : BaseItem() {
			override fun getItemProvider() = ItemWrapper(
				ItemStack(PAPER).withName("<!i><yellow>Insert Material Below".mm())
					.withLore(
						listOf("<yellow>Power<gray>: <green><0>".mm(data.data.power)) +
								materialPowerMap.entries.map {
									"<aqua><0><gray>:</gray> <gold><1>".mm(Component.translatable(it.key), it.value)
								}
					)

			)
			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			}
		}
		val inv = VirtualInventory(null, 9)
		inv.setItemUpdateHandler {
			val item = it.newItemStack ?: return@setItemUpdateHandler
			if (item.type !in materialPowerMap) {
				it.isCancelled = true
				return@setItemUpdateHandler
			}
			val power = materialPowerMap[item.type]!! * it.addedAmount
			val newData = chunk.getLodestone() ?: return@setItemUpdateHandler
			newData.data.power += power
			chunk.saveLodestone(newData)
			it.newItemStack = ItemStack(AIR)
			displayItem.notifyWindows()

		}

		return GUIBuilder(GUIType.NORMAL)
			.setStructure(
				"b # # # p # # # #",
				"x x x x x x x x x",
			)
			.addIngredient('x', inv)
			.addIngredient(
				'p', displayItem
			)
			.addIngredient('#', GuiItems.Blank)
			.addIngredient('b', GuiItems.Back { _, _, _ ->
				SimpleWindow(player, "Lodestone Settings", lodestoneGui(player, chunk)).show()
			})
			.build()

	}

	private fun lodestoneGroupGui(player: Player, chunk: Chunk): AnvilWindow? {
		val data = chunk.getLodestone() ?: return null
		val outputItem = object : BaseItem() {
			var currentGroup = data.data.groupName
			override fun getItemProvider(): ItemProvider {
				if (currentGroup.isNullOrEmpty()) {
					return ItemWrapper(ItemStack(BLACK_STAINED_GLASS_PANE).withName("<!i><yellow>Enter a group name.".mm()))
				}
				val found = playerGroups[currentGroup]
					?: return ItemWrapper(ItemStack(RED_STAINED_GLASS_PANE).withName("<!i><red>No group found.".mm()))
				if (!found.hasPermission(player, Permission.CreateLodestone)) {
					return ItemWrapper(ItemStack(RED_STAINED_GLASS_PANE).withName("<!i><red>You do not have permission to create lodestones in that group.".mm()))
				}
				return ItemWrapper(
					ItemStack(GREEN_STAINED_GLASS_PANE).withName(
						"<!i><green>Click to set group: <yellow><0>".mm(
							found.name
						)
					)
				)
			}

			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
				val found = playerGroups[currentGroup] ?: return
				if (!found.hasPermission(player, Permission.CreateLodestone)) return
				val newData = chunk.getLodestone() ?: return
				newData.data.groupName = currentGroup
				chunk.saveLodestone(newData)
				SimpleWindow(player, "Lodestone Settings", lodestoneGui(player, chunk)).show()
			}

		}

		val gui = GUIBuilder(GUIType.NORMAL)
			.setStructure(
				"# b c",
			)
			.addIngredient('b', GuiItems.Back { _, _, _ ->
				SimpleWindow(player, "Lodestone Settings", lodestoneGui(player, chunk)).show()
			})
			.addIngredient(
				'c', outputItem
			)
			.addIngredient('#', ItemWrapper(
				ItemStack(BLACK_STAINED_GLASS_PANE).withName("<!i><0>".mm(data.data.groupName ?: ""))
			))
			.build()
		return AnvilWindow(player, "Enter Group:", gui) {
			outputItem.currentGroup = it
			outputItem.notifyWindows()
		}
	}

	private fun lodestonePermissionsGui(player: Player, chunk: Chunk, rank: Rank): GUI? {
		val data = chunk.getLodestone() ?: return null
		return GUIBuilder(GUIType.SCROLL_ITEMS)
			.setStructure(
				"< x x x x x x x u",
				"# x x x x x x x #",
				"# x x x x x x x #",
				"# x x x x x x x #",
				"# x x x x x x x d",
			)
			.addIngredient('x', Markers.ITEM_LIST_SLOT_HORIZONTAL)
			.addIngredient('u', ScrollUp())
			.addIngredient('d', ScrollDown())
			.addIngredient('<', GuiItems.Back { _, _, _ ->
				SimpleWindow(player, "Select Rank", selectRankPermissionsGui(player, chunk)).show()
			})
			.addIngredient('#', GuiItems.Blank)
			.setItems(Permission.values().filter { it.lodestone }
				.map { PermissionGuiItem(it, chunk, rank) })
			.build()

	}

	private class PermissionSetButton(
		val player: Player, val chunk: Chunk, val rank: Rank, val permission: Permission,
		val value: Boolean?, val name: Component, val description: String, val material: Material
	) : ControlItem<BaseGUI>() {
		override fun getItemProvider(gui: BaseGUI) = ItemWrapper(ItemStack(WHITE_STAINED_GLASS_PANE)
			.withName(name.notItalic())
			.withLore(
				listOf(
					"<!i><gray>$description".mm()
				)
			)
			.also {
				val data = chunk.getLodestone() ?: return@also
				if (data.data.permissionOverrides[rank]?.get(permission) == value) {
					it.editMeta { m ->
						m.lore(listOf("<!i><green>Selected".mm()) + (m.lore() ?: listOf()))
					}
				}
			}
		)

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			val data = chunk.getLodestone() ?: return
			if (!data.data.hasPermission(
					player,
					Permission.EditLodestone
				) || data.data.group!!.rankOf(player) > rank
			) return
			val newData = chunk.getLodestone() ?: return
			if (newData.data.permissionOverrides[rank]?.get(permission) == value) return
			if (newData.data.permissionOverrides[rank] == null) newData.data.permissionOverrides[rank] =
				EnumMap(Permission::class.java)
			if (value == null) newData.data.permissionOverrides[rank]?.remove(permission)
			else newData.data.permissionOverrides[rank]?.put(permission, value)
			chunk.saveLodestone(newData)
			gui.updateControlItems()
		}
	}

	private fun setSpecificPermissionGui(
		player: Player,
		chunk: Chunk,
		rank: Rank,
		permission: Permission
	): GUI? {
		val data = chunk.getLodestone() ?: return null
		return GUIBuilder(GUIType.NORMAL)
			.setStructure(
				"< . . . p . . . #",
				"# . . . . . . . #",
				"# . u . a . d . #",
				"# . . . . . . . #",
				"# . . . . . . . #",
			)
			.addIngredient('p', SimpleItem(ItemWrapper(permission.icon)))
			.addIngredient(
				'u',
				PermissionSetButton(
					player, chunk, rank, permission, null,
					"Inherited".mm(),
					"Uses the permission set in the player group.",
					WHITE_STAINED_GLASS_PANE
				)
			)
			.addIngredient(
				'a',
				PermissionSetButton(
					player, chunk, rank, permission, true,
					"Allow".mm(),
					"Allows the permission, overriding the group.",
					LIME_STAINED_GLASS_PANE
				)
			)
			.addIngredient(
				'd',
				PermissionSetButton(
					player, chunk, rank, permission, false,
					"Deny".mm(),
					"Denies the permission, overriding the group.",
					RED_STAINED_GLASS_PANE
				)
			)
			.addIngredient('<', GuiItems.Back { _, _, _ ->
				SimpleWindow(player, "Select Permission", lodestonePermissionsGui(player, chunk, rank)).show()
			})
			.addIngredient('#', GuiItems.Blank)
			.build()

	}

	private fun selectRankPermissionsGui(player: Player, chunk: Chunk): GUI? {
		val data = chunk.getLodestone() ?: return null
		val group = data.data.group!!
		val rank = group.rankOf(player)
		val editable = Rank.values().filter { it.editable && it > rank }
		return GUIBuilder(GUIType.SCROLL_ITEMS)
			.setStructure(
				"< . x . x . x . #",
				"# . . . . . . . #",
				"# . x . x . x . #",
				"# . . . . . . . #",
				"# . x . x . x . #",
			)
			.addIngredient('x', Markers.ITEM_LIST_SLOT_HORIZONTAL)
			.addIngredient('#', GuiItems.Blank)
			.addIngredient('<', GuiItems.Back { _, _, _ ->
				SimpleWindow(player, "Lodestone Settings", lodestoneGui(player, chunk)).show()
			})
			.setItems(editable.map { RankGuiItem(data, chunk, it) })
			.build()
	}

	private class RankGuiItem(val lodestone: ChunkLodestoneData, val chunk: Chunk, val rank: Rank) : BaseItem() {
		override fun getItemProvider() = ItemWrapper(
			ItemStack(PlayerGroupCommand.materialFromRank(rank))
				.withName(rank.name.component(NTC.YELLOW).notItalic())
		)

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			if (!lodestone.data.hasPermission(player, Permission.EditLodestone)) return
			SimpleWindow(
				player,
				"${rank.name} Lodestone Overrides",
				lodestonePermissionsGui(player, chunk, rank)
			).show()
		}
	}

	private class PermissionGuiItem(
		val permission: Permission,
		val chunk: Chunk,
		val rank: Rank
	) : BaseItem() {
		override fun getItemProvider() = ItemWrapper(permission.icon.clone().also {
			val data = chunk.getLodestone() ?: return@also
			it.editMeta { m ->
				val hasPermission = data.data.permissionOverrides[rank]?.get(permission)
				m.lore(
					listOf(
						"<!i><gold>State<gray>: <0>".mm(
							when (hasPermission) {
								true -> "<green>ALLOW".mm()
								false -> "<red>DENY".mm()
								null -> "<white>INHERITED".mm()
							}
						),
						"<!i><gold>Click to edit.".mm()
					) + (m.lore() ?: listOf())
				)
				if (hasPermission == true) {
					m.addEnchant(Enchantment.DAMAGE_UNDEAD, 1, true)
					m.addItemFlags(HIDE_ENCHANTS)
				}
			}
		})

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			SimpleWindow(player, "Configure ${rank.name} Permissions", setSpecificPermissionGui(player, chunk, rank, permission)).show()
		}
	}

	private fun Chunk.getLodestone(check: Boolean = true): ChunkLodestoneData? {
		if (!persistentDataContainer.has(EstKey.lodestone)) return null
		val data =
			minifiedGson.fromJson(persistentDataContainer.get<String>(EstKey.lodestone), ChunkLodestoneData::class.java)
				?: return null
		if (check && (data.x < 0 || data.x > 15 || data.z < 0 || data.z > 15 || getBlock(
				data.x,
				data.y,
				data.z
			).type != LODESTONE)
		) {
			persistentDataContainer.remove(EstKey.lodestone)
			return null
		}
		data.data.group
		return data
	}

	private fun Chunk.getPoweredLodestone(): ChunkLodestoneData? {
		val lodestone = getLodestone()
		if (lodestone == null || lodestone.data.group == null || lodestone.data.power <= 0L) return null
		return lodestone
	}

	private fun Chunk.saveLodestone(data: ChunkLodestoneData) {
		persistentDataContainer.set(EstKey.lodestone, minifiedGson.toJson(data))
	}

	private fun ChunkLodestoneData.sendMessage(player: Player) {
		player.sendMessage("<red>Chunk protected by <gold><0></gold>.".mm(data.groupName ?: "??"))
	}
}