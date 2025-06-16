package de.frinshhd.logicTags.utils

import de.frinshhd.logicTags.LogicTags
import dev.vankka.enhancedlegacytext.EnhancedLegacyText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

object MessageFormat {
    fun build(message: String): Component {
        val component = EnhancedLegacyText.get().buildComponent(message).build()
        return MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(component))
    }

    fun send(sender: CommandSender, message: String) {
        sender.sendMessage(build(message))
    }

    fun sendNoPerm(sender: CommandSender) = LogicTags.translationManager.send(sender, "noPermission")

}
