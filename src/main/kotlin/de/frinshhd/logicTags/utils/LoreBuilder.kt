package de.frinshhd.logicTags.utils

import org.bukkit.ChatColor
import java.util.*

object LoreBuilder {
    @JvmOverloads
    fun build(string: String, color: ChatColor? = null): MutableList<String?> {
        val lines: MutableList<String?> = ArrayList<String?>()

        val maxLenght = 36

        val parts = string.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (part in parts) {
            var index = 0
            var lastLineBreak = 0

            while (part.length > index) {
                val c = part.get(index)

                if (c == ' ' && index - lastLineBreak > maxLenght) {
                    if (color != null) {
                        lines.add(color.toString() + part.substring(lastLineBreak, index))
                    } else {
                        lines.add(part.substring(lastLineBreak, index))
                    }
                    lastLineBreak = index + 1
                }

                index++
            }

            if (color != null) {
                lines.add(color.toString() + part.substring(lastLineBreak))
            } else {
                lines.add(part.substring(lastLineBreak))
            }
        }

        return lines
    }

    fun buildSimple(string: String): MutableList<String?>? {
        val parts = string.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        return Arrays.stream<String?>(parts).toList()
    }
}
