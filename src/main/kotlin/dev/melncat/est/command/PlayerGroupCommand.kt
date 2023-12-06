package dev.melncat.est.command

import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument
import cloud.commandframework.context.CommandContext
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext
import cloud.commandframework.extra.confirmation.CommandConfirmationManager
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import de.studiocode.invui.gui.GUI
import de.studiocode.invui.gui.builder.GUIBuilder
import de.studiocode.invui.gui.builder.guitype.GUIType
import de.studiocode.invui.gui.structure.Markers
import de.studiocode.invui.item.ItemProvider
import de.studiocode.invui.item.ItemWrapper
import de.studiocode.invui.item.impl.BaseItem
import de.studiocode.invui.item.impl.CommandItem
import de.studiocode.invui.item.impl.SimpleItem
import de.studiocode.invui.item.impl.controlitem.ControlItem
import de.studiocode.invui.window.impl.single.SimpleWindow
import dev.melncat.est.listener.playerGroupChats
import dev.melncat.est.playergroup.PlayerGroup
import dev.melncat.est.playergroup.PlayerGroup.Permission
import dev.melncat.est.playergroup.PlayerGroup.Permission.Delete
import dev.melncat.est.playergroup.PlayerGroup.Rank
import dev.melncat.est.playergroup.PlayerGroup.Rank.Admin
import dev.melncat.est.playergroup.PlayerGroup.Rank.Manager
import dev.melncat.est.playergroup.PlayerGroup.Rank.Member
import dev.melncat.est.playergroup.PlayerGroup.Rank.Moderator
import dev.melncat.est.playergroup.PlayerGroup.Rank.Outsider
import dev.melncat.est.playergroup.PlayerGroup.Rank.Owner
import dev.melncat.est.playergroup.playerGroups
import dev.melncat.est.playergroup.playerToGroups
import dev.melncat.est.util.GuiItems
import dev.melncat.est.util.GuiItems.CloseCommandItem
import dev.melncat.est.util.GuiItems.NextPage
import dev.melncat.est.util.GuiItems.PreviousPage
import dev.melncat.est.util.GuiItems.ScrollDown
import dev.melncat.est.util.GuiItems.ScrollUp
import dev.melncat.est.util.notItalic
import dev.melncat.est.util.withLore
import dev.melncat.est.util.withName
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.TD
import dev.melncat.furcation.util.component
import dev.melncat.furcation.util.mm
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.minecraft.world.entity.vehicle.Boat
import org.bukkit.Bukkit
import org.bukkit.Material.*
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.ClickType.LEFT
import org.bukkit.event.inventory.ClickType.RIGHT
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.Optional
import java.util.UUID
import java.util.concurrent.TimeUnit.SECONDS
import java.util.function.Consumer


@RegisterCommand
object PlayerGroupCommand : FCommand {
	private fun getGroup(ctx: CommandContext<CommandSender>): PlayerGroup? {
		val str = ctx.get<String>("group")
		if (str in playerGroups) return playerGroups[str]
		ctx.sender.sendMessage("<gold><0><red> is not a valid group.".mm(str))
		return null
	}

