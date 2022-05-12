package cf.melncat.est.command

import cf.melncat.est.util.NTC
import cf.melncat.est.util.defaultSelectors
import cf.melncat.est.util.div
import cf.melncat.est.util.matchEntityTypeOrThrow
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.PolyglotException
import java.awt.SystemColor.text
import kotlin.reflect.KClass

@RegisterCommand
object EvalCommand : BaseCommand(
	"eval"
) {
	private val graalContext: Context

	init {
		val oldClassLoader = Thread.currentThread().contextClassLoader
		Thread.currentThread().contextClassLoader = KClass::class.java.classLoader
		graalContext = Context.newBuilder("js")
			.hostClassLoader(KClass::class.java.classLoader)
			.option("engine.WarnInterpreterOnly", "false")
			.allowAllAccess(true).build()
		Thread.currentThread().contextClassLoader = oldClassLoader
		graalContext.eval("js", "const{Bukkit}=org.bukkit")
	}

	override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
		val data = args.joinToString(" ")
		graalContext.getBindings("js").putMember("player", sender)
		try {
			val result = graalContext.eval("js", data)
			sender.sendMessage(result.toString())
		} catch (e: PolyglotException) {
			sender.sendMessage(e.localizedMessage / NTC.RED)
		}
		return true
	}
}