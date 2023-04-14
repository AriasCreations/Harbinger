/**
 * We are Harbinger
 */
package dev.zontreck.harbinger;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Path;

import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.harbinger.daemons.HTTPServer;
import dev.zontreck.harbinger.daemons.plugins.PluginLoader;
import dev.zontreck.harbinger.data.containers.Products;
import dev.zontreck.harbinger.data.containers.Servers;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.*;
import dev.zontreck.harbinger.handlers.HandlerRegistry;
import dev.zontreck.harbinger.handlers.ModifyProduct;
import dev.zontreck.harbinger.httphandlers.HTTPEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.terminal.Terminal;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.data.Persist;

public class HarbingerServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HarbingerServer.class.getSimpleName());
    public static final Path PLUGINS;

    static{
        PLUGINS = Path.of("plugins");

        if(!PLUGINS.toFile().exists())
        {
            PLUGINS.toFile().mkdir();
        }
    }

    public static void main(String[] args) {
        LOGGER.info("We are Harbinger");

        EventBus.BUS.register(Persist.class);
        EventBus.BUS.register(Product.class);
        EventBus.BUS.register(Products.class);
        EventBus.BUS.register(Servers.class);
        EventBus.BUS.register(SupportReps.class);
        EventBus.BUS.register(Server.class);
        EventBus.BUS.register(Version.class);
        EventBus.BUS.register(Person.class);
        EventBus.BUS.register(PermissionLevel.class);
        EventBus.BUS.register(HTTPEvents.class);

        HandlerRegistry.register(EventBus.BUS);
        CommandRegistry.register(EventBus.BUS);

        // Start up the server
        // Read the NBT Files for the database
        // This is designed to work without mysql
        if(Folder.size(Persist.MEMORY) == 0)
        {
            LOGGER.info("No settings exist yet!");
            
        }

        Terminal.PREFIX = "HARBINGER";
        Terminal.startTerminal();
        LOGGER.info("Server is running");
        HTTPServer.startServer();


        LOGGER.info("Scanning plugins...");
        try {
            PluginLoader.scan();
            PluginLoader.activate();
        } catch (MalformedURLException |
                 IllegalAccessException |
                 InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }

        while(Terminal.isRunning()){}
        
        LOGGER.info("Saving...");
        Persist.save();
    }
}
