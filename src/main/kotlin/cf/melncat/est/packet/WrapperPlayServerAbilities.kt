package cf.melncat.est.packet

import com.comphenix.protocol.PacketType.Play.Server
import org.bukkit.World
import org.bukkit.entity.Entity

class WrapperPlayServerAbilities : AbstractPacket(Server.ABILITIES) {
	init {
		handle.modifier.writeDefaults()
	}

	var invulnerable: Boolean by mod(handle.booleans, 0)
	var flying: Boolean by mod(handle.booleans, 1)
	var canFly: Boolean by mod(handle.booleans, 2)
	var instantBuild: Boolean by mod(handle.booleans, 3)
	var flySpeed: Float by mod(handle.float, 0)
	var walkSpeed: Float by mod(handle.float, 1)
}