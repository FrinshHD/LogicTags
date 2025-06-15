package de.frinshhd.logicTags

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class SettingsManager {

    private val configFile = File(LogicTags.instance.dataFolder, "config.yml")
    private val config = YamlConfiguration()

    private val defaultSettings = mapOf(
        "customTeams" to true,
        "seeOwnTag" to false,
        "maxTagLength" to 20,
        "tagInfoJoinMessage" to false,
        "language" to "en",
    )

    init {
        setupFile()
    }

    fun <T : Any> getSetting(key: String, clazz: Class<T>): T {
        val value = config.get(key)
        val default = defaultSettings[key]

        @Suppress("UNCHECKED_CAST")
        return when {
            clazz == Boolean::class.java -> when (value) {
                is Boolean -> value as T
                is String -> value.toBoolean() as T
                is Number -> (value.toInt() != 0) as T
                else -> (default as? Boolean ?: false) as T
            }

            clazz == Int::class.java -> when (value) {
                is Int -> value as T
                is Number -> value.toInt() as T
                is String -> value.toIntOrNull() as? T ?: (default as? Int ?: 0) as T
                else -> (default as? Int ?: 0) as T
            }

            clazz == String::class.java -> value?.toString() as T? ?: (default as? String ?: "") as T
            clazz.isInstance(value) -> clazz.cast(value)
            clazz.isInstance(default) -> clazz.cast(default)
            else -> clazz.getDeclaredConstructorOrNull()?.newInstance()
                ?: error("Cannot provide default for type: ${clazz.name}")
        }
    }

    // Helper extension for safe constructor access
    private fun <T> Class<T>.getDeclaredConstructorOrNull() = try {
        getDeclaredConstructor()
    } catch (_: Exception) {
        null
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
    fun isTagInfoJoinMessage(): Boolean = getSetting("tagInfoJoinMessage", Boolean::class.java)
    fun getLanguage(): String = getSetting("language", String::class.java)
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