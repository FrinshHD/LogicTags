package de.frinshhd.logicTags

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ChangeTagCommand: CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("Please provide a tag.")
            return true
        }

        val player = sender
        val tag = args.joinToString(" ")

        Main.tagsHandler.updateText(player, tag)
        return true
    }
}