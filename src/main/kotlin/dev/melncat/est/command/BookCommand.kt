package dev.melncat.est.command

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import dev.melncat.est.plugin
import dev.melncat.est.util.giveItems
import dev.melncat.est.util.meta
import dev.melncat.est.util.usageError
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.TD
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.map.MinecraftFont
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.math.ceil
import kotlin.math.floor

@RegisterCommand
object BookCommand : FCommand {
	private val bookDir = File(plugin.dataFolder, "books").apply { mkdirs() }
	private val font = MinecraftFont.Font
	private val maxLineWidth = font.getWidth("LLLLLLLLLLLLLLLLLLL")

	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("book", ArgumentDescription.of("Gives you a preset book.")) {
			permission = "est.command.book"
			senderType<Player>()
			argument(StringArgument.newBuilder<CommandSender>("name")
				.greedy()
				.withSuggestionsProvider { _, _ ->
					bookDir.listFiles()?.map { it.nameWithoutExtension } ?: listOf()
				}
			)
			handler { ctx ->
				val player = ctx.sender as Player
				val name = ctx.get<String>("name")
				if (name.isBlank()) {
					player.usageError("No book specified.")
					return@handler
				}
				if (name.contains("../")) {
					player.usageError("Illegal path.")
					return@handler
				}
				val file = File(bookDir, "$name.txt")
				if (!file.exists()) {
					player.usageError("No book with that name was found.")
					return@handler
				}
				val bookData = file.readLines(StandardCharsets.UTF_8)
				val bookTitle = bookData[0].replace("\uFEFF", "")
				val data = bookData.subList(2, bookData.size).joinToString("\n")
				val bookPages = getLines(data).chunked(14)
				val bookItems = bookPages.chunked(999).let { a ->
					a.mapIndexed { i, x ->
						ItemStack(WRITTEN_BOOK).meta<BookMeta> {
							displayName(
								LegacyComponentSerializer.legacyAmpersand().deserialize(name).let {
									if (a.size <= 1) it else it
										.append(Component.text(" [", NTC.GRAY))
										.append(Component.text(i + 1, NTC.YELLOW))
										.append(Component.text("/", NTC.GRAY))
										.append(Component.text(a.size, NTC.GREEN))
										.append(Component.text("]", NTC.GRAY))
								}
									.decoration(TD.ITALIC, false)
							)
							title = bookTitle
							author = bookData[1]
							addPages(
								Component.text(bookCover(name, bookData[1], i, a.size).joinToString("\n")),
								*x.map { Component.text(it.joinToString("\n")) }.toTypedArray()
							)
						}
					}
				}
				player.inventory.giveItems(bookItems)

			}
		}
	}

	private fun getLines(rawText: String): List<String> {
		var i = 0
		val ret = mutableListOf<String>()
		val currL = mutableListOf<String>()
		val str = rawText.split(Regex("\\n+")).map { it.split(Regex("\\s+")) }
			.map { l ->
				l.flatMap {
					if (getWidth(it) <= maxLineWidth) return@flatMap listOf(it)
					val r = it.toCharArray().fold(listOf("")) { l, c ->
						if (getWidth(l.last() + c) > maxLineWidth) l + c.toString()
						else l.dropLast(1) + (l.last() + c)
					}
					r
				}.toMutableList()
			}.toMutableList()
		while (str.isNotEmpty()) {
			i++
			while (str[0].isNotEmpty()) {
				i++
				if (i > 10000000) {
					throw Exception("a")
				}
				val width = getWidth((currL + str[0][0]).joinToString(" "))
				if (width > maxLineWidth) break
				currL.add(str[0].removeAt(0))
			}
			ret.add(currL.joinToString(" "))
			currL.clear()
			if (str[0].isEmpty()) str.removeAt(0)
		}
		return ret
	}

	private fun getWidth(str: String) =
		font.getWidth(str.split("").joinToString("") { if (font.isValid(it)) it else "@" })

	private val spaceWidth = getWidth(" ")

	private fun centerBookLine(str: String) =
		getWidth(str).let { " ".repeat(((maxLineWidth - it).toDouble() / 2 / spaceWidth).toInt()) }.let { "$it$str" }

	private const val hr = "==================="
	private fun bookCover(name: String, author: String, part: Int, totalParts: Int): List<String> {
		val nameLines = getLines(name).map(::centerBookLine)
		val namePadding = (7 - nameLines.size).toDouble() / 2.0
		val authorLines = getLines("> By $author").toTypedArray()
		return listOf(
			hr,
			*Array(floor(namePadding).toInt()) { "" },
			*nameLines.toTypedArray(),
			*Array(ceil(namePadding).toInt()) { "" },
			hr,
			*if (totalParts <= 1) authorLines else arrayOf("", "> Part ${part + 1} of $totalParts", "", *authorLines)
		)
	}
}