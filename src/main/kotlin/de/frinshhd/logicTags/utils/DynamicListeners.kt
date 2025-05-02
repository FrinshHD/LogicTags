package de.frinshhd.logicTags.utils

import de.frinshhd.logicTags.Main
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

                        Bukkit.getServer().getPluginManager().registerEvents(listener, Main.instance)
                    }
                } catch (e: ClassNotFoundException) {
                    Main.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: IllegalAccessException) {
                    Main.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: InstantiationException) {
                    Main.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: InvocationTargetException) {
                    Main.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: IllegalArgumentException) {
                    Main.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                } catch (e: NoClassDefFoundError) {
                    Main.instance.logger.warning("Error loading listeners in class " + className + " " + e)
                }
            }
        }
    }
}

