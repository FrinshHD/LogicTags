package de.frinshhd.logicTags

import de.frinshhd.logicTags.utils.MessageFormat
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

@Suppress("unused")
class PlayerTagCommand {

    companion object {
        const val COMMAND_PREFIX = "tag"
    }

    @Command(COMMAND_PREFIX)
    @CommandDescription("Command for LogicTags")
    fun tag(sender: CommandSender) {
        if (sender !is Player) {
            MessageFormat.send(sender, "&cThis command can only be used by players.")
            return
        }

        val tag = Main.playerTagManager.getTag(sender)
        if (tag == null) {
            MessageFormat.send(sender, "&cYou don't have a tag set.")
            return
        }

        MessageFormat.send(sender, "&7Your current tag is &r$tag&7.")
    }

    @Command("$COMMAND_PREFIX list")
    fun tagList(sender: CommandSender) {
        val tagsMap = Main.playerTagManager.getTagsMapPlayer(sender)

        if (tagsMap.isEmpty()) {
            MessageFormat.send(sender, "&cNo tags are available.")
            return
        }

        val tagsMessage = tagsMap.entries
            .joinToString(separator = "\n", prefix = "&7Available Tags:\n") { (id, tag) ->
                "&7ID: &d$id &7- Name: &d${tag.name}"
            }

        MessageFormat.send(sender, tagsMessage)
    }

    @Command("$COMMAND_PREFIX select <id>")
    fun tagSelect(sender: CommandSender, @Argument("id") id: String?) {
        if (sender !is Player) {
            MessageFormat.send(sender, "&cThis command can only be used by players. But you entered the id: &d$id")
            return
        }

        if (id == null) {
            val availableIds = Main.playerTagManager.getTagsMap().keys.joinToString(", ")
            MessageFormat.send(sender, "&cPlease provide a tag ID. Available IDs: &d$availableIds")
            return
        }

        val tagDetails = Main.playerTagManager.getTagsMapPlayer(sender)[id]
        if (tagDetails == null) {
            MessageFormat.send(sender, "&cInvalid tag ID. Use one of the available IDs.")
            return
        }

        Main.tagsHandler.updateText(sender, tagDetails.name)
        MessageFormat.send(sender, "&7Tag changed to &d${tagDetails.name}&7.")
    }

    @Command("$COMMAND_PREFIX change <tag>")
    @Permission("${Main.PERMISSION_PREFIX}.change")
    fun tagChange(sender: CommandSender, @Argument(value = "tag") @Greedy tag: String?) {
        if (sender !is Player) {
            MessageFormat.send(sender, "&cThis command can only be used by players. But you entered the tag: &r$tag")
            return
        }

        if (tag == null || tag.isBlank()) {
            MessageFormat.send(sender, "&cPlease provide a tag.")
            return
        }

        if (Main.settingsManager.getMaxTagLength() > 0 && tag.length > Main.settingsManager.getMaxTagLength()) {
            MessageFormat.send(
                sender,
                "&cTag is too long. Maximum length is ${Main.settingsManager.getMaxTagLength()} characters."
            )
            return
        }

        Main.tagsHandler.updateText(sender, tag)
        MessageFormat.send(sender, "&7Tag changed to &d$tag&7.")
    }

    @Command("$COMMAND_PREFIX remove")
    fun tagRemove(sender: CommandSender) {
        if (sender !is Player) {
            MessageFormat.send(sender, "&cThis command can only be used by players.")
            return
        }

        Main.tagsHandler.removePlayerTag(sender)
        MessageFormat.send(sender, "&7Tag removed.")
    }

    @Command("$COMMAND_PREFIX reload")
    @Permission("${Main.PERMISSION_PREFIX}.reload")
    fun tagReload(sender: CommandSender) {
        Main.playerTagManager.reloadTags()
        Main.settingsManager.reloadSettings()
        MessageFormat.send(sender, "&7Tags reloaded.")
    }

    @Command("$COMMAND_PREFIX help")
    fun tagHelp(sender: CommandSender) {
        val commands = listOf(
            null to "&7/tag &7- View your current tag",
            null to "&7/tag list &7- List all available tags",
            null to "&7/tag select <id> &7- Select a tag by ID",
            "${Main.PERMISSION_PREFIX}.change" to "&7/tag change <tag> &7- Change your tag to the specified text",
            null to "&7/tag remove &7- Remove your current tag",
            "${Main.PERMISSION_PREFIX}.reload" to "&7/tag reload &7- Reload the tags configuration"
        )

        val helpMessage = buildString {
            append("&dLogicTags Help:\n&r")
            commands.forEach { (permission, description) ->
                if (permission == null || sender.hasPermission(permission)) {
                    append("$description\n")
                }
            }
        }

        MessageFormat.send(sender, helpMessage.trim())
    }
}