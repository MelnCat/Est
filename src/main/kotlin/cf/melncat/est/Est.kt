package cf.melncat.est

import cf.melncat.est.command.BaseCommand
import cf.melncat.est.command.RegisterCommand
import cf.melncat.est.itemproxy.setupItemProxy
import cf.melncat.est.listener.ChatListener
import cf.melncat.est.listener.DamageListener
import cf.melncat.est.listener.DispenserListener
import cf.melncat.est.listener.ItemListener
import cf.melncat.est.util.PDC
import cf.melncat.est.util.armorEffectTick
import cf.melncat.est.util.changeBlastResistance
import cf.melncat.est.util.loadConfig
import cf.melncat.est.util.reflections
import com.comphenix.protocol.ProtocolLibrary
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.scanners.Scanners.TypesAnnotated
import kotlin.reflect.KClass


lateinit var plugin: Est private set

class Est : JavaPlugin() {
	override fun onEnable() {
		plugin = this
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
		server.scheduler.runTaskTimer(this, ::armorEffectTick, 0, 5)
		setupItemProxy()
	}

	override fun onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListeners(this)
	}
}
