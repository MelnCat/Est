package cf.melncat.est.packet

import com.comphenix.protocol.PacketType.Play.Server
import com.comphenix.protocol.events.PacketContainer


class WrapperPlayServerCollect : AbstractPacket(Server.COLLECT) {
	var collectedEntityId: Int by mod(handle.integers)
	var collectorEntityId: Int by mod(handle.integers, 1)
}