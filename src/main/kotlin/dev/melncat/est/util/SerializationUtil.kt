package dev.melncat.est.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.SnbtPrinterTagVisitor
import net.minecraft.nbt.TagParser
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

object NamespacedKeySerializer : KSerializer<NamespacedKey> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("key", PrimitiveKind.STRING)
	override fun serialize(encoder: Encoder, value: NamespacedKey) = encoder.encodeString(value.asString())
	override fun deserialize(decoder: Decoder): NamespacedKey =
		NamespacedKey.fromString(decoder.decodeString()) ?: throw IllegalArgumentException("Invalid NamespacedKey.")
}

fun ItemStack.serializeToString(): String = SnbtPrinterTagVisitor().visit(CraftItemStack.asNMSCopy(this).save(CompoundTag()))

fun itemFromString(str: String): ItemStack = CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.of(TagParser.parseTag(str)))

