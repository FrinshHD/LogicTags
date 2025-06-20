package de.frinshhd.logicTags.api

import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull
import javax.annotation.Nullable

interface LogicTagsAPI {
    /**
     * Sets a tag for a player.
     *
     * @param player The player to set the tag for. Must not be null.
     * @param tag The tag string to assign. Pass null to clear the tag.
     * @param saveToConfig If true, saves the tag to the config file. Defaults to false.
     */
    fun setPlayerTag(@NotNull player: Player, @Nullable tag: String?, saveToConfig: Boolean = false)

    /**
     * Gets the current tag for a player (may be in-memory, not from config).
     *
     * @param player The player whose tag to retrieve. Must not be null.
     * @return The player's tag, or null if not set.
     */
    @Nullable
    fun getPlayerTag(@NotNull player: Player): String?

    /**
     * Gets the tag for a player as stored in the config file.
     *
     * @param player The player whose config tag to retrieve. Must not be null.
     * @return The player's tag from config, or null if not set.
     */
    @Nullable
    fun getPlayerTagConfig(@NotNull player: Player): String?

    /**
     * Removes the tag for a player.
     *
     * @param player The player whose tag to clear. Must not be null.
     * @param saveToConfig If true, removes the tag from the config file as well. Defaults to false.
     */
    fun removePlayerTag(@NotNull player: Player, saveToConfig: Boolean = false)
}