package dev.melncat.est.command

import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.paper.PaperCommandManager
import com.oracle.truffle.api.Truffle
import dev.melncat.est.playergroup.playerGroups
import dev.melncat.furcation.plugin.loaders.FCommand
import dev.melncat.furcation.plugin.loaders.RegisterCommand
import dev.melncat.furcation.util.NTC
import dev.melncat.furcation.util.component
import org.bukkit.command.CommandSender
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.PolyglotException

@RegisterCommand
object EvalCommand : FCommand {
	private val graalContext: Context

	init {
		val oldClassLoader = Thread.currentThread().contextClassLoader
		Thread.currentThread().contextClassLoader = Truffle::class.java.classLoader
		graalContext = Context.newBuilder("js")
			.hostClassLoader(Truffle::class.java.classLoader)
			.option("engine.WarnInterpreterOnly", "false")
			.allowAllAccess(true).build()
		Thread.currentThread().contextClassLoader = oldClassLoader
		graalContext.eval("js", "const{Bukkit}=org.bukkit")
	}

	override fun register(manager: PaperCommandManager<CommandSender>) {
		manager.buildAndRegister("eval") {
			permission = "est.command.eval"
			argument(StringArgument.greedy("data"))
			handler { ctx ->
				graalContext.getBindings("js").putMember("player", ctx.sender)
				graalContext.getBindings("js").putMember("playerGroups", playerGroups)
				try {
					val result = graalContext.eval("js", ctx.get<String>("data"))
					ctx.sender.sendMessage(result.toString())
				} catch (e: PolyglotException) {
					ctx.sender.sendMessage(e.localizedMessage.component(NTC.RED))
				}
			}
		}
	}
}