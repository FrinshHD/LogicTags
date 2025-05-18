package de.frinshhd.logicTags

import com.github.retrooper.packetevents.PacketEvents
import de.frinshhd.logicTags.utils.DynamicListeners
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.incendo.cloud.paper.PaperCommandManager
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.util.logging.Level


class Main : JavaPlugin() {

    private lateinit var commandManager: LegacyPaperCommandManager<CommandSender>
    private lateinit var annotationParser: AnnotationParser<CommandSender>
    private lateinit var manager: PaperCommandManager<CommandSender>

    companion object {
        lateinit var instance: Main
            private set
        lateinit var tagsHandler: TagsHandler
            private set
        lateinit var playerTagManager: PlayerTagManager
            private set

        lateinit var settingsManager: SettingsManager
            private set
    }

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        instance = this

        setup()

        // Register commands
        Bukkit.getPluginCommand("changetag")?.setExecutor(ChangeTagCommand())
        Bukkit.getPluginCommand("removetag")?.setExecutor(RemoveTagCommand())

        println("LogicTags enabled!")
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().forEach { player -> player.kickPlayer("Server reloading, please rejoin!") }

        PacketEvents.getAPI().terminate()
    }

    private fun setup() {
        PacketEvents.getAPI().init()

        saveDefaultConfig()

        instance.logger.level = Level.ALL


        // Find plugin class names for dynamic loading
        val fullCanonicalName = instance.javaClass.canonicalName
        val canonicalName = fullCanonicalName.substring(0, fullCanonicalName.lastIndexOf("."))

        val reflections = Reflections(canonicalName, Scanners.SubTypes)
        val classNames = reflections.getAll(Scanners.SubTypes)

        DynamicListeners.load(classNames, canonicalName)

        settingsManager = SettingsManager()

        tagsHandler = TagsHandler()
        playerTagManager = PlayerTagManager()

        setupCommands()
    }


    private fun setupCommands() {
        commandManager = LegacyPaperCommandManager.createNative(instance, ExecutionCoordinator.simpleCoordinator());

        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER))
            commandManager.registerBrigadier();

        annotationParser = AnnotationParser(commandManager, CommandSender::class.java)

        annotationParser.parse(PlayerTagCommands())
    }
}
