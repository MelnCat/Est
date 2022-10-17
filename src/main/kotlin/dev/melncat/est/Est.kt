package dev.melncat.est

import dev.melncat.est.command.tickParabolas
import dev.melncat.est.listener.tickCampfireResting
import dev.melncat.est.listener.tickWeaponArtCooldowns
import dev.melncat.est.util.changeBlastResistance
import dev.melncat.est.util.getRegistration
import dev.melncat.est.util.loadConfig
import dev.melncat.est.util.loadSocialCredit
import dev.melncat.est.util.runTaskTimer
import dev.melncat.est.util.saveSocialCredit
import dev.melncat.est.util.tickItemEffects
import dev.melncat.est.weaponarts.defaultWeaponArts
import dev.melncat.est.weaponarts.tickWeaponArts
import dev.melncat.furcation.plugin.FPlugin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.milkbowl.vault.economy.Economy

lateinit var plugin: Est private set
lateinit var eco: Economy private set

class Est : FPlugin("dev.melncat.est") {
	override fun enable() {
		plugin = this
		eco = server.servicesManager.getRegistration<Economy>()!!.provider
		loadConfig()
		changeBlastResistance()
		defaultWeaponArts()
		server.scheduler.runTaskTimer(0, 5, ::tickItemEffects)
		server.scheduler.runTaskTimer(0, 1, ::tickParabolas)
		server.scheduler.runTaskTimer(0, 1, ::tickWeaponArts)
		server.scheduler.runTaskTimer(0, 1, ::tickWeaponArtCooldowns)
		server.scheduler.runTaskTimer(0, 1, ::tickCampfireResting)
		runBlocking {
			launch {
				loadSocialCredit()
				server.scheduler.runTaskTimer(0, 20 * 60 * 10, ::saveSocialCredit)
			}
		}
	}
}
