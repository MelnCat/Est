package dev.melncat.est.util

import org.bukkit.EntityEffect.HURT
import org.bukkit.GameMode.CREATIVE
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player


fun Player.attackWith(
	entity: LivingEntity,
	damage: Double = 0.0,
	defaultDamage: Double = 0.0,
	knockback: Double = 0.4,
	melee: Boolean = false
) {
	if (entity is Player && entity.gameMode == CREATIVE) return
	if (entity == this) return
	if (entity.isInvulnerable) return
	entity.knockback(
		knockback,
		-location.direction.x,
		-location.direction.z
	)
	entity.damage(damage, this)
	entity.playEffect(HURT)
	if (melee) swingMainHand()
}

