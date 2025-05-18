package de.frinshhd.logicTags

import de.frinshhd.logicTags.utils.MessageFormat
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission

class PlayerTagCommands {

    @Command("tag list")
    fun tagList(sender: CommandSender) {
        val tagsMap = Main.playerTagManager.getTagsMapPlayer(sender)

        if (tagsMap.isEmpty()) {
            MessageFormat.send(sender, "&cNo tags are available.")
            return
        }

        val tagsMessage = tagsMap.entries
            .joinToString(separator = "\n", prefix = "&6Available Tags:\n") { (id, tag) ->
                "&eID: &a$id &7- &bName: ${tag.name}"
            }

        MessageFormat.send(sender, tagsMessage)
    }

    @Command("tag select <id>")
    fun tagSelect(sender: CommandSender, @Argument("id") id: String?) {
        if (sender !is Player) {
            MessageFormat.send(sender, "&cThis command can only be used by players. But you entered the id: &e$id")
            return
        }

        if (id == null) {
            val availableIds = Main.playerTagManager.getTagsMap().keys.joinToString(", ")
            MessageFormat.send(sender, "&cPlease provide a tag ID. Available IDs: &e$availableIds")
            return
        }

        val tagDetails = Main.playerTagManager.getTagsMapPlayer(sender)[id]
        if (tagDetails == null) {
            MessageFormat.send(sender, "&cInvalid tag ID. Use one of the available IDs.")
            return
        }

        Main.tagsHandler.updateText(sender, tagDetails.name)
        MessageFormat.send(sender, "&aTag changed to &b${tagDetails.name}&a.")
    }

    @Command("tag change <tag>")
    @Permission("logictags.change")
    fun tagChange(sender: CommandSender, @Argument("tag") tag: String?) {
        if (sender !is Player) {
            MessageFormat.send(sender, "&cThis command can only be used by players. But you entered the tag: &e$tag")
            return
        }

        if (tag == null) {
            MessageFormat.send(sender, "&cPlease provide a tag.")
            return
        }

        Main.tagsHandler.updateText(sender, tag)
        MessageFormat.send(sender, "&aTag changed to &b$tag&a.")
    }

    @Command("tag remove")
    fun tagRemove(sender: CommandSender) {
        if (sender !is Player) {
            MessageFormat.send(sender, "&cThis command can only be used by players.")
            return
        }

        Main.tagsHandler.removePlayerTag(sender)
        MessageFormat.send(sender, "&aTag removed.")
    }

    @Command("tag reload")
    @Permission("logictags.reload")
    fun tagReload(sender: CommandSender) {
        Main.playerTagManager.reloadTags()
        MessageFormat.send(sender, "&aTags reloaded.")
    }

    @Command("tag help")
    fun tagHelp(sender: CommandSender) {
        val helpMessage = """
                             &6Tag Commands Help:
                             &e/tag list &7- &bList all available tags.
                             &e/tag select <id> &7- &bSelect a tag by ID.
                             &e/tag change <tag> &7- &bChange your tag to the specified tag. &8(Permission: logictags.change)
                             &e/tag remove &7- &bRemove your current tag.
                             &e/tag reload &7- &bReload the tags configuration. &8(Permission: logictags.reload)
                         """.trimIndent()

        MessageFormat.send(sender, helpMessage)
    }
}