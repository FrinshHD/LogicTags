package de.frinshhd.logicTags.api

import de.frinshhd.logicTags.LogicTags
import org.bukkit.entity.Player

internal class LogicTagsAPIImpl(private val logicTags: LogicTags) : LogicTagsAPI {

    override fun setPlayerTag(player: Player, tag: String?, saveToConfig: Boolean) {
        if (tag == null) {
            removePlayerTag(player)
        } else {
            LogicTags.tagsHandler.updateText(player, tag)
        }

        if (saveToConfig) {
            LogicTags.playerTagManager.setTag(player, tag)
        }
    }

    override fun getPlayerTag(player: Player): String? {
        return LogicTags.tagsHandler.tagsMap[player]?.text
    }

    override fun getPlayerTagConfig(player: Player): String? {
        return LogicTags.playerTagManager.getTag(player)
    }

    override fun removePlayerTag(player: Player, saveToConfig: Boolean) {
        if (saveToConfig) {
            LogicTags.playerTagManager.removeTag(player)
        }

        LogicTags.tagsHandler.removePlayerTag(player)
    }

}