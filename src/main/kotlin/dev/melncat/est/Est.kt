package dev.melncat.est

import dev.melncat.est.classloader.EstClassLoader
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
import dev.melncat.est.weaponarts.registerDefaultWeaponArts
import dev.melncat.est.weaponarts.tickWeaponArts
import dev.melncat.furcation.plugin.FPlugin
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.papermc.paper.network.ChannelInitializeListenerHolder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.milkbowl.vault.economy.Economy
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.PluginClassLoader
import xyz.xenondevs.nova.api.Nova
import xyz.xenondevs.nova.material.NovaMaterialRegistry
import java.net.URLClassLoader

lateinit var plugin: Est private set
lateinit var eco: Economy private set

private val listenerKey = Key.key("est", "listener")

class Est : FPlugin("dev.melncat.est") {
	override fun enable() {
		// I love causing pain
		PluginClassLoader::class.java.getDeclaredField("libraryLoader").let {
			it.isAccessible = true
			println(it.get(classLoader))
			val novaPlugin = Bukkit.getPluginManager().getPlugin("Nova")!!
			val nova = novaPlugin::class.java.getDeclaredMethod("getNova").invoke(novaPlugin)::class.java.classLoader
			it.set(classLoader, it.get(classLoader))
			val libLoader = it.get(classLoader) as URLClassLoader
			it.set(classLoader, EstClassLoader(nova, libLoader))
		}
		println(NovaMaterialRegistry.values.first().item.id)
		plugin = this
		eco = server.servicesManager.getRegistration<Economy>()!!.provider
		loadConfig()
		changeBlastResistance()
		registerDefaultWeaponArts()
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

		ChannelInitializeListenerHolder.addListener(listenerKey) { channel ->
			channel.pipeline()
				.addBefore("packet_handler", "est", object : ChannelDuplexHandler() {
					override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
						run {
							if (msg is ServerboundSetCreativeModeSlotPacket) {
								val tag = msg.item.tag ?: return@run
								if (!tag.contains("PublicBukkitValues")) return@run
								val pbv = tag.getCompound("PublicBukkitValues")
								if (!pbv.contains("est:old_item")) return@run
								return super.channelRead(
									ctx,
									ServerboundSetCreativeModeSlotPacket(
										msg.slotNum,
										CraftItemStack.asNMSCopy(ItemStack.deserializeBytes(pbv.getByteArray("est:old_item")))
									)
								)
							}
						}
						super.channelRead(ctx, msg)
					}
				})
		}
	}

	override fun disable() {
		ChannelInitializeListenerHolder.removeListener(listenerKey)
	}
}
