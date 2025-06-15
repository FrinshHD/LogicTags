package de.frinshhd.logicTags.api

import de.frinshhd.logicTags.LogicTags
import org.bukkit.entity.Player

internal class LogicTagsAPIImpl(private val logicTags: LogicTags) : LogicTagsAPI {

    override fun setPlayerTag(player: Player, tag: String?) {
        if (tag == null) {
            removePlayerTag(player)
        } else {
            LogicTags.playerTagManager.setTag(player, tag)
        }
    }

    override fun getPlayerTag(player: Player): String? {
        return LogicTags.playerTagManager.getTag(player)
    }

    override fun removePlayerTag(player: Player) {
        LogicTags.playerTagManager.removeTag(player)
    }

}