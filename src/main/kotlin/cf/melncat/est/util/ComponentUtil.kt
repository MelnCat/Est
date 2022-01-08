package cf.melncat.est.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

typealias TC = TextColor
typealias NTC = NamedTextColor
typealias TD = TextDecoration

operator fun String.div(color: TC)
	= Component.text(this, color)

fun String.component(color: TC)
	= Component.text(this, color)
