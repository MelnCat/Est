package cf.melncat.est.packet

import cf.melncat.est.util.PDCSerializable
import cf.melncat.est.util.get
import cf.melncat.est.util.set
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.reflect.StructureModifier
import org.bukkit.entity.Player
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KProperty


sealed class AbstractPacket(val handle: PacketContainer, type: PacketType) {
	constructor(type: PacketType) : this(PacketContainer(type), type)

	init {
		require(handle.type == type) { "${handle.handle} is not a packet of type $type" }
		handle.modifier.writeDefaults()
	}


	fun sendPacket(receiver: Player) {
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(
				receiver,
				handle
			)
		} catch (e: InvocationTargetException) {
			throw RuntimeException("Cannot send packet.", e)
		}
	}

	/**
	 * Send the current packet to all online players.
	 */
	fun broadcastPacket() {
		ProtocolLibrary.getProtocolManager().broadcastServerPacket(handle)
	}

	fun receivePacket(sender: Player) {
		try {
			ProtocolLibrary.getProtocolManager().recieveClientPacket(
				sender,
				handle
			)
		} catch (e: Exception) {
			throw RuntimeException("Cannot receive packet.", e)
		}
	}

	protected class StructureModifierDelegate<T>(val modifier: StructureModifier<T>, val offset: Int) {
		operator fun getValue(thisRef: AbstractPacket, property: KProperty<*>): T
			= modifier.read(offset)

		operator fun setValue(thisRef: AbstractPacket, property: KProperty<*>, value: T): StructureModifier<T>
			= modifier.write(offset, value)
	}

	protected fun <T> mod(modifier: StructureModifier<T>, offset: Int = 0)
		= StructureModifierDelegate(modifier, offset)
}
