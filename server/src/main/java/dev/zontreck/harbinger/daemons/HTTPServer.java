package dev.zontreck.harbinger.daemons;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.HarbingerServer;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.HTTPStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * This class handles the http server booting
 */
public class HTTPServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPServer.class.getSimpleName());
    public HTTPServer()
    {
    }

    private static final HTTPServer instance = new HTTPServer();

    public boolean running=false;
    public HttpServer server;
    public static boolean startServer()
    {
        if(instance.running)
        {
            // Server is already running
            LOGGER.info("Server is already running");
            return true;
        }else {
            if(Persist.serverSettings.enabled)
            {
                // Start up server
                LOGGER.info("Starting server");
                try {
                    instance.server = HttpServer.create(new InetSocketAddress(Persist.serverSettings.port),60);
                    HTTPStartingEvent HSE = new HTTPStartingEvent(Persist.serverSettings.port);
                    EventBus.BUS.post(HSE);

                    for(Map.Entry<String, HttpHandler> entry : HSE.contexts.entrySet())
                    {
                        instance.server.createContext(entry.getKey(), entry.getValue());
                    }
                    instance.server.setExecutor(new ScheduledThreadPoolExecutor(1024));
                    instance.server.start();
                    instance.running=true;
                    return true;

                } catch (IOException e) {
                    return false;
                }
            }else {
                LOGGER.warn("HTTP Server cannot be started because it is disabled");
                return false;
            }
        }
    }

    public static void stopServer()
    {
        if(!instance.running)
        {
            // Server is already stopped
            LOGGER.info("Server is already stopped!");
        }else {
            LOGGER.info("Stopping server in 1 seconds");
            instance.server.stop(1);
            instance.running=false;
            instance.server=null;
        }
    }



}
