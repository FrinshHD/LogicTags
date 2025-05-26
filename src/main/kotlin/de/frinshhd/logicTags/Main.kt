package de.frinshhd.logicTags

import com.github.retrooper.packetevents.PacketEvents
import de.frinshhd.logicTags.utils.DynamicListeners
import de.frinshhd.logicTags.utils.MessageFormat
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.exception.NoPermissionException
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.util.logging.Level


class Main : JavaPlugin() {

    private lateinit var commandManager: LegacyPaperCommandManager<CommandSender>
    private lateinit var annotationParser: AnnotationParser<CommandSender>

    companion object {
        const val PERMISSION_PREFIX = "logictags"

        lateinit var instance: Main
            private set
        lateinit var tagsHandler: TagsHandler
            private set
        lateinit var playerTagManager: PlayerTagManager
            private set

        lateinit var settingsManager: SettingsManager
            private set

        lateinit var translationManager: TranslationManager
            private set
    }

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        instance = this

        setup()

        logger.info("LogicTags enabled!")
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().forEach { player -> player.kickPlayer("Server reloading, please rejoin!") }

        PacketEvents.getAPI().terminate()
    }

    private fun setup() {
        PacketEvents.getAPI().init()

        saveDefaultConfig()

        instance.logger.level = Level.SEVERE


        // Find plugin class names for dynamic loading
        val fullCanonicalName = instance.javaClass.canonicalName
        val canonicalName = fullCanonicalName.substring(0, fullCanonicalName.lastIndexOf("."))

        val reflections = Reflections(canonicalName, Scanners.SubTypes)
        val classNames = reflections.getAll(Scanners.SubTypes)

        DynamicListeners.load(classNames, canonicalName)

        settingsManager = SettingsManager()

        translationManager = TranslationManager()

        tagsHandler = TagsHandler()
        playerTagManager = PlayerTagManager()

        setupCommands()
    }


    private fun setupCommands() {
        commandManager = LegacyPaperCommandManager.createNative(instance, ExecutionCoordinator.simpleCoordinator())

        annotationParser = AnnotationParser(commandManager, CommandSender::class.java)

        annotationParser.parse(PlayerTagCommand())

        commandManager.exceptionController().registerHandler(NoPermissionException::class.java) { exception ->
            MessageFormat.sendNoPerm(exception.context().sender())
        }
    }
}
