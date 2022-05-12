package cf.melncat.est

import cf.melncat.est.command.BaseCommand
import cf.melncat.est.command.RegisterCommand
import cf.melncat.est.command.tickParabolas
import cf.melncat.est.itemproxy.setupItemProxy
import cf.melncat.est.listener.ChatListener
import cf.melncat.est.listener.DamageListener
import cf.melncat.est.listener.DispenserListener
import cf.melncat.est.listener.ItemListener
import cf.melncat.est.util.armorEffectTick
import cf.melncat.est.util.changeBlastResistance
import cf.melncat.est.util.getRegistration
import cf.melncat.est.util.loadConfig
import cf.melncat.est.util.loadSocialCredit
import cf.melncat.est.util.reflections
import cf.melncat.est.util.runTaskTimer
import cf.melncat.est.util.saveSocialCredit
import com.comphenix.protocol.ProtocolLibrary
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.scanners.Scanners.TypesAnnotated

lateinit var plugin: Est private set
lateinit var eco: Economy private set

class Est : JavaPlugin() {
	override fun onEnable() {
		plugin = this
		eco = server.servicesManager.getRegistration<Economy>()!!.provider
		loadConfig()
		val map = Bukkit.getServer().commandMap
		val commands =
			reflections.get(TypesAnnotated.with(RegisterCommand::class.java).asClass<Any>())
				.map { it.kotlin.objectInstance!! as BaseCommand }
		commands.forEach {
			it.register(map)
		}
		changeBlastResistance()
		server.pluginManager.registerEvents(DamageListener, this)
		server.pluginManager.registerEvents(ItemListener, this)
		server.pluginManager.registerEvents(DispenserListener, this)
		server.pluginManager.registerEvents(ChatListener, this)
		server.scheduler.runTaskTimer(0, 5, ::armorEffectTick)
		server.scheduler.runTaskTimer(0, 1, ::tickParabolas)
		setupItemProxy()
		runBlocking {
			launch {
				loadSocialCredit()
				server.scheduler.runTaskTimer(0, 20 * 60 * 10, ::saveSocialCredit)
			}
		}
	}

	override fun onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListeners(this)
	}
}
