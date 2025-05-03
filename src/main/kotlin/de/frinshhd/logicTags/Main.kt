package de.frinshhd.logicTags

import com.github.retrooper.packetevents.PacketEvents
import de.frinshhd.logicTags.utils.DynamicListeners
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.util.logging.Level

class Main : JavaPlugin() {

    companion object {
        lateinit var instance: Main
            private set
        lateinit var tagsHandler: TagsHandler
            private set
        lateinit var playerTagManager: PlayerTagManager
            private set

        fun getTextFormated(text: String) = ChatColor.translateAlternateColorCodes('&', text)
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

        tagsHandler = TagsHandler()
        playerTagManager = PlayerTagManager()
    }
}
