package dev.melncat.est.listener

import dev.melncat.furcation.plugin.loaders.FListener
import dev.melncat.furcation.plugin.loaders.RegisterListener
import dev.melncat.furcation.util.component
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.LOW
import org.bukkit.event.block.SignChangeEvent

@RegisterListener
object SignListener : FListener {
	@EventHandler(priority = LOW)
	fun onSignChange(event: SignChangeEvent) {
		if (PlainTextComponentSerializer.plainText().serialize(event.line(0) ?: Component.empty()) == "[marker]")
			event.line(0, Component.text("[dynmap]"))
	}
}