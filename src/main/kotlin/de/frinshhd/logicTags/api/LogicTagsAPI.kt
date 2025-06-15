package de.frinshhd.logicTags.api

import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull
import javax.annotation.Nullable

interface LogicTagsAPI {
    /**
     * Sets a tag for a player.
     * @param player The player to set the tag for.
     * @param tag The tag string. Null to clear the tag.
     */
    fun setPlayerTag(@NotNull player: Player, @Nullable tag: String?)

    /**
     * Gets the current tag for a player.
     * @param player The player whose tag to retrieve.
     * @return The player's tag, or null if not set.
     */
    @Nullable
    fun getPlayerTag(@NotNull player: Player): String?

    /**
     * Removes the tag for a player.
     * @param player The player whose tag to clear.
     */
    fun removePlayerTag(@NotNull player: Player)
}