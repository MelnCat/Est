package cf.melncat.est

import cf.melncat.est.command.BaseCommand
import cf.melncat.est.command.RegisterCommand
import cf.melncat.est.command.tickParabolas
import cf.melncat.est.itemproxy.setupItemProxy
import cf.melncat.est.listener.tickWeaponArtCooldowns
import cf.melncat.est.util.armorEffectTick
import cf.melncat.est.util.changeBlastResistance
import cf.melncat.est.util.getRegistration
import cf.melncat.est.util.loadConfig
import cf.melncat.est.util.loadSocialCredit
import cf.melncat.est.util.runTaskLater
import cf.melncat.est.util.runTaskTimer
import cf.melncat.est.util.saveSocialCredit
import cf.melncat.est.weaponarts.defaultWeaponArts
import cf.melncat.est.weaponarts.weaponArtTick
import cf.melncat.furcation.plugin.FPlugin
import com.comphenix.protocol.ProtocolLibrary
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.reflections.Reflections
import org.reflections.scanners.Scanners.TypesAnnotated
import kotlin.reflect.KClass

lateinit var plugin: Est private set
lateinit var eco: Economy private set

class Est : FPlugin("cf.melncat.est") {
	override fun enable() {
		plugin = this
		eco = server.servicesManager.getRegistration<Economy>()!!.provider
		loadConfig()
		val map = Bukkit.getServer().commandMap
		logger.info(FPlugin::class.java.classLoader.toString())
		logger.info(KClass::class.java.classLoader.toString())
		logger.info(Reflections::class.java.classLoader.toString())
		val commands =
			reflections.get(TypesAnnotated.with(RegisterCommand::class.java).asClass<Any>())
				.map { it.kotlin.objectInstance!! as BaseCommand }
		server.scheduler.runTaskLater(20) {
			commands.forEach {
				it.register(map)
			}
		}
		changeBlastResistance()
		defaultWeaponArts()
		server.scheduler.runTaskTimer(0, 5, ::armorEffectTick)
		server.scheduler.runTaskTimer(0, 1, ::tickParabolas)
		server.scheduler.runTaskTimer(0, 1, ::weaponArtTick)
		server.scheduler.runTaskTimer(0, 1, ::tickWeaponArtCooldowns)
		setupItemProxy()
		runBlocking {
			launch {
				loadSocialCredit()
				server.scheduler.runTaskTimer(0, 20 * 60 * 10, ::saveSocialCredit)
			}
		}
	}

	override fun disable() {
		ProtocolLibrary.getProtocolManager().removePacketListeners(this)
	}
}
