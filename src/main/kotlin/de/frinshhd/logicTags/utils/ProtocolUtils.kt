package de.frinshhd.logicTags.utils

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.world.Location
import org.bukkit.entity.Player

object ProtocolUtils {

    fun getPlayerLocation(player: Player) =
        Location(player.location.x, player.location.y, player.location.z, player.yaw, player.pitch)

    fun getUser(player: Player) = PacketEvents.getAPI().playerManager.getUser(player)

}