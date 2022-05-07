package cf.melncat.est.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
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

fun String.mm(vararg placeholders: Pair<String, Any>, instance: MiniMessage = mm) = this.mm(instance)
//	instance.parse(this, placeholders.map { Template.of(it.first, it.second.toString()) })

fun String.mm(vararg placeholders: Any, instance: MiniMessage = mm) = this.mm(instance)
//	instance.parse(this, placeholders.mapIndexed { i, x -> Template.of(i.toString(), x.toString()) })
/**
 * MiniMessage (Placeholders dirty)
 */
fun String.mmr(vararg placeholders: Pair<String, String>, instance: MiniMessage = mm) = this.mm(instance)
//	instance.parse(this, *placeholders.flatMap { it.toList() }.toTypedArray())
