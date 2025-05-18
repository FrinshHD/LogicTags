package de.frinshhd.logicTags

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class SettingsManager {

    private val configFile = File(Main.instance.dataFolder, "config.yml")
    private val config = YamlConfiguration()

    // Define all keys and their default values in a map
    private val defaultSettings = mapOf(
        "customTeams" to true,
        "seeOwnTag" to false
    )

    init {
        setupFile()
    }

    // Public API
    fun getSetting(key: String): Any? = config.get(key)

    fun setSetting(key: String, value: Any?) {
        config.set(key, value)
        saveConfig()
    }

    fun reloadSettings() {
        config.load(configFile)
    }

    fun hasCustomTeams(): Boolean = getSetting("customTeams") as? Boolean ?: defaultSettings["customTeams"] as Boolean

    fun isSeeOwnTag(): Boolean = getSetting("seeOwnTag") as? Boolean ?: defaultSettings["seeOwnTag"] as Boolean

    // Private helpers
    private fun setupFile() {
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            configFile.createNewFile()
        }
        config.load(configFile)

        // Add default values if they don't exist
        defaultSettings.forEach { (key, value) ->
            if (!config.contains(key)) {
                config.set(key, value)
            }
        }

        saveConfig()
    }

    private fun saveConfig() {
        config.save(configFile)
    }
}