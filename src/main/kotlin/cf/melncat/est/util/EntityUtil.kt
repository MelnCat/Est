package cf.melncat.est.util

import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector

fun LivingEntity.knockback(strength: Double, x: Double, z: Double, entity: Entity? = null)
		= (this as CraftLivingEntity).handle.knockback(strength, x, z, (entity as CraftEntity).handle)
fun LivingEntity.knockback(strength: Double, direction: Vector, entity: Entity? = null)
		= knockback(strength, direction.x, direction.z, entity)