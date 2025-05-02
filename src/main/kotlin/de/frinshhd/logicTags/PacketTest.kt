package de.frinshhd.logicTags

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.player.User
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PacketTest : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val player: Player = event.player

        player.sendMessage("PacketTest: Packet sent to you!")
        Main.tagsHandler.addPlayerTag(player, "§7Test §4${player.name}", true)

    }

    fun test(player: Player) {
        Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
            val user: User = PacketEvents.getAPI().playerManager.getUser(onlinePlayer)


            val packet = WrapperPlayServerTeams(
                "customName${player.name}",
                WrapperPlayServerTeams.TeamMode.CREATE,
                WrapperPlayServerTeams.ScoreBoardTeamInfo(
                    null,
                    null,
                    null,
                    WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                    WrapperPlayServerTeams.CollisionRule.ALWAYS,
                    NamedTextColor.WHITE,
                    WrapperPlayServerTeams.OptionData.NONE
                ),
                player.name
            )

            user.sendPacket(packet)
        }
    }
}