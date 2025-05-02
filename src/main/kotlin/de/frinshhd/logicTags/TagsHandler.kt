package de.frinshhd.logicTags

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.User
import com.github.retrooper.packetevents.wrapper.play.server.*
import de.frinshhd.logicTags.utils.PlayerHashMap
import de.frinshhd.logicTags.utils.PlayerHashSet
import de.frinshhd.logicTags.utils.ProtocolUtils
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class TagsHandler {

    val tagsMap: PlayerHashMap<Player, TagData> = PlayerHashMap()

    init {
        PacketEvents.getAPI().eventManager.registerListener(TagsHandlerPacketListener(), PacketListenerPriority.NORMAL)
    }

    fun addPlayerTag(player: Player, text: String, hasTag: Boolean = true) {
        if (hasTag) tagsMap.put(player, TagData(SpigotReflectionUtil.generateEntityId(), text))

        /*tagsMap.forEach { (tagPlayer, data) ->
            if (!player.canSee(tagPlayer) || player == tagPlayer) return@forEach

            spawnTextDisplay(player, data.entityId, tagPlayer, data.text)
        }

        if (hasTag) {
            Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
                if (onlinePlayer == player) return@forEach

                val tagData = tagsMap[player] ?: return@forEach

                spawnTextDisplay(onlinePlayer, tagData.entityId, player, tagData.text)

            }
        }*/
    }

    fun spawnPlayers(player: Player, players: List<Player>) {
        println("Spawning players for: ${player.name}")
        players.forEach { otherPlayers ->
                if (otherPlayers == player) return@forEach

                val tagData = tagsMap[otherPlayers] ?: return@forEach

                spawnTextDisplay(player, tagData.entityId, otherPlayers, tagData.text)
            }
    }

    /**
     * Spawns a text display entity for a player.
     *
     * @param player        The player for whom the text display is spawned.
     * @param id            The ID of the text display entity.
     * @param playerToMount The player to mount the text display entity on.
     */
    private fun spawnTextDisplay(player: Player, id: Int, playerToMount: Player, text: String) {
        println("Spawning text display for player: ${player.name} mounted on player $playerToMount with ID: $id")

        val user: User = ProtocolUtils.getUser(player)

        if (playerToMount != player) {
            user.sendPacket(WrapperPlayServerTeams(
                "customName${playerToMount.name}",
                WrapperPlayServerTeams.TeamMode.CREATE,
                WrapperPlayServerTeams.ScoreBoardTeamInfo(
                    Component.text(""),
                    Component.text(""),
                    Component.text(""),
                    WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                    WrapperPlayServerTeams.CollisionRule.ALWAYS,
                    NamedTextColor.WHITE,
                    WrapperPlayServerTeams.OptionData.NONE
                ),
                playerToMount.name
            ))
        }

        val spawnPacket = WrapperPlayServerSpawnEntity(
            id,
            UUID.randomUUID(),
            EntityTypes.TEXT_DISPLAY,
            SpigotConversionUtil.fromBukkitLocation(playerToMount.location).apply {
                pitch = 0F
                yaw = 0F
            },
            0F,
            0,
            null
        )

        user.sendPacket(spawnPacket)

        // MetadataPacket
        val entityData = mutableListOf(
            EntityData(15, EntityDataTypes.BYTE, 1.toByte()),
            EntityData(23, EntityDataTypes.ADV_COMPONENT, Component.text("$text\n\n")),
            EntityData(25, EntityDataTypes.INT, 0),
        )

        // MetadataPacket
        val metadataPacket = WrapperPlayServerEntityMetadata(id, entityData)
        user.sendPacket(metadataPacket)

        // MountPacket
        val mountPacket = WrapperPlayServerSetPassengers(playerToMount.entityId, intArrayOf(id))

        object : BukkitRunnable() {
            var count = 0

            override fun run() {
                if (count >= 3) {
                    cancel()
                    return
                }

                user.sendPacket(mountPacket)
                count++
            }
        }.runTaskTimer(Main.instance, 0L, 20L)

        tagsMap[playerToMount]?.players?.add(player)
    }

    fun updateText(player: Player, text: String) {
        val tagData = tagsMap[player] ?: return

        tagData.text = text

        ArrayList(tagData.players).forEach { otherPlayer ->
            spawnTextDisplay(otherPlayer, tagData.entityId, player, text)
        }
    }

    fun removePlayerTagForPlayer(player: Player, tagData: TagData) {
        val user = ProtocolUtils.getUser(player)

        val entityRemovePacket = WrapperPlayServerDestroyEntities(tagData.entityId)

        user.sendPacket(entityRemovePacket)

        tagData.players.remove(player)
    }

    fun removePlayerTag(tagData: TagData) {
        tagData.players.forEach { player ->
            removePlayerTagForPlayer(player, tagData)
        }
    }
}

class TagsHandlerPacketListener: PacketListener {

    @Override
    override fun onPacketSend(event: PacketSendEvent) {
        val player: Player = event.getPlayer() ?: return

        when (event.packetType) {
            PacketType.Play.Server.SPAWN_ENTITY -> {
                if (event.packetType != PacketType.Play.Server.SPAWN_ENTITY)
                    return;

                val spawnEntityPacket = WrapperPlayServerSpawnEntity(event)

                if (spawnEntityPacket.entityType != EntityTypes.PLAYER) return

                Bukkit.getScheduler().runTask(Main.instance, Runnable {
                    Main.tagsHandler.spawnPlayers( event.getPlayer(), listOf(Bukkit.getPlayer(spawnEntityPacket.uuid.get()) ?: return@Runnable))
                })
            }

            PacketType.Play.Server.DESTROY_ENTITIES -> {

                val destroyEntitiesPacket = WrapperPlayServerDestroyEntities(event)

                destroyEntitiesPacket.entityIds.forEach { entityId ->
                    val entity: Entity = SpigotReflectionUtil.getEntityById(player.world, entityId) ?: return@forEach

                    if (entity.type != EntityType.PLAYER) return@forEach

                    val playerToDestroy: Player = entity as Player

                    val tagData: TagData = Main.tagsHandler.tagsMap[playerToDestroy] ?: return@forEach

                    Main.tagsHandler.removePlayerTagForPlayer(player, tagData)
                }
            }
        }
    }

    /*@Override
    override fun onPacketReceive(event: PacketReceiveEvent) {

        when (event.packetType) {
            PacketType.Play.Client.ENTITY_ACTION -> {
                val entityActionPacket = WrapperPlayClientEntityAction(event)

                if (entityActionPacket.action == WrapperPlayClientEntityAction.Action.START_SNEAKING)
                    Main.tagsHandler.removePlayerTagForPlayer()
            }
        }

    }*/
}

class TagsHandlerListener: Listener {

}

data class TagData(
    val entityId: Int,
    var text: String,
    val players: PlayerHashSet<Player> = PlayerHashSet()
)
