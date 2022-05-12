package cf.melncat.est.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

//import net.kyori.adventure.text.minimessage.Template
//import net.kyori.adventure.text.minimessage.transformation.TransformationRegistry
//import net.kyori.adventure.text.minimessage.transformation.TransformationType

typealias TC = TextColor
typealias NTC = NamedTextColor
typealias TD = TextDecoration

private val mm = MiniMessage.miniMessage()

operator fun String.div(color: TC) = Component.text(this, color)

fun String.component(color: TC) = Component.text(this, color)

operator fun Component.plus(other: Component) = append(other)

fun String.mm(instance: MiniMessage = mm) = Component.text(this)// instance.parse(this)

fun String.mm(vararg placeholders: Pair<String, String>, parsed: Boolean = false, instance: MiniMessage = mm) =
	instance.deserialize(this, TagResolver.resolver(placeholders.map {
		if (parsed) Placeholder.parsed(it.first, it.second)
		else Placeholder.unparsed(it.first, it.second)
	}))

fun String.mm(vararg placeholders: String, parsed: Boolean = false, instance: MiniMessage = mm) =
	instance.deserialize(this, TagResolver.resolver(placeholders.mapIndexed { i, it ->
		if (parsed) Placeholder.parsed(i.toString(), it)
		else Placeholder.unparsed(i.toString(), it)
	}))