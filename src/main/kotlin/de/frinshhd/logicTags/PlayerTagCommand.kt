package de.frinshhd.logicTags

import de.frinshhd.logicTags.utils.MessageFormat
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
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
            MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.onlyPlayers"))
            return
        }

        val tag = LogicTags.playerTagManager.getTag(sender)
        if (tag == null) {
            MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.noTagSet"))
            return
        }

        MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.currentTag", Translatable("tag", tag)))
    }

    @Command("$COMMAND_PREFIX list")
    fun tagList(sender: CommandSender) {
        val tagsMap = LogicTags.playerTagManager.getTagsMapPlayer(sender)

        if (tagsMap.isEmpty()) {
            MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.noTagsAvailable"))
            return
        }

        val tagsMessage = tagsMap.entries
            .joinToString(
                separator = "\n",
                prefix = LogicTags.translationManager.get("tagCommand.availableTags")
            ) { (id, tag) ->
                LogicTags.translationManager.get(
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
            MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.onlyPlayers"))
            return
        }

        if (id == null) {
            val availableIds = LogicTags.playerTagManager.getTagsMap().keys.joinToString(", ")
            MessageFormat.send(
                sender,
                LogicTags.translationManager.get("tagCommand.provideTagId", Translatable("availableIds", availableIds))
            )
            return
        }

        val tagDetails = LogicTags.playerTagManager.getTagsMapPlayer(sender)[id]
        if (tagDetails == null) {
            MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.invalidTagId"))
            return
        }

        LogicTags.tagsHandler.updateText(sender, tagDetails.name)
        MessageFormat.send(
            sender,
            LogicTags.translationManager.get("tagCommand.tagChanged", Translatable("tag", tagDetails.name))
        )
    }

    @Command("$COMMAND_PREFIX change <tag>")
    @Permission("${LogicTags.PERMISSION_PREFIX}.change")
    fun tagChange(sender: CommandSender, @Argument(value = "tag") @Greedy tag: String?) {
        if (sender !is Player) {
            MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.onlyPlayers"))
            return
        }

        if (tag == null || tag.isBlank()) {
            MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.provideTag"))
            return
        }

        val tagLength = PlainTextComponentSerializer.plainText().serialize(MessageFormat.build(tag)).length

        if (LogicTags.settingsManager.getMaxTagLength() > 0 && tagLength > LogicTags.settingsManager.getMaxTagLength()) {
            MessageFormat.send(
                sender,
                LogicTags.translationManager.get(
                    "tagCommand.tagTooLong",
                    Translatable("maxLength", LogicTags.settingsManager.getMaxTagLength().toString())
                )
            )
            return
        }

        LogicTags.tagsHandler.updateText(sender, tag)
        MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.tagChanged", Translatable("tag", tag)))
    }

    @Command("$COMMAND_PREFIX remove")
    fun tagRemove(sender: CommandSender) {
        if (sender !is Player) {
            MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.onlyPlayers"))
            return
        }

        LogicTags.tagsHandler.removePlayerTag(sender)
        MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.tagRemoved"))
    }

    @Command("$COMMAND_PREFIX reload")
    @Permission("${LogicTags.PERMISSION_PREFIX}.reload")
    fun tagReload(sender: CommandSender) {
        LogicTags.playerTagManager.reloadTags()
        LogicTags.settingsManager.reloadSettings()
        LogicTags.translationManager.reload()
        MessageFormat.send(sender, LogicTags.translationManager.get("tagCommand.tagsReloaded"))
    }

    @Command("$COMMAND_PREFIX help")
    fun tagHelp(sender: CommandSender) {
        val commands = listOf(
            null to LogicTags.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag"),
                Translatable("description", LogicTags.translationManager.get("tagCommand.descriptions.tag"))
            ),
            null to LogicTags.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag list"),
                Translatable("description", LogicTags.translationManager.get("tagCommand.descriptions.list"))
            ),
            null to LogicTags.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag select <id>"),
                Translatable("description", LogicTags.translationManager.get("tagCommand.descriptions.select"))
            ),
            "${LogicTags.PERMISSION_PREFIX}.change" to LogicTags.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag change <tag>"),
                Translatable("description", LogicTags.translationManager.get("tagCommand.descriptions.change"))
            ),
            null to LogicTags.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag remove"),
                Translatable("description", LogicTags.translationManager.get("tagCommand.descriptions.remove"))
            ),
            "${LogicTags.PERMISSION_PREFIX}.reload" to LogicTags.translationManager.get(
                "tagCommand.helpCommand",
                Translatable("command", "/tag reload"),
                Translatable("description", LogicTags.translationManager.get("tagCommand.descriptions.reload"))
            )
        )

        val helpMessage = buildString {
            append(LogicTags.translationManager.get("tagCommand.helpHeader"))
            commands.forEach { (permission, description) ->
                if (permission == null || sender.hasPermission(permission)) {
                    append("$description\n")
                }
            }
        }

        MessageFormat.send(sender, helpMessage.trim())
    }
}