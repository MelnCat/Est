package cf.melncat.est.command

import cf.melncat.est.util.NTC
import cf.melncat.est.util.div
import com.oracle.truffle.api.Truffle
import org.bukkit.command.CommandSender
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.PolyglotException

@RegisterCommand
object EvalCommand : BaseCommand(
	"eval"
) {
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