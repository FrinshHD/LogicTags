package de.frinshhd.logicTags.utils

import de.frinshhd.logicTags.LogicTags
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent

class PlayerHashSet<K> : java.util.HashSet<Player>(), org.bukkit.event.Listener {
    init {
        LogicTags.instance.server.pluginManager.registerEvents(this, LogicTags.instance)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player: Player = event.getPlayer()

        this.remove(player)
    }
}
