/**
 * We are Harbinger
 */
package dev.zontreck.harbinger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.jar.Manifest;

import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.ariaslib.terminal.Task;
import dev.zontreck.ariaslib.terminal.TaskBus;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.harbinger.daemons.DiscordBot;
import dev.zontreck.harbinger.daemons.HTTPServer;
import dev.zontreck.harbinger.daemons.plugins.PluginLoader;
import dev.zontreck.harbinger.data.containers.Products;
import dev.zontreck.harbinger.data.containers.Servers;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.*;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.events.ServerTickEvent;
import dev.zontreck.harbinger.handlers.EventsRegistry;
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

        UUID ID = Product.makeProductID(1, 59);

        LOGGER.info("We are Harbinger");

        ScheduledThreadPoolExecutor exec;
        DelayedExecutorService.start();

        exec = DelayedExecutorService.getExecutor();
        TaskBus.register();

        TaskBus.tasks.add(new Task("Register Events") {
            @Override
            public void run() {

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
                EventsRegistry.register(EventBus.BUS);

                CommandRegistry.register(EventBus.BUS);
                EventBus.BUS.register(DiscordBot.class);

                Task run = new Task("server-tick", true) {
                    @Override
                    public void run() {
                        EventBus.BUS.post(new ServerTickEvent());
                    }
                };
                DelayedExecutorService.getInstance().scheduleRepeating(run, ServerTickEvent.FREQUENCY);


                setSuccess();
            }
        });

        // Start up the server
        // Read the NBT Files for the database
        // This is designed to work without mysql
        if(Folder.size(Persist.MEMORY) == 0)
        {
            LOGGER.info("No settings exist yet!");
            
        }


        TaskBus.tasks.add(new Task("Start HTTP Server") {
            @Override
            public void run()
            {
                if(HTTPServer.startServer()){
                    setSuccess();
                }else setFail();

            }
        });

        TaskBus.tasks.add(new Task("Scan plugins") {
            @Override
            public void run() {
                try {
                    PluginLoader.scan();
                    setSuccess();
                } catch (Exception E)
                {
                    setFail();
                }
            }
        });

        TaskBus.tasks.add(new Task("Activate plugins") {
            @Override
            public void run()
            {
                PluginLoader.activate();
                setSuccess();
            }
        });

        TaskBus.tasks.add(new Task("Load Version from Jar") {
            @Override
            public void run()
            {
                Persist.HARBINGER_VERSION  = this.getClass().getPackage().getImplementationVersion();

                setSuccess();
            }
        });

        TaskBus.tasks.add(new Task("Startup Completed", true) {
            @Override
            public void run()
            {

                Terminal.PREFIX = "HARBINGER";
                Terminal.startTerminal();
                LOGGER.info("Server is running");
            }
        });

        Servers.registerServerHandler();

        try {
            exec.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Saving...");

        EventBus.BUS.post(new MemoryAlteredEvent());
    }
}
