package dev.melncat.est.playergroup

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import dev.melncat.est.listener.playerGroupChats
import dev.melncat.est.playergroup.PlayerGroup.Rank.Owner
import dev.melncat.est.plugin
import dev.melncat.est.util.gson
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.TD
import dev.melncat.furcation.util.mm
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.nova.util.removeIf
import java.nio.file.Files
import java.util.EnumMap
import java.util.EnumSet
import java.util.UUID
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

val playerGroups = mutableMapOf<String, PlayerGroup>()
val playerToGroups: LoadingCache<UUID, MutableList<PlayerGroup>> = CacheBuilder.newBuilder()
	.build(object : CacheLoader<UUID, MutableList<PlayerGroup>>() {
		override fun load(player: UUID): MutableList<PlayerGroup> {
			return playerGroups.values.filter { it.hasPlayer(player) }.toMutableList()
		}
	})

data class PlayerGroup(
	val name: String,
	private val players: MutableMap<UUID, Rank>,
	private val permissions: EnumMap<Rank, EnumSet<Permission>> = EnumMap<Rank, EnumSet<Permission>>(Rank::class.java).also {
		it[Rank.Owner] = EnumSet.allOf(Permission::class.java)
		it[Rank.Admin] = EnumSet.range(Permission.Break, Permission.Kick)
		it[Rank.Manager] = EnumSet.of(
			Permission.Break,
			Permission.Place,
			Permission.Doors,
			Permission.Interact,
			Permission.Storage,
			Permission.CreateLodestone,
			Permission.PowerLodestone,
			Permission.EditLodestone,
			Permission.Chat,
			Permission.SeeMembers
		)
		it[Rank.Moderator] = EnumSet.of(
			Permission.Break,
			Permission.Place,
			Permission.Doors,
			Permission.Interact,
			Permission.Storage,
			Permission.CreateLodestone,
			Permission.PowerLodestone,
			Permission.Chat,
			Permission.SeeMembers
		)
		it[Rank.Member] = EnumSet.of(
			Permission.Break,
			Permission.Place,
			Permission.Doors,
			Permission.Interact,
			Permission.Storage,
			Permission.PowerLodestone,
			Permission.Chat,
			Permission.SeeMembers
		)
		it[Rank.Outsider] = EnumSet.of(
			Permission.Interact
		)
	}
) {
	companion object {
		fun new(name: String, owner: UUID): PlayerGroup {
			val instance = PlayerGroup(name, mutableMapOf(owner to Rank.Owner))
			playerGroups[name] = instance
			playerToGroups[owner].add(instance)
			return instance
		}
	}

	val playerCount
		get() = players.size
	val members: Map<UUID, Rank>
		get() = players
	val invited: Set<UUID>
		get() = invites
	private val invites = mutableSetOf<UUID>()


	fun rankOf(player: OfflinePlayer) = rankOf(player.uniqueId)
	fun rankOf(player: UUID) = players[player] ?: Rank.Outsider
	fun hasPermission(player: OfflinePlayer, permission: Permission) = hasPermission(player.uniqueId, permission)
	fun hasPermission(player: UUID, permission: Permission) =
		rankOf(player) == Owner || permissions[rankOf(player)]?.let { permission in it } == true

	fun hasPlayer(player: UUID) = player in players
	fun hasPlayer(player: OfflinePlayer) = hasPlayer(player.uniqueId)
	fun invite(target: UUID) = invites.add(target)
	fun uninvite(target: UUID) = invites.remove(target)

	fun getPermissions(rank: Rank): EnumSet<Permission> = permissions[rank] ?: EnumSet.noneOf(Permission::class.java)
	fun setPermissions(rank: Rank, perms: EnumSet<Permission>) {
		permissions[rank] = perms
	}

	fun setPermission(rank: Rank, perm: Permission, set: Boolean) {
		if (set) permissions[rank]?.add(perm)
		else permissions[rank]?.remove(perm)
	}

	fun join(player: UUID): Boolean {
		return if (player in invites) {
			playerToGroups[player].add(this)
			players[player] = Rank.Member
			invites.remove(player)
			true
		} else false
	}

	fun broadcast(message: Component) {
		players.keys.mapNotNull { Bukkit.getPlayer(it) }.forEach {
			it.sendMessage("<dark_gray>[<aqua><0><dark_gray>] ".mm(name).append(message))
		}
	}

	fun chat(event: AsyncChatEvent) {
		(players.keys.mapNotNull { Bukkit.getPlayer(it) } + Bukkit.getConsoleSender()).forEach {
			@Suppress("Deprecated")
			it.sendMessage(
				event.player,
				"<dark_gray>[<white><0><dark_gray>] <yellow><1><gray>: <white><2>".mm(
					name,
					event.player.name,
					event.signedMessage().message()
				)
			)
		}
	}

	fun isInvited(player: UUID) = player in invites

	fun kick(player: UUID): Boolean {
		return if (player in players) {
			players.remove(player)
			playerToGroups[player].remove(this)
			if (playerGroupChats[player] == name) playerGroupChats.remove(player)
			true
		} else false
	}

	fun delete() {
		playerToGroups.asMap().values.forEach {
			it.remove(this)
		}
		playerGroups.remove(name)
		playerGroupChats.removeIf { it.value == name }
	}

	fun setRank(player: UUID, rank: Rank) {
		if (player !in players) return
		if (rank == Rank.Owner) players.entries.find { it.value == Rank.Owner }!!.setValue(Rank.Admin)
		players[player] = rank

	}


	enum class Rank(val assignable: Boolean = true, val editable: Boolean = true) {
		Owner(editable = false),
		Admin,
		Manager,
		Moderator,
		Member,
		Outsider(false)
	}

	enum class Permission(
		val display: String,
		val description: String,
		val material: Material,
		val lodestone: Boolean = true
	) {
		Break("Break", "Allows breaking blocks.", IRON_PICKAXE),
		Place("Place", "Allows placing blocks.", BRICKS),
		Doors("Doors", "Allows using doors.", OAK_DOOR),
		Interact("Interact", "Allows using levers, buttons, etc.", LEVER),
		Storage("Storage", "Allows using chests, barrels, etc.", CHEST),
		Chat("Chat", "Allows chatting in the group.", OAK_SIGN, false),
		SeeMembers("See Members", "Allows seeing the members of the group.", PLAYER_HEAD, false),
		CreateLodestone("Create Lodestone", "Allows setting a lodestone to this group.", LODESTONE, false),
		PowerLodestone("Power Lodestone", "Allows powering lodestones from this group.", LODESTONE),
		EditLodestone("Edit Lodestone", "Allows editing lodestones from this group.", LODESTONE),
		EditPermissions("Edit Permissions", "Allows editing permissions in the group.", IRON_DOOR, false),
		Invite("Invite", "Allows inviting other players to this group.", WRITABLE_BOOK, false),
		Rank("Rank", "Allows changing the rank of players.", LEATHER_BOOTS, false),
		Kick("Kick", "Allows kicking players from the group.", LEATHER_BOOTS, false),
		Delete("Delete", "Allows deleting the group.", TNT, false);

		val icon = ItemStack(material).apply {
			editMeta {
				it.displayName(display.mm().color(NTC.YELLOW).decoration(TD.ITALIC, false))
				it.lore(
					listOf(
						description.mm().color(NTC.GRAY).decoration(TD.ITALIC, false)
					)
				)
				it.addItemFlags(*ItemFlag.values())
			}
		}
	}
}

fun loadPlayerGroups() {
	val folder = plugin.dataFolder.toPath().resolve("playerGroups")
	if (!folder.exists()) folder.createDirectories()
	Files.list(folder).forEach {
		val group = gson.fromJson(it.readText(), PlayerGroup::class.java) ?: return@forEach
		playerGroups[group.name] = group
	}
}

fun savePlayerGroups() {
	val folder = plugin.dataFolder.toPath().resolve("playerGroups")
	if (!folder.exists()) folder.createDirectories()
	Files.list(folder).forEach {
		if (it.nameWithoutExtension !in playerGroups) it.deleteExisting()
	}
	for (group in playerGroups.values) {
		val file = folder.resolve("${group.name}.json")
		if (file.notExists()) file.createFile()
		file.writeText(gson.toJson(group))

	}
}