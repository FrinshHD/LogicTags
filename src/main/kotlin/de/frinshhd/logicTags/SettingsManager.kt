package de.frinshhd.logicTags

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class SettingsManager {

    private val configFile = File(Main.instance.dataFolder, "config.yml")
    private val config = YamlConfiguration()

    private val defaultSettings = mapOf(
        "customTeams" to true,
        "seeOwnTag" to false,
        "maxTagLength" to 20,
    )

    init {
        setupFile()
    }

    fun <T : Any> getSetting(key: String, clazz: Class<T>): T {
        val value = config.get(key, defaultSettings[key])
        val default = defaultSettings[key]

        return when {
            clazz.isInstance(value) -> clazz.cast(value)
            clazz.isInstance(default) -> clazz.cast(default)
            clazz == Boolean::class.java -> false as T
            clazz == Int::class.java -> 0 as T
            clazz == String::class.java -> "" as T
            else -> throw IllegalStateException("Unsupported type for key: $key")
        }
    }

    fun setSetting(key: String, value: Any?) {
        config.set(key, value)
        saveConfig()
    }

    fun reloadSettings() {
        config.load(configFile)
    }

    fun hasCustomTeams(): Boolean = getSetting("customTeams", Boolean::class.java)

    fun isSeeOwnTag(): Boolean = getSetting("seeOwnTag", Boolean::class.java)
    fun getMaxTagLength(): Int = getSetting("maxTagLength", Int::class.java)

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