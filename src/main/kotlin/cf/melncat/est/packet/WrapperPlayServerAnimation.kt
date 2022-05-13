package cf.melncat.est.packet

import com.comphenix.protocol.PacketType.Play.Server
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.World
import org.bukkit.entity.Entity

/**
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets
 * Copyright (C) dmulloy2 <http://dmulloy2.net>
 * Copyright (C) Kristian S. Strangeland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


class WrapperPlayServerAnimation : AbstractPacket(Server.ANIMATION) {
	var entityId: Int by mod(handle.integers)
	var animation: Int by mod(handle.integers, 1)
	fun getEntity(world: World): Entity {
		return handle.getEntityModifier(world).read(0)
	}
}