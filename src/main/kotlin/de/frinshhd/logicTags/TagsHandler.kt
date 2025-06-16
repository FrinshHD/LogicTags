package de.frinshhd.logicTags

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.User
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import de.frinshhd.logicTags.utils.MessageFormat
import de.frinshhd.logicTags.utils.PlayerHashMap
import de.frinshhd.logicTags.utils.PlayerHashSet
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta
import me.tofaa.entitylib.meta.display.TextDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.spigotmc.event.entity.EntityDismountEvent
import org.spigotmc.event.entity.EntityMountEvent

class TagsHandler {

    val tagsMap: PlayerHashMap<Player, TagData> = PlayerHashMap()

    init {
        PacketEvents.getAPI().eventManager.registerListener(TagsHandlerPacketListener(), PacketListenerPriority.NORMAL)
    }

    fun addPlayerTag(player: Player, text: String?) {
        val tagData = TagData(SpigotReflectionUtil.generateEntityId(), text)
        tagsMap.put(player, tagData)
    }

    fun spawnPlayers(player: Player, players: List<Player>) {
        players.forEach { otherPlayers ->
            if (otherPlayers == player && !LogicTags.settingsManager.isSeeOwnTag()) return@forEach

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
    fun spawnTextDisplay(player: Player, id: Int, playerToMount: Player, text: String?) {
        val user: User = PacketEvents.getAPI().playerManager.getUser(player)

        if (playerToMount != player && LogicTags.settingsManager.hasCustomTeams()) {
            val teamName = "customName${playerToMount.name}"
            val scoreboard = player.scoreboard
            val alreadyInTeam = scoreboard.getTeam(teamName)?.hasEntry(playerToMount.name) == true
            if (!alreadyInTeam) {
                user.sendPacket(
                    WrapperPlayServerTeams(
                        teamName,
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
                    )
                )
            }
        }

        val location = playerToMount.location.clone()
        location.add(0.0, playerToMount.height, 0.0)

        val holo = WrapperEntity(id, EntityTypes.TEXT_DISPLAY)

        val holoMeta = holo.entityMeta as TextDisplayMeta

        if (text != null) holoMeta.text = Component.text("${MessageFormat.build(text)}\n\n")

        holoMeta.apply {
            isShadow = false
            billboardConstraints = AbstractDisplayMeta.BillboardConstraints.VERTICAL
            backgroundColor = 0
        }


        holo.spawn(SpigotConversionUtil.fromBukkitLocation(location).apply {
            pitch = 0F
            yaw = 0F
        })

        holo.addViewer(user)

        // MountPacket
        val mountPacket = WrapperPlayServerSetPassengers(playerToMount.entityId, intArrayOf(id))

        user.sendPacket(mountPacket)

        tagsMap[playerToMount]?.players?.add(player)
    }

    fun updateText(player: Player, text: String?) {
        val tagData = tagsMap[player] ?: return

        tagData.text = text

        ArrayList(tagData.players).forEach { otherPlayer ->
            spawnTextDisplay(otherPlayer, tagData.entityId, player, text)
        }

        LogicTags.playerTagManager.setTag(player, text)
    }

    fun removePlayerTagForPlayer(player: Player, tagData: TagData, isPassive: Boolean = false) {
        val user = PacketEvents.getAPI().playerManager.getUser(player) ?: return

        val entityRemovePacket = WrapperPlayServerDestroyEntities(tagData.entityId)

        user.sendPacket(entityRemovePacket)

        if (!isPassive) tagData.players.remove(player)
    }

    fun removePlayerTag(player: Player) {
        val tagData = tagsMap[player] ?: return

        removePlayerTag(tagData, true)

        tagsMap[player]?.text = null
        LogicTags.playerTagManager.removeTag(player)
    }

    fun removePlayerTag(tagData: TagData, isPassive: Boolean = false) {
        tagData.players.forEach { player ->
            removePlayerTagForPlayer(player, tagData, isPassive)
        }
    }
}

class TagsHandlerPacketListener : PacketListener {

    @Override
    override fun onPacketSend(event: PacketSendEvent) {
        val player: Player = event.getPlayer() ?: return

        when (event.packetType) {
            PacketType.Play.Server.SPAWN_ENTITY -> {
                val spawnEntityPacket = WrapperPlayServerSpawnEntity(event)

                if (spawnEntityPacket.entityType != EntityTypes.PLAYER) return

                Bukkit.getScheduler().runTask(LogicTags.instance, Runnable {
                    LogicTags.tagsHandler.spawnPlayers(
                        event.getPlayer(),
                        listOf(Bukkit.getPlayer(spawnEntityPacket.uuid.get()) ?: return@Runnable)
                    )
                })
            }

            PacketType.Play.Server.DESTROY_ENTITIES -> {

                val destroyEntitiesPacket = WrapperPlayServerDestroyEntities(event)

                destroyEntitiesPacket.entityIds.forEach { entityId ->
                    val entity: Entity = SpigotConversionUtil.getEntityById(player.world, entityId) ?: return@forEach

                    if (entity.type != EntityType.PLAYER) return@forEach

                    val playerToDestroy: Player = entity as Player

                    val tagData: TagData = LogicTags.tagsHandler.tagsMap[playerToDestroy] ?: return@forEach

                    LogicTags.tagsHandler.removePlayerTagForPlayer(player, tagData)
                }
            }

            PacketType.Play.Server.SET_PASSENGERS -> {
                val setPassengersPacket = WrapperPlayServerSetPassengers(event)
            }
        }
    }

    @Override
    override fun onPacketReceive(event: PacketReceiveEvent) {

        when (event.packetType) {
            PacketType.Play.Client.ENTITY_ACTION -> {

                val entityActionPacket = WrapperPlayClientEntityAction(event)

                if (entityActionPacket.action == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
                    val tagData = LogicTags.tagsHandler.tagsMap[event.getPlayer()] ?: return
                    LogicTags.tagsHandler.removePlayerTag(tagData, true)
                }

                if (entityActionPacket.action == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
                    val tagData = LogicTags.tagsHandler.tagsMap[event.getPlayer()] ?: return

                    Bukkit.getScheduler().runTaskLater(LogicTags.instance, Runnable {
                        tagData.players.forEach { player ->
                            LogicTags.tagsHandler.spawnTextDisplay(
                                player,
                                tagData.entityId,
                                event.getPlayer(),
                                tagData.text
                            )
                        }
                    }, 3L)
                }
            }
        }

    }
}

class TagsHandlerListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player: Player = event.getPlayer()

        val tagData = LogicTags.tagsHandler.tagsMap[player] ?: return

        LogicTags.tagsHandler.removePlayerTag(tagData)
    }

    @EventHandler
    fun onMountEvent(event: EntityMountEvent) {
        val mounted: Player = event.mount as? Player ?: return

        val tagData = LogicTags.tagsHandler.tagsMap[mounted] ?: return
        LogicTags.tagsHandler.removePlayerTag(tagData, true)
    }

    @EventHandler
    fun onUnmountEvent(event: EntityDismountEvent) {
        val mounted: Player = event.dismounted as? Player ?: return
        val tagData = LogicTags.tagsHandler.tagsMap[mounted] ?: return

        Bukkit.getScheduler().runTask(LogicTags.instance, Runnable {
            Bukkit.getScheduler().runTaskLater(LogicTags.instance, Runnable {
                tagData.players.forEach { viewer ->
                    LogicTags.tagsHandler.spawnTextDisplay(
                        viewer,
                        tagData.entityId,
                        mounted,
                        tagData.text
                    )
                }
            }, 5L)
        })
    }
}

data class TagData(
    val entityId: Int,
    var text: String?,
    val players: PlayerHashSet<Player> = PlayerHashSet()
)
