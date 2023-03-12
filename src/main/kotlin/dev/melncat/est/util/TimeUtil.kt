package dev.melncat.est.util

import org.bukkit.Bukkit
import org.bukkit.EntityEffect.HURT
import org.bukkit.GameMode.CREATIVE
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

fun syncTime() {
	if (!config.fullDay24Hour) return
	Bukkit.getWorld("world")!!.fullTime = (System.currentTimeMillis() - 1672560000000L) * 24000L / (1000L * 60L * 60L * 24L)
}