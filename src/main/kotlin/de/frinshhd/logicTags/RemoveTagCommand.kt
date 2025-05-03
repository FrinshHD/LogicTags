package de.frinshhd.logicTags

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RemoveTagCommand: CommandExecutor {
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

        val player: Player = sender

        Main.playerTagManager.removeTag(player)

        Main.tagsHandler.updateText(player, null)

        player.sendMessage("Tag removed.")

        return true
    }
}