package cf.melncat.est.util

import cf.melncat.est.packet.WrapperPlayServerAnimation
import cf.melncat.est.packet.WrapperPlayServerEntityStatus
import cf.melncat.est.plugin
import net.minecraft.util.Mth
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player


fun attackEntity(
	player: Player,
	entity: LivingEntity,
	damage: Double = 0.0,
	defaultDamage: Double = 0.0,
	knockback: Double = 0.4
) {
	entity.knockback(
		knockback,
		player.location.direction.multiply(-1),
		player
	)
	entity.damage(damage, player)
	val packet = WrapperPlayServerEntityStatus().apply {
		entityId = entity.entityId
		entityStatus = 2
	}
	packet.broadcastPacket()
	player.swingMainHand()
}