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
    private static final Logger LOGGER = LoggerFactory.getLogger(HarbingerServer.class.getName());

    public static void main(String[] args) {
        LOGGER.info("We are Harbinger");

        EventBus.BUS.register(Persist.class);
        CommandRegistry.register(EventBus.BUS);

        // Start up the server
        // Read the NBT Files for the database
        // This is designed to work without mysql
        if(Persist.MEMORY.size() == 0)
        {
            LOGGER.info("Initializing new data file...");
            
        }else {
            LOGGER.info("Memory was found, validating data");

        }


        Terminal.startTerminal();
        while(Terminal.isRunning()){}
        
    }
}