	override fun register(manager: PaperCommandManager<CommandSender>) {
		val confirmationManager = CommandConfirmationManager<CommandSender>(
			30L,
			SECONDS,
			{ it.commandContext.sender.sendMessage("<yellow>Are you sure you want to do this action? Run <white>/pg confirm<yellow> to confirm.".mm()) }
		) { it.sendMessage("<red>You don't have any pending commands.".mm()) }
		confirmationManager.registerConfirmationProcessor(manager)

		val base = manager.buildAndRegister("pg", aliases = arrayOf("playergroup", "playergroups")) {
			permission = "est.command.playergroup"
			senderType<Player>()
			handler { ctx ->
				val player = ctx.sender as Player
				SimpleWindow(player, "Your Player Groups", mainGui(player)).show()

			}
		}
		base.registerCopy("list") {
			handler { ctx ->
				val player = ctx.sender as Player
				SimpleWindow(player, "All Player Groups", mainGui(player, true)).show()
			}
		}
		base.registerCopy("confirm") {
			handler(confirmationManager.createConfirmationExecutionHandler())
		}
		base.registerCopy("info") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { _, _ ->
				playerGroups.keys.toList()
			})
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				SimpleWindow(player, group.name, groupGui(player, group)).show()
			}
		}
		val nameRegex = Regex("[a-zA-Z0-9_\\-$#@^]+")
		base.registerCopy("create") {
			argument(StringArgument.builder("name"))
			handler { ctx ->
				val player = ctx.sender as Player
				val name = ctx.get<String>("name")
				if (playerGroups.keys.any { it.equals(name, true) }) {
					player.sendMessage("<red>That name is already taken.".mm())
					return@handler
				}
				if (playerToGroups[player.uniqueId].size > 100) {
					player.sendMessage("<red>You are in the maximum number of groups.".mm())
					return@handler
				}
				if (!name.matches(nameRegex)) {
					player.sendMessage("<red>Group names may only contain letters, numbers, and the special characters _-$#@^.".mm())
					return@handler
				}
				if (name.length < 2 || name.length > 24) {
					player.sendMessage("<red>Group names must be between 2-24 characters.".mm())
					return@handler
				}
				val group = PlayerGroup.new(name, player.uniqueId)
				player.sendMessage("<green>You have successfully created the group <yellow><0><green>!".mm(group.name))
			}
		}
		base.registerCopy("join") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.isInvited((ctx.sender as Player).uniqueId) }.map { it.name }
			})
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				if (group.join(player.uniqueId)) {
					player.sendMessage("<green>You have joined the group <yellow><0><green>.".mm(group.name))
					group.broadcast("<yellow><0><gray> has joined the group.".mm(player.name))
				} else player.sendMessage("<red>You cannot join that group.".mm())
			}
		}
		base.registerCopy("delete") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.hasPermission(ctx.sender as Player, Permission.Delete) }.map { it.name }
			})
			meta(CommandConfirmationManager.META_CONFIRMATION_REQUIRED, true)
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				if (!group.hasPermission(player, Permission.Delete)) {
					player.sendMessage("<red>You do not have permission to delete this group.".mm())
					return@handler
				}
				group.broadcast("<green>The group has been deleted by <yellow><0></yellow>.".mm(player.name))
				group.delete()
				player.sendMessage("<green>The group has been successfully deleted.".mm())
			}
		}
		base.registerCopy("decline") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.isInvited((ctx.sender as Player).uniqueId) }.map { it.name }
			})
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				if (group.uninvite(player.uniqueId)) {
					player.sendMessage("<green>You declined the invite from <yellow><0><green>.".mm(group.name))
					group.broadcast("<yellow><0><gray> declined the invite to join.".mm(player.name))
				} else player.sendMessage("<red>You do not have an invite from that group.".mm())
			}
		}
		base.registerCopy("leave") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.hasPlayer(ctx.sender as Player) }.map { it.name }
			})
			meta(CommandConfirmationManager.META_CONFIRMATION_REQUIRED, true)
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				if (!group.hasPlayer(player.uniqueId)) {
					player.sendMessage("<red>You are not a member of that group.".mm())
					return@handler
				}
				if (group.rankOf(player) == Owner) {
					player.sendMessage("<red>You cannot leave a group that you are the owner of. Try deleting the group or promoting another player to owner first".mm())
					return@handler
				}
				group.kick(player.uniqueId)
				group.broadcast("<yellow><0><gray> has left the group.".mm(player.name))
			}
		}
		base.registerCopy("invite") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.hasPermission((ctx.sender as Player).uniqueId, Permission.Invite) }
					.map { it.name }
			})
			argument(OfflinePlayerArgument.of("target"))
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				val target = ctx.get<OfflinePlayer>("target")
				if (!group.hasPermission(player, Permission.Invite)) {
					player.sendMessage("<red>You do not have permission to invite players to that group.".mm())
					return@handler
				}
				if (group.hasPlayer(target)) {
					player.sendMessage("<red>That player is already a member of the group.".mm())
					return@handler
				}
				if (group.isInvited(target.uniqueId)) {
					player.sendMessage("<red>That player has already been invited.".mm())
					return@handler
				}
				group.invite(target.uniqueId)
				if (target is Player) target.sendMessage(
					("<green>You have been invited to join the group <yellow><0><green>." +
							"\n<green>Run <white><u><click:run_command:'/pg join ${group.name}'>/pg join ${group.name}</click></u><green> to join, or " +
							"<white><u><click:run_command:'/pg decline ${group.name}'>/pg decline ${group.name}</click></u><green> to decline.").mm(
						group.name
					)
				)
				group.broadcast(
					"<yellow><0> <green>has been invited by <yellow><1></yellow>.".mm(
						target.name ?: "??",
						player.name
					)
				)
			}
		}
		base.registerCopy("uninvite") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.hasPermission((ctx.sender as Player).uniqueId, Permission.Invite) }
					.map { it.name }
			})
			argument(OfflinePlayerArgument.builder<CommandSender>("target").withSuggestionsProvider { ctx, _ ->
				ctx.getOptional<String>("group")
					.flatMap { Optional.ofNullable(playerGroups[it]) }
					.map {
						if (it.hasPermission(ctx.sender as Player, Permission.Invite))
							it.invited.map { p -> Bukkit.getOfflinePlayer(p).name }
						else emptyList()
					}
					.orElseGet { emptyList() }
			})
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				val target = ctx.get<OfflinePlayer>("target")
				if (!group.hasPermission(player, Permission.Invite)) {
					player.sendMessage("<red>You do not have permission to uninvite players from that group.".mm())
					return@handler
				}
				if (group.uninvite(target.uniqueId)) {
					if (target is Player) {
						target.sendMessage(
							"<green>Your invitation to join the group <yellow><0></yellow> has been revoked.".mm(
								group.name
							)
						)
					}
					group.broadcast(
						"<yellow><0> <green>has been uninvited by <yellow><1></yellow>.".mm(
							target.name ?: "??", player.name
						)
					)
				} else player.sendMessage("<red>That player does not have an invitation to join the group.".mm())

			}
		}
		base.registerCopy("kick") {
			meta(CommandConfirmationManager.META_CONFIRMATION_REQUIRED, true)
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.hasPermission((ctx.sender as Player).uniqueId, Permission.Kick) }
					.map { it.name }
			})
			argument(OfflinePlayerArgument.builder<CommandSender>("target").withSuggestionsProvider { ctx, _ ->
				ctx.getOptional<String>("group")
					.flatMap { Optional.ofNullable(playerGroups[it]) }
					.map {
						if (it.hasPermission(ctx.sender as Player, Permission.Kick))
							it.members.keys.filter { p -> canKick(it, (ctx.sender as Player).uniqueId, p) }
								.map { p -> Bukkit.getOfflinePlayer(p).name }
						else emptyList()
					}
					.orElseGet { emptyList() }
			})
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				val target = ctx.get<OfflinePlayer>("target")
				if (!group.hasPermission(player, Permission.Kick)) {
					player.sendMessage("<red>You do not have permission to kick players in that group.".mm())
					return@handler
				}
				if (!canKick(group, player.uniqueId, target.uniqueId)) {
					player.sendMessage("<red>You cannot kick players with higher or equal rank.".mm())
					return@handler
				}
				if (group.kick(target.uniqueId)) {
					if (target is Player) target.sendMessage(
						"<green>You have been kicked from the group <yellow><0></yellow>.".mm(
							group.name
						)
					)
					group.broadcast(
						"<yellow><0> <green>has been kicked from the group by <yellow><1></yellow>.".mm(
							target.name ?: "??",
							player.name
						)
					)
				} else player.sendMessage("<red>That player does not belong to the group.".mm())

			}
		}
		base.registerCopy("listinvites") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.hasPermission((ctx.sender as Player).uniqueId, Permission.Invite) }
					.map { it.name }
			})
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				if (!group.hasPermission(player, Permission.Invite)) {
					player.sendMessage("<red>You do not have permission to view the list of invites from that group.".mm())
					return@handler
				}
				player.sendMessage(
					"<green>Invite List<gray>: <0>".mm(
						Component.join(
							JoinConfiguration.commas(true),
							group.invited.map {
								(Bukkit.getOfflinePlayer(it).name ?: it.toString()).component(NTC.YELLOW)
							}.toList()
						)
					)
				)

			}
		}
		base.registerCopy("perms") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter {
					it.hasPermission(
						(ctx.sender as Player).uniqueId,
						Permission.EditPermissions
					)
				}
					.map { it.name }
			})
			argument(EnumArgument.optional(Rank::class.java, "rank"))
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				val rank = ctx.getOptional<Rank>("rank")
				if (!group.hasPermission(player, Permission.EditPermissions)) {
					player.sendMessage("<red>You do not have permission to edit permissions in that group.".mm())
					return@handler
				}
				rank.ifPresentOrElse({ r ->
					if (r < group.rankOf(player)) {
						player.sendMessage("<red>You can only edit permissions of ranks that are lower than you.".mm())
						return@ifPresentOrElse
					}
					SimpleWindow(player, "${r.name} Permissions", editPermissionsGui(group, r)).show()
				}) {
					SimpleWindow(player, "Select Rank", selectRankPermissionsGui(player, group)).show()
				}

			}
		}
		base.registerCopy("setrank") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.hasPermission((ctx.sender as Player).uniqueId, Permission.Rank) }
					.map { it.name }
			})
			argument(OfflinePlayerArgument.builder<CommandSender>("target").withSuggestionsProvider { ctx, _ ->
				ctx.getOptional<String>("group")
					.flatMap { Optional.ofNullable(playerGroups[it]) }
					.map {
						if (it.hasPermission(ctx.sender as Player, Permission.Rank))
							it.members.filter { p -> p.value > it.rankOf(ctx.sender as Player) }
								.map { p -> Bukkit.getOfflinePlayer(p.key).name }
						else emptyList()
					}
					.orElseGet { emptyList() }
			})
			argument(EnumArgument.optional(Rank::class.java, "rank"))
			handler { ctx ->
				val player = ctx.sender as Player
				val group = getGroup(ctx) ?: return@handler
				val target = ctx.get<OfflinePlayer>("target")
				val rank = ctx.getOptional<Rank>("rank")
				if (!group.hasPermission(player, Permission.Rank)) {
					player.sendMessage("<red>You do not have permission to set ranks in that group.".mm())
					return@handler
				}
				rank.ifPresentOrElse({ r ->
					if (r < group.rankOf(player)) {
						player.sendMessage("<red>You can only set ranks that are lower than you.".mm())
						return@ifPresentOrElse
					}
					group.setRank(target.uniqueId, r)
					player.sendMessage("<green>Successfully set.".mm())
					player.performCommand("pg info ${group.name}")
				}) {
					SimpleWindow(
						player,
						"Select Rank For ${target.name}",
						selectRankMemberGui(player, group, target)
					).show()
				}

			}
		}
		base.registerCopy("chat") {
			argument(StringArgument.builder<CommandSender>("group").withSuggestionsProvider { ctx, _ ->
				playerGroups.values.filter { it.hasPermission((ctx.sender as Player).uniqueId, Permission.Chat) }
					.map { it.name } + "off"
			})
			handler { ctx ->
				val player = ctx.sender as Player
				if (ctx.get<String>("group").equals("off", true)) {
					playerGroupChats.remove(player.uniqueId)
					player.sendMessage("<green>You are no longer chatting in a player group.".mm())
					return@handler
				}
				val group = getGroup(ctx) ?: return@handler
				if (!group.hasPermission(player, Permission.Chat)) {
					player.sendMessage("<red>You do not have permission to chat in that group.".mm())
					return@handler
				}
				playerGroupChats[player.uniqueId] = group.name
				player.sendMessage("<green>You are now chatting in the group <yellow><0><green>.".mm(group.name))


			}
		}

	}

	private fun mainGui(player: Player, listAll: Boolean = false): GUI {
		return GUIBuilder(GUIType.PAGED_ITEMS)
			.setStructure(
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"u # # # # # # # d"
			)
			.addIngredient('x', Markers.ITEM_LIST_SLOT_HORIZONTAL)
			.addIngredient('#', GuiItems.Blank)
			.addIngredient('u', PreviousPage())
			.addIngredient('d', NextPage())
			.setItems((if (listAll) playerGroups.values else playerToGroups[player.uniqueId]).sortedByDescending { if (it.hasPermission(player, Permission.SeeMembers)) it.playerCount else 1 }
				.map { guiItem(player, it) })
			.build()
	}

	private fun groupGui(player: Player, group: PlayerGroup): GUI {
		val gui = GUIBuilder(GUIType.PAGED_ITEMS)
			.setStructure(
				"< p . . . i e l t",
				"u # # # # # # # d",
				"x x x x x x x x x",
				"x x x x x x x x x"
			)
			.addIngredient('x', Markers.ITEM_LIST_SLOT_HORIZONTAL)
			.addIngredient('#', GuiItems.Blank)
			.addIngredient('u', PreviousPage())
			.addIngredient('d', NextPage())
			.addIngredient('<', GuiItems.Back("pg"))
			.addIngredient(
				'p', SimpleItem(
					ItemWrapper(
						ItemStack(PAPER).withName(group.name.component(NTC.YELLOW).notItalic())
					)
				)
			)
			.setItems(
				(if (group.hasPermission(
						player,
						Permission.SeeMembers
					)
				) group.members.entries else group.members.entries.filter { it.value == Owner }).sortedBy { it.value }
					.map { MemberGuiItem(group, player, Bukkit.getOfflinePlayer(it.key)) })
		if (group.hasPermission(player, Delete)) gui.addIngredient(
			't', CloseCommandItem(
				ItemWrapper(ItemStack(TNT).withName("<!i><red>Delete Group".mm())),
				"/pg delete ${group.name}"
			)
		)
		if (group.hasPermission(player, Permission.EditPermissions)) gui.addIngredient(
			'e', CommandItem(
				ItemWrapper(ItemStack(IRON_DOOR).withName("<!i><yellow>Manage Permissions".mm())),
				"/pg perms ${group.name}"
			)
		)
		if (group.hasPermission(player, Permission.Invite)) gui.addIngredient(
			'i', SimpleItem(
				ItemWrapper(
					ItemStack(WRITABLE_BOOK).withName("<!i><yellow>Invite Player".mm()).withLore(
						listOf(
							"<!i><gray>Invite a player using <gold>/pg invite ${group.name} <player>".mm()
						)
					)
				)
			)
		)
		if (group.hasPlayer(player)) gui.addIngredient(
			'l', CloseCommandItem(
				ItemWrapper(ItemStack(OAK_DOOR).withName("<!i><red>Leave Group".mm())),
				"/pg leave ${group.name}"
			)
		)
		return gui.build()
	}

	private class MemberGuiItem(val group: PlayerGroup, val player: Player, val member: OfflinePlayer) : BaseItem() {
		override fun getItemProvider() = ItemWrapper(
			ItemStack(materialFromRank(group.rankOf(member))).let {
				val lore = mutableListOf(
					"<!i><green>Rank<gray>: <gold><0>".mm(group.rankOf(member).name)
				)

				if (canKick(group, player.uniqueId, member.uniqueId))
					lore.add("<!i><gray>Right-click to kick.".mm())
				if (canSetRank(group, player.uniqueId, member.uniqueId))
					lore.add("<!i><gray>Left-click to change rank.".mm())
				it.withLore(lore)
					.withName((member.name ?: member.uniqueId.toString()).component(NTC.YELLOW).notItalic())
			}
		)

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			if (clickType == RIGHT && canKick(group, player.uniqueId, member.uniqueId)) {
				player.closeInventory()
				player.performCommand("pg kick ${group.name} ${member.name}")
			} else if (clickType == LEFT && canSetRank(group, player.uniqueId, member.uniqueId)) {
				player.performCommand("pg setrank ${group.name} ${member.name}")
			}
		}
	}

	private fun canKick(group: PlayerGroup, kicker: UUID, kickee: UUID) =
		group.hasPermission(kicker, Permission.Kick) && group.rankOf(kicker) < group.rankOf(kickee)

	private fun canSetRank(group: PlayerGroup, ranker: UUID, rankee: UUID) =
		group.hasPermission(ranker, Permission.Rank) && group.rankOf(ranker) < group.rankOf(rankee)

	fun materialFromRank(rank: Rank) = when (rank) {
		Owner -> NETHERITE_BLOCK
		Admin -> DIAMOND_BLOCK
		Manager -> GOLD_BLOCK
		Moderator -> IRON_BLOCK
		Member -> COPPER_BLOCK
		Outsider -> STONE
	}

	private fun guiItem(player: Player, group: PlayerGroup) = CommandItem(
		ItemWrapper(
			ItemStack(
				materialFromRank(group.rankOf(player))
			).also {
				it.editMeta { m ->
					m.displayName(group.name.component(NTC.YELLOW).decoration(TD.ITALIC, false))
					m.lore(
						listOf(
							"<!i><green>Rank<gray>: <gold><0>".mm(group.rankOf(player).name),
							"<!i><green>Member Count<gray>: <gold><0>".mm(
								if (group.hasPermission(player, Permission.SeeMembers)) group.playerCount else "?"
							)
						)
					)
				}
			}
		), "/pg info ${group.name}"
	)

	private class RankGuiItem(val rank: Rank, val command: String) : BaseItem() {
		override fun getItemProvider() = ItemWrapper(
			ItemStack(materialFromRank(rank))
				.withName(rank.name.component(NTC.YELLOW).notItalic())
		)

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			player.performCommand(command)
		}
	}

	private fun selectRankPermissionsGui(player: Player, group: PlayerGroup): GUI {
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
			.addIngredient('<', GuiItems.Back("pg info ${group.name}"))
			.setItems(editable.map { RankGuiItem(it, "pg perms ${group.name} ${it.name}") })
			.build()
	}

	private fun selectRankMemberGui(player: Player, group: PlayerGroup, member: OfflinePlayer): GUI {
		val rank = group.rankOf(player)
		val editable = Rank.values().filter { it.assignable && (it > rank || (it == Owner && rank == Owner)) }
		return GUIBuilder(GUIType.SCROLL_ITEMS)
			.setStructure(
				"< x x . x . x . #",
				"# . . . . . . . #",
				"# . x . x . x . #",
				"# . . . . . . . #",
				"# . x . x . x . #",
			)
			.addIngredient('x', Markers.ITEM_LIST_SLOT_HORIZONTAL)
			.addIngredient('#', GuiItems.Blank)
			.addIngredient('<', GuiItems.Back("pg info ${group.name}"))
			.setItems(editable.map { RankGuiItem(it, "pg setrank ${group.name} ${member.name} ${it.name}") })
			.build()
	}

	private class PermissionGuiItem(val permission: Permission, val group: PlayerGroup, val rank: Rank) : BaseItem() {
		override fun getItemProvider() = ItemWrapper(permission.icon.clone().also {
			it.editMeta { m ->
				val hasPermission = group.getPermissions(rank).contains(permission)
				m.lore(
					listOf(
						"<!i><gold>State<gray>: <0>".mm(
							if (hasPermission) "<green>ALLOW".mm()
							else "<red>DENY".mm()
						),
						"<!i><gold>Click to toggle.".mm()
					) + (m.lore() ?: listOf())
				)
				if (hasPermission) {
					m.addEnchant(Enchantment.DAMAGE_UNDEAD, 1, true)
					m.addItemFlags(ItemFlag.HIDE_ENCHANTS)
				}
			}
		})

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			if (!group.hasPermission(player, Permission.EditPermissions) || rank < group.rankOf(player)) return
			if (group.getPermissions(rank).contains(permission)) group.getPermissions(rank).remove(permission)
			else group.getPermissions(rank).add(permission)
			notifyWindows()
		}
	}

	private fun editPermissionsGui(group: PlayerGroup, rank: Rank): GUI {
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
			.addIngredient('<', GuiItems.Back("pg perms ${group.name}"))
			.addIngredient('#', GuiItems.Blank)
			.setItems(Permission.values().map { PermissionGuiItem(it, group, rank) })
			.build()
	}
}