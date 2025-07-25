package de.frinshhd.logicTags

import de.frinshhd.logicTags.LogicTags.Companion.translationManager
import de.frinshhd.logicTags.utils.MessageFormat
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File

class PlayerTagManager : Listener {

    private val playerTagsFile = File(LogicTags.instance.dataFolder, "player_tags.yml")
    private val playerTagsConfig = YamlConfiguration()

    private val tagsFile = File(LogicTags.instance.dataFolder, "tags.yml")
    private val tagsConfig = YamlConfiguration()

    private val availableTags = mutableListOf<Map<String, String>>()

    init {
        setupFile(playerTagsFile, playerTagsConfig)
        setupFile(tagsFile, tagsConfig, copyFromResources = true)
        loadTags()
    }

    fun getTag(player: Player): String? =
        playerTagsConfig.getString("tags.${player.uniqueId}")

    fun setTag(player: Player, tag: String?) {
        playerTagsConfig.set("tags.${player.uniqueId}", tag)
        saveConfig(playerTagsConfig, playerTagsFile)
    }

    fun removeTag(player: Player) {
        playerTagsConfig.set("tags.${player.uniqueId}", null)
        saveConfig(playerTagsConfig, playerTagsFile)
    }

    fun getTagsMap(): Map<String, TagDetails> =
        availableTags.mapNotNull { tag ->
            val id = tag["id"] ?: return@mapNotNull null
            val name = tag["name"] ?: ""
            val description = tag["description"] ?: ""
            val permission = tag["permission"] ?: ""
            id to TagDetails(name, description, permission)
        }.toMap()

    fun getTagsMapPlayer(player: CommandSender): Map<String, TagDetails> =
        getTagsMap().filter { (_, tag) ->
            tag.permission.isEmpty() || player.hasPermission(
                tag.permission
            )
        }

    private fun setupFile(file: File, config: YamlConfiguration, copyFromResources: Boolean = false) {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            if (copyFromResources) {
                LogicTags.instance.getResource(file.name)?.use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } else {
                file.createNewFile()
            }
        }
        config.load(file)
    }

    private fun saveConfig(config: YamlConfiguration, file: File) {
        config.save(file)
    }

    private fun loadTags() {
        val tagsList = tagsConfig.getList("tags") as? List<Map<String, String>> ?: return
        availableTags.clear()
        availableTags.addAll(tagsList)
    }

    fun reloadTags() {
        tagsConfig.load(tagsFile)
        loadTags()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        LogicTags.tagsHandler.addPlayerTag(player, LogicTags.playerTagManager.getTag(player))

        if (LogicTags.settingsManager.isTagInfoJoinMessage()) {
            val tag: String? = LogicTags.playerTagManager.getTag(event.player)

            if (tag != null)
                MessageFormat.send(
                    player,
                    translationManager.get("tagInfoJoinMessage", Translatable("tag", tag))
                )

        }

        if (LogicTags.settingsManager.isSeeOwnTag())
            LogicTags.tagsHandler.spawnPlayers(event.player, listOf(event.player))
    }

}

data class TagDetails(
    val name: String,
    val description: String,
    val permission: String
)