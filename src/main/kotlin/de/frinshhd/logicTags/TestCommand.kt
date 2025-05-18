package de.frinshhd.logicTags

import org.incendo.cloud.annotations.Command

class TestCommand {

    @Command("test")
    fun testCommand() {
        // This is a test command
        // You can add your logic here
        println("Test command executed")
    }

}