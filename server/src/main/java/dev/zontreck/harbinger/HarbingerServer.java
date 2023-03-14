/**
 * We are Harbinger
 */
package dev.zontreck.harbinger;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.terminal.Terminal;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.data.Persist;

public class HarbingerServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HarbingerServer.class.getSimpleName());

    public static void main(String[] args) {
        LOGGER.info("We are Harbinger");

        EventBus.BUS.register(Persist.class);
        CommandRegistry.register(EventBus.BUS);

        // Start up the server
        // Read the NBT Files for the database
        // This is designed to work without mysql
        if(Persist.MEMORY.size() == 0)
        {
            LOGGER.info("No settings exist yet!");
            
        }

        Terminal.PREFIX = "HARBINGER";
        Terminal.startTerminal();
        LOGGER.info("Server is running");
        while(Terminal.isRunning()){}
        
        LOGGER.info("Saving...");
        Persist.save();
    }
}
