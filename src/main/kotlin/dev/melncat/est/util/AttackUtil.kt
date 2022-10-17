package dev.melncat.est.util

import org.bukkit.EntityEffect.HURT
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
	entity.playEffect(HURT)
	player.swingMainHand()
}