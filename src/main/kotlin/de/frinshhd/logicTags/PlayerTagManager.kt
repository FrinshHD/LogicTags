package de.frinshhd.logicTags

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class PlayerTagManager() {

    private val file = File(Main.instance.dataFolder, "player_tags.yml")
    private val config = YamlConfiguration()

    init {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        config.load(file)
    }

    fun getTag(player: Player): String? {
        return config.getString("tags.${player.uniqueId}")
    }

    fun setTag(player: Player, tag: String) {
        config.set("tags.${player.uniqueId}", tag)
        save()
    }

    fun removeTag(player: Player) {
        config.set("tags.${player.uniqueId}", null)
        save()
    }

    private fun save() {
        config.save(file)
    }
}