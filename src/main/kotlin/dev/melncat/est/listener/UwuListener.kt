package dev.melncat.est.listener

import java.util.UUID
import com.destroystokyo.paper.MaterialTags
import dev.melncat.est.util.translate
import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.VillagerAcquireTradeEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.asJavaRandom

var uwuEnabled = false
val uwuEnabledUsers = mutableSetOf<UUID>()
val translatedUsers = mutableMapOf<UUID, String>()
@RegisterListener
object UwuListener : FListener {
	@EventHandler
	fun onChat(event: AsyncChatEvent) {
		if (uwuEnabled || uwuEnabledUsers.contains(event.player.uniqueId)) event.message(event.message().replaceText(TextReplacementConfig.builder()
			.match("([lr]|((?<=[b-df-hj-km-np-qs-tvx-z])(?=[aeiou])(?![aeiou]\\b)))")
			.replacement("w")
			.build()))
		if (translatedUsers.containsKey(event.player.uniqueId)) {
			val lang = translatedUsers[event.player.uniqueId] ?: return
			event.message(Component.text(
				translate(PlainTextComponentSerializer.plainText().serialize(event.message()), lang)
			))
		}
	}
}