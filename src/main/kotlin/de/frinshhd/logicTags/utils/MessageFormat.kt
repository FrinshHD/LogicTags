package de.frinshhd.logicTags.utils

import de.frinshhd.logicTags.LogicTags
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.regex.Pattern

object MessageFormat {
    fun build(message: String): String {
        var message = message
        val pattern = Pattern.compile("#[a-fA-F0-9]{6}")
        var matcher = pattern.matcher(message)
        while (matcher.find()) {
            val hexCode = message.substring(matcher.start(), matcher.end())
            val replaceSharp = hexCode.replace('#', 'x')

            val ch = replaceSharp.toCharArray()
            val builder = StringBuilder()
            for (c in ch) {
                builder.append("&" + c)
            }

            message = message.replace(hexCode, builder.toString())
            matcher = pattern.matcher(message)
        }
        return ChatColor.translateAlternateColorCodes('&', message)
    }

    fun send(sender: CommandSender, message: String) {
        sender.sendMessage(Component.text(build(message)))
    }

    fun sendNoPerm(sender: CommandSender) = LogicTags.translationManager.send(sender, "noPermission")

}

