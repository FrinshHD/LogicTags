package de.frinshhd.logicTags

import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TranslationManager(
    private val languagesDir: File = File(LogicTags.instance.dataFolder, "languages"),
    private val defaultLang: String = "en"
) {
    private val translations = mutableMapOf<String, Map<String, String>>()

    private var lang: String = defaultLang

    init {
        ensureDefaultLanguageFile()
        loadLanguages()

        setLanguage(LogicTags.settingsManager.getLanguage())
    }

    private fun ensureDefaultLanguageFile() {
        if (!languagesDir.exists()) languagesDir.mkdirs()
        val defaultFile = File(languagesDir, "$defaultLang.yml")
        if (!defaultFile.exists()) {
            LogicTags.instance.getResource("languages/$defaultLang.yml")?.use { input ->
                defaultFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    private fun loadLanguages() {
        languagesDir.listFiles { file -> file.extension == "yml" }?.forEach { file ->
            val lang = file.nameWithoutExtension
            val config = YamlConfiguration.loadConfiguration(file)
            val map = mutableMapOf<String, String>()
            config.getKeys(true).forEach { key ->
                val value = config.getString(key)
                if (value != null) {
                    map[key] = value
                }
            }
            translations[lang] = map
        }
    }

    fun reload() {
        translations.clear()
        loadLanguages()
        setLanguage(LogicTags.settingsManager.getLanguage())
    }

    fun get(key: String, vararg placeholders: Translatable): String {
        val message = translations[lang]?.get(key)
            ?: translations[defaultLang]?.get(key)
            ?: LogicTags.instance.getResource("languages/$defaultLang.yml")?.use { input ->
                val config = YamlConfiguration.loadConfiguration(input.reader())
                config.getString(key)
            }
            ?: "%$key%"
        return replacePlaceholders(message, *placeholders)
    }

    fun send(player: CommandSender, key: String, vararg placeholders: Translatable) {
        val message = get(key, *placeholders)
        player.sendMessage(message)
    }

    private fun replacePlaceholders(message: String, vararg placeholders: Translatable): String {
        var result = message
        placeholders.forEach { placeholder ->
            result = result.replace("%${placeholder.key}%", placeholder.value)
        }
        return result
    }

    fun setLanguage(lang: String) {
        if (translations.containsKey(lang)) {
            this.lang = lang
        } else {
            LogicTags.instance.logger.info("Language '$lang' not found, using default '$defaultLang'.")
            this.lang = defaultLang
        }
    }
}

data class Translatable(
    val key: String,
    val value: String,
)