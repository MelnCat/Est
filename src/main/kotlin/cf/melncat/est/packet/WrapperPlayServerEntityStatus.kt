package cf.melncat.est.packet

import com.comphenix.protocol.PacketType.Play.Server
import org.bukkit.World
import org.bukkit.entity.Entity

class WrapperPlayServerEntityStatus : AbstractPacket(Server.ENTITY_STATUS) {
	var entityId: Int by mod(handle.integers)
	var entityStatus: Byte by mod(handle.bytes)
	fun getEntity(world: World): Entity {
		return handle.getEntityModifier(world).read(0)
	}
}