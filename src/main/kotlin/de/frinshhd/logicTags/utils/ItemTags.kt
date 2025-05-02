package de.frinshhd.logicTags.utils

import de.frinshhd.logicTags.Main
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ItemTags {
    fun tagItemMeta(meta: ItemMeta, id: kotlin.String) {
        if (meta == null) {
            return
        }


        val container: PersistentDataContainer = meta.getPersistentDataContainer()
        val key: NamespacedKey = NamespacedKey(Main.instance, "itemTag")
        container.set<kotlin.String?, kotlin.String?>(key, PersistentDataType.STRING, id)
    }

    fun tagItem(itemStack: org.bukkit.inventory.ItemStack?, id: kotlin.String): org.bukkit.inventory.ItemStack? {
        if (itemStack == null) {
            return null
        }

        val meta: ItemMeta = itemStack.itemMeta
        tagItemMeta(meta, id)
        itemStack.setItemMeta(meta)

        return itemStack
    }

    fun extractItemId(itemMeta: ItemMeta?): kotlin.String? {
        //if itemMeta doesn't exist
        if (itemMeta == null) {
            return null
        }

        val container: PersistentDataContainer = itemMeta.getPersistentDataContainer()

        if (container.getKeys().isEmpty()) {
            return null
        }

        var tag: kotlin.String? = null

        //get all keys
        for (key in container.getKeys()) {
            if (!key.getNamespace().equals(Main.instance.name, ignoreCase = true)) {
                continue
            }

            if (!key.getKey().equals("itemTag", ignoreCase = true)) {
                continue
            }

            tag = container.get<String,String>(key, PersistentDataType.STRING)
        }

        return tag
    }
}
