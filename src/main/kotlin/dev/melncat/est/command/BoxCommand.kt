package dev.melncat.est.command

import cloud.commandframework.arguments.standard.BooleanArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.plugin
import dev.melncat.est.util.isAir
import dev.melncat.est.util.loadConfig
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.mm
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace.EAST
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.block.BlockFace.SOUTH
import org.bukkit.block.BlockFace.WEST
import org.bukkit.block.Container
import org.bukkit.block.data.Bisected.Half.TOP
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.Stairs.Shape.OUTER_LEFT
import org.bukkit.block.data.type.Stairs.Shape.OUTER_RIGHT
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.math.floor

@RegisterCommand
object BoxCommand : FCommand {
	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("box") {
			permission = "est.command.box"
			senderType<Player>()
			argument(StringArgument.newBuilder<CommandSender>("type")
				.asOptionalWithDefault("normal")
				.withSuggestionsProvider { _, _ -> listOf("normal", "central") }
			)
			argument(StringArgument.newBuilder<CommandSender>("open")
				.asOptionalWithDefault("normal")
				.withSuggestionsProvider { _, _ -> listOf("closed", "open", "boss") }
			)
			handler { ctx ->
				val player = ctx.sender as Player
				val chunk = player.chunk
				val type = ctx.get<String>("type")
				val open = ctx.get<String>("open")
				val baseY = (floor(player.location.y / 16) * 16).toInt()
				val baseX = chunk.x * 16
				val baseZ = chunk.z * 16
				val world = player.world
				for (i in 0 until 16)
					for (j in 0 until 16)
						for (k in 0 until 16) {
							val material = if (isInner(i) && isInner(j) && isInner(k)) Material.AIR
							else if (open == "open" && (j in 1..3 && (i in 6..9 || k in 6..9))) Material.AIR
							else if (open == "open" && (j == 4 && (i in 7..8 || k in 7..8))) Material.AIR
							else if (open == "boss" && j in 1..4 && (i in 5..10 || k in 5..10)) Material . POWDER_SNOW
							else if (open == "boss" && j == 5 && (i in 6..9 || k in 6..9)) Material.POWDER_SNOW
							else if (open == "boss" && j == 6 && (i in 7..8 || k in 7..8)) Material.POWDER_SNOW
							else Material.STONE_BRICKS
							world.setType(i + chunk.x * 16, baseY + j, k + chunk.z * 16, material)
						}

				if (type == "central") {
					world.pillar(1 + chunk.x * 16, baseY, 1 + chunk.z * 16, Material.STONE_BRICKS)
					world.pillar(1 + chunk.x * 16, baseY, 14 + chunk.z * 16, Material.STONE_BRICKS)
					world.pillar(14 + chunk.x * 16, baseY, 14 + chunk.z * 16, Material.STONE_BRICKS)
					world.pillar(14 + chunk.x * 16, baseY, 1 + chunk.z * 16, Material.STONE_BRICKS)
					world.setType(2 + baseX, baseY + 1, 1 + baseZ, Material.STONE_BRICK_STAIRS)
					world.stair(2 + baseX, baseY + 1, 1 + baseZ) { it.facing = WEST }
					world.stair(1 + baseX, baseY + 1, 2 + baseZ) { it.facing = NORTH }
					world.stair(2 + baseX, baseY + 1, 2 + baseZ) { it.facing = NORTH; it.shape = OUTER_LEFT }
					world.stair(13 + baseX, baseY + 1, 1 + baseZ) { it.facing = EAST }
					world.stair(14 + baseX, baseY + 1, 2 + baseZ) { it.facing = NORTH }
					world.stair(13 + baseX, baseY + 1, 2 + baseZ) { it.facing = NORTH; it.shape = OUTER_RIGHT }
					world.stair(1 + baseX, baseY + 1, 13 + baseZ) { it.facing = SOUTH }
					world.stair(2 + baseX, baseY + 1, 14 + baseZ) { it.facing = WEST }
					world.stair(2 + baseX, baseY + 1, 13 + baseZ) { it.facing = WEST; it.shape = OUTER_LEFT }
					world.stair(14 + baseX, baseY + 1, 13 + baseZ) { it.facing = SOUTH }
					world.stair(13 + baseX, baseY + 1, 14 + baseZ) { it.facing = EAST }
					world.stair(13 + baseX, baseY + 1, 13 + baseZ) { it.facing = EAST; it.shape = OUTER_RIGHT }
				}
			}
		}
	}

	fun isInner(number: Int) = number != 0 && number != 15
	fun World.pillar(x: Int, y: Int, z: Int, type: Material) {
		for (i in 0 until 16)
			setType(x, y + i, z, type)
	}

	fun World.stair(x: Int, y: Int, z: Int, cb: (Stairs) -> Unit) {
		setType(x, y, z, Material.STONE_BRICK_STAIRS)
		val block = getBlockAt(x, y, z)
		block.blockData = block.blockData.also { cb(it as Stairs) }
		setType(x, y + 13, z, Material.STONE_BRICK_STAIRS)
		val topBlock = getBlockAt(x, y + 13, z)
		topBlock.blockData = topBlock.blockData.also { cb(it as Stairs); it.half = TOP }
	}
}