package de.frinshhd.logicTags

import com.github.retrooper.packetevents.PacketEvents
import de.frinshhd.logicTags.api.LogicTagsAPI
import de.frinshhd.logicTags.api.LogicTagsAPIImpl
import de.frinshhd.logicTags.utils.DynamicListeners
import de.frinshhd.logicTags.utils.MessageFormat
import de.frinshhd.logicTags.utils.Metrics
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.tofaa.entitylib.APIConfig
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.exception.NoPermissionException
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.util.logging.Level

class LogicTags : JavaPlugin() {

    private lateinit var commandManager: LegacyPaperCommandManager<CommandSender>
    private lateinit var annotationParser: AnnotationParser<CommandSender>

    companion object {
        const val PERMISSION_PREFIX = "logictags"

        lateinit var instance: LogicTags
            private set
        lateinit var tagsHandler: TagsHandler
            private set
        lateinit var playerTagManager: PlayerTagManager
            private set

        lateinit var settingsManager: SettingsManager
            private set

        lateinit var translationManager: TranslationManager
            private set

        @JvmStatic
        lateinit var api: LogicTagsAPI
            private set
    }

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().settings.checkForUpdates(false)
        PacketEvents.getAPI().load()

        api = LogicTagsAPIImpl(this)
        server.servicesManager.register(LogicTagsAPI::class.java, api, this, org.bukkit.plugin.ServicePriority.Normal)
    }

    override fun onEnable() {
        instance = this

        setup()

        logger.info("LogicTags enabled!")
    }

    override fun onDisable() {
        if (!Bukkit.getServer().isStopping)
            Bukkit.getOnlinePlayers().forEach { player -> player.kickPlayer("Server reloading, please rejoin!") }

        PacketEvents.getAPI().terminate()
    }

    private fun setup() {
        PacketEvents.getAPI().init()

        val platform = SpigotEntityLibPlatform(this)
        val settings: APIConfig? = APIConfig(PacketEvents.getAPI())
            .debugMode()
            .tickTickables()
            .usePlatformLogger()

        EntityLib.init(platform, settings)

        saveDefaultConfig()

        instance.logger.level = Level.SEVERE

        // Find plugin class names for dynamic loading
        val fullCanonicalName = instance.javaClass.canonicalName
        val canonicalName = fullCanonicalName.substring(0, fullCanonicalName.lastIndexOf("."))

        val reflections = Reflections(ConfigurationBuilder().forPackage(canonicalName).setScanners(Scanners.SubTypes))
        val classNames = reflections.getAll(Scanners.SubTypes)

        DynamicListeners.load(classNames, canonicalName)

        settingsManager = SettingsManager()

        translationManager = TranslationManager()

        tagsHandler = TagsHandler()
        playerTagManager = PlayerTagManager()

        setupCommands()

        Metrics(this, 25996)
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
