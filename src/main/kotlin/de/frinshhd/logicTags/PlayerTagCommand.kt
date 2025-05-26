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
            MessageFormat.send(sender, Main.translationManager.get("tagCommand.onlyPlayers"))
            return
        }

        val tag = Main.playerTagManager.getTag(sender)
        if (tag == null) {
            MessageFormat.send(sender, Main.translationManager.get("tagCommand.noTagSet"))
            return
        }

        MessageFormat.send(sender, Main.translationManager.get("tagCommand.currentTag", Translatable("tag", tag)))
    }

    @Command("$COMMAND_PREFIX list")
    fun tagList(sender: CommandSender) {
        val tagsMap = Main.playerTagManager.getTagsMapPlayer(sender)

        if (tagsMap.isEmpty()) {
            MessageFormat.send(sender, Main.translationManager.get("tagCommand.noTagsAvailable"))
            return
        }

        val tagsMessage = tagsMap.entries
            .joinToString(
                separator = "\n",
                prefix = Main.translationManager.get("tagCommand.availableTags")
            ) { (id, tag) ->
                Main.translationManager.get(
                    "tagCommand.tagDetails",
                    Translatable("id", id),
                    Translatable("name", tag.name)
                )
            }

        MessageFormat.send(sender, tagsMessage)
    }

    @Command("$COMMAND_PREFIX select <id>")
    fun tagSelect(sender: CommandSender, @Argument("id") id: String?) {
        if (sender !is Player) {
            MessageFormat.send(sender, Main.translationManager.get("tagCommand.onlyPlayers"))
            return
        }

        if (id == null) {
            val availableIds = Main.playerTagManager.getTagsMap().keys.joinToString(", ")
            MessageFormat.send(
                sender,
                Main.translationManager.get("tagCommand.provideTagId", Translatable("availableIds", availableIds))
            )
            return
        }

        val tagDetails = Main.playerTagManager.getTagsMapPlayer(sender)[id]
        if (tagDetails == null) {
            MessageFormat.send(sender, Main.translationManager.get("tagCommand.invalidTagId"))
            return
        }

        Main.tagsHandler.updateText(sender, tagDetails.name)
        MessageFormat.send(
            sender,
            Main.translationManager.get("tagCommand.tagChanged", Translatable("tag", tagDetails.name))
        )
    }

    @Command("$COMMAND_PREFIX change <tag>")
    @Permission("${Main.PERMISSION_PREFIX}.change")
    fun tagChange(sender: CommandSender, @Argument(value = "tag") @Greedy tag: String?) {
        if (sender !is Player) {
            MessageFormat.send(sender, Main.translationManager.get("tagCommand.onlyPlayers"))
            return
        }

        if (tag == null || tag.isBlank()) {
            MessageFormat.send(sender, Main.translationManager.get("tagCommand.provideTag"))
            return
        }

        if (Main.settingsManager.getMaxTagLength() > 0 && tag.length > Main.settingsManager.getMaxTagLength()) {
            MessageFormat.send(
                sender,
                Main.translationManager.get(
                    "tagCommand.tagTooLong",
                    Translatable("maxLength", Main.settingsManager.getMaxTagLength().toString())
                )
            )
            return
        }

        Main.tagsHandler.updateText(sender, tag)
        MessageFormat.send(sender, Main.translationManager.get("tagCommand.tagChanged", Translatable("tag", tag)))
    }

    @Command("$COMMAND_PREFIX remove")
    fun tagRemove(sender: CommandSender) {
        if (sender !is Player) {
            MessageFormat.send(sender, Main.translationManager.get("tagCommand.onlyPlayers"))
            return
        }

        Main.tagsHandler.removePlayerTag(sender)
        MessageFormat.send(sender, Main.translationManager.get("tagCommand.tagRemoved"))
    }

    @Command("$COMMAND_PREFIX reload")
    @Permission("${Main.PERMISSION_PREFIX}.reload")
    fun tagReload(sender: CommandSender) {
        Main.playerTagManager.reloadTags()
        Main.settingsManager.reloadSettings()
        Main.translationManager.reload()
        MessageFormat.send(sender, Main.translationManager.get("tagCommand.tagsReloaded"))
    }

    @Command("$COMMAND_PREFIX help")
    fun tagHelp(sender: CommandSender) {
        val commands = listOf(
            null to Main.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag"),
                Translatable("description", Main.translationManager.get("tagCommand.descriptions.tag"))
            ),
            null to Main.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag list"),
                Translatable("description", Main.translationManager.get("tagCommand.descriptions.list"))
            ),
            null to Main.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag select <id>"),
                Translatable("description", Main.translationManager.get("tagCommand.descriptions.select"))
            ),
            "${Main.PERMISSION_PREFIX}.change" to Main.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag change <tag>"),
                Translatable("description", Main.translationManager.get("tagCommand.descriptions.change"))
            ),
            null to Main.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag remove"),
                Translatable("description", Main.translationManager.get("tagCommand.descriptions.remove"))
            ),
            "${Main.PERMISSION_PREFIX}.reload" to Main.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag reload"),
                Translatable("description", Main.translationManager.get("tagCommand.descriptions.reload"))
            )
        )

        val helpMessage = buildString {
            append(Main.translationManager.get("tagCommand.helpHeader"))
            commands.forEach { (permission, description) ->
                if (permission == null || sender.hasPermission(permission)) {
                    append("$description\n")
                }
            }
        }

        MessageFormat.send(sender, helpMessage.trim())
    }
}