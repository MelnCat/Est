package dev.melncat.est.util

import dev.melncat.furcation.util.component
import dev.melncat.furcation.util.mm
import io.th0rgal.oraxen.items.ItemBuilder
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack
import org.purpurmc.purpur.language.Language

const val CUSTOM_ATTRIBUTE_KEY = "custom_attributes"
private val customAttributes = mutableMapOf<String, CustomAttributeType>()

object CustomAttributeRegistry : MutableMap<String, CustomAttributeType> by customAttributes {
	fun register(key: String, attribute: CustomAttributeType) {
		this[key] = attribute
	}

}

sealed class CustomAttributeType(val id: String, val name: Component) {
	open fun add(item: ItemStack, modifier: CustomAttributeModifier) {}
	open fun add(item: ItemBuilder, modifier: CustomAttributeModifier) {}
	open fun remove(item: ItemStack, modifier: CustomAttributeModifier) {}
}
class VanillaAttributeType(private val vanilla: Attribute) : CustomAttributeType(vanilla.name, Language.getLanguage().getOrDefault(vanilla).mm()) {
	override fun add(item: ItemStack, modifier: CustomAttributeModifier) {
		val name = "$id-CUSTOM"
		item.removeAttributeModifier(vanilla)
		item.addAttributeModifier(vanilla, AttributeModifier(name, modifier.amount, modifier.operation.operation))
	}
	override fun add(item: ItemBuilder, modifier: CustomAttributeModifier) {
		val name = "$id-CUSTOM"
		item.addAttributeModifiers(vanilla, AttributeModifier(name, modifier.amount, modifier.operation.operation))
	}
	override fun remove(item: ItemStack, modifier: CustomAttributeModifier) {
		item.removeAttributeModifier(vanilla)
	}
}

data class CustomAttributeModifier(val type: CustomAttributeType, val operation: CustomAttributeOperation, val amount: Double) {

}

enum class CustomAttributeOperation(val operation: AttributeModifier.Operation) {
	Add(AttributeModifier.Operation.ADD_NUMBER),
	Multiply(AttributeModifier.Operation.ADD_SCALAR),
	MultiplyStack(AttributeModifier.Operation.MULTIPLY_SCALAR_1)
}