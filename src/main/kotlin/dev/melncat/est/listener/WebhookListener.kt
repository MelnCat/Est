package dev.melncat.est.listener

import dev.melncat.est.util.config
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import io.papermc.paper.advancement.AdvancementDisplay.Frame.CHALLENGE
import io.papermc.paper.advancement.AdvancementDisplay.Frame.GOAL
import io.papermc.paper.advancement.AdvancementDisplay.Frame.TASK
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

@RegisterListener
object WebhookListener : FListener {
	private val client = HttpClient.newHttpClient()
	private val serializer = PlainTextComponentSerializer.plainText()

	private val defaultAllowedMentions: Map<String, List<String>> = mapOf(
		"parsed" to listOf(),
		"users" to listOf(),
		"roles" to listOf()
	)

	@Serializable
	private data class WebhookBody(
		val content: String,
		@SerialName("allowed_mentions")
		val allowedMentions: Map<String, List<String>>
	)

	 fun sendMessage(message: String) {
		 for (url in config.otherWebhooks) {
			 val req = HttpRequest.newBuilder(URI.create(url))
				 .POST(BodyPublishers.ofString(Json.encodeToString(WebhookBody(message, defaultAllowedMentions))))
				 .header("Content-Type", "application/json")
				 .build()
			 client.sendAsync(req, BodyHandlers.discarding())
		 }
		val url = config.webhookUrl ?: return
		val req = HttpRequest.newBuilder(URI.create(url))
			.POST(BodyPublishers.ofString(Json.encodeToString(WebhookBody(message, defaultAllowedMentions))))
			.header("Content-Type", "application/json")
			.build()
		client.sendAsync(req, BodyHandlers.discarding())
	}

	@EventHandler(ignoreCancelled = true, priority = MONITOR)
	fun onChat(event: AsyncChatEvent) {
		sendMessage("<**${event.player.name}**> ${serializer.serialize(event.message())}")
	}

	@EventHandler(ignoreCancelled = true, priority = MONITOR)
	fun onAdvancement(event: PlayerAdvancementDoneEvent) {
		val display = event.advancement.display ?: return
		sendMessage(
			"**${event.player.name}** has ${
				when (display.frame()) {
					TASK -> "made the advancement"
					GOAL -> "reached the goal"
					CHALLENGE -> "completed the challenge"
				}
			} **[${serializer.serialize(display.title())}]** (*${serializer.serialize(display.description())}*)"
		)
	}

	@EventHandler(ignoreCancelled = true, priority = MONITOR)
	fun onDeath(event: PlayerDeathEvent) {
		val message = serializer.serializeOrNull(event.deathMessage()) ?: return
		sendMessage(message)
	}

	@EventHandler(ignoreCancelled = true, priority = MONITOR)
	fun onJoin(event: PlayerJoinEvent) {
		sendMessage("**${event.player.name}** joined the game")
	}

	@EventHandler(ignoreCancelled = true, priority = MONITOR)
	fun onQuit(event: PlayerQuitEvent) {
		sendMessage("**${event.player.name}** left the game")
	}

}
