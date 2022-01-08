package cf.melncat.est.command

import cf.melncat.est.util.usageError
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@RegisterCommand
object ToBundleCommand : BaseCommand(
	"tobundle"
) {
	override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
		if (sender !is Player) return sender.usageError("You must be a player.")
		val item = sender.inventory.itemInMainHand
		if (item.amount <= 0) return sender.usageError("You must hold an item.")
		val nbt = NBTItem(item).getCompound("BlockEntityTag")?.getCompoundList("Items")
			?: return sender.usageError("You must hold an item with contents.")
		val bundle = NBTItem(ItemStack(Material.BUNDLE)).also { it.getCompoundList("Items").addAll(nbt) }.item
		sender.inventory.setItemInMainHand(bundle)
		return true
	}
}