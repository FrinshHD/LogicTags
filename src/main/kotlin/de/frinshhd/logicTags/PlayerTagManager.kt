package de.frinshhd.logicTags

import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File

class PlayerTagManager : Listener {

    private val playerTagsFile = File(Main.instance.dataFolder, "player_tags.yml")
    private val playerTagsConfig = YamlConfiguration()

    private val tagsFile = File(Main.instance.dataFolder, "tags.yml")
    private val tagsConfig = YamlConfiguration()

    private val availableTags = mutableListOf<Map<String, String>>()

    init {
        setupFile(playerTagsFile, playerTagsConfig)
        setupFile(tagsFile, tagsConfig, copyFromResources = true)
        loadTags()
    }

    // Public API
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
        getTagsMap().filter { (id, tag) ->
            tag.permission == null || tag.permission.isEmpty() || player.hasPermission(
                tag.permission
            )
        }

    // Private helpers
    private fun setupFile(file: File, config: YamlConfiguration, copyFromResources: Boolean = false) {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            if (copyFromResources) {
                Main.instance.getResource(file.name)?.use { inputStream ->
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
    fun onPlayerJoin(event: PlayerJoinEvent) =
        Main.tagsHandler.addPlayerTag(event.player, Main.playerTagManager.getTag(event.player))
}

data class TagDetails(
    val name: String,
    val description: String,
    val permission: String
)