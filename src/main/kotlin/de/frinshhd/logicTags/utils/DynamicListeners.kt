package de.frinshhd.logicTags.utils

import de.frinshhd.logicTags.LogicTags
import org.bukkit.Bukkit
import java.lang.reflect.InvocationTargetException

object DynamicListeners {
    fun load(classNames: MutableSet<String>, canonicalName: String) {
        for (className in classNames) {
            if (className.contains(canonicalName)) {
                try {
                    val cls = Class.forName(className)

                    val listenerClass: Class<org.bukkit.event.Listener> =
                        org.bukkit.event.Listener::class.java

                    if (listenerClass.isAssignableFrom(cls)) {
                        val constructor = cls.getConstructors()[0]
                        val listener = constructor.newInstance() as org.bukkit.event.Listener

                        Bukkit.getServer().getPluginManager().registerEvents(listener, LogicTags.instance)
                    }
                } catch (e: ClassNotFoundException) {
                    LogicTags.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: IllegalAccessException) {
                    LogicTags.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: InstantiationException) {
                    LogicTags.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: InvocationTargetException) {
                    LogicTags.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: IllegalArgumentException) {
                    LogicTags.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: NoClassDefFoundError) {
                    LogicTags.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                }
            }
        }
    }
}

