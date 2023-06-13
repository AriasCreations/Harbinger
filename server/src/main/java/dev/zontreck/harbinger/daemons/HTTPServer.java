package dev.zontreck.harbinger.daemons;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.HTTPStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * This class handles the http server booting
 */
public class HTTPServer {
	private static final Logger LOGGER = LoggerFactory.getLogger ( HTTPServer.class.getSimpleName ( ) );
	private static final HTTPServer instance = new HTTPServer ( );
	public boolean running;
	public HttpServer server;

	public HTTPServer ( ) {
	}

	public static boolean startServer ( ) {
		if ( HTTPServer.instance.running ) {
			// Server is already running
			HTTPServer.LOGGER.info ( "Server is already running" );
			return true;
		}
		else {
			if ( Persist.serverSettings.enabled ) {
				// Start up server
				HTTPServer.LOGGER.info ( "Starting server" );
				try {
					HTTPServer.instance.server = HttpServer.create ( new InetSocketAddress ( Persist.serverSettings.port ) , 60 );
					final HTTPStartingEvent HSE = new HTTPStartingEvent ( Persist.serverSettings.port );
					EventBus.BUS.post ( HSE );

					for ( final Map.Entry<String, HttpHandler> entry : HSE.contexts.entrySet ( ) ) {
						HTTPServer.instance.server.createContext ( entry.getKey ( ) , entry.getValue ( ) );
					}
					HTTPServer.instance.server.setExecutor ( new ScheduledThreadPoolExecutor ( 1024 ) );
					HTTPServer.instance.server.start ( );
					HTTPServer.instance.running = true;
					return true;

				} catch ( final IOException e ) {
					return false;
				}
			}
			else {
				HTTPServer.LOGGER.warn ( "HTTP Server cannot be started because it is disabled" );
				return false;
			}
		}
	}

	public static void stopServer ( ) {
		if ( ! HTTPServer.instance.running ) {
			// Server is already stopped
			HTTPServer.LOGGER.info ( "Server is already stopped!" );
		}
		else {
			HTTPServer.LOGGER.info ( "Stopping server in 1 seconds" );
			HTTPServer.instance.server.stop ( 1 );
			HTTPServer.instance.running = false;
			HTTPServer.instance.server = null;
		}
	}


}
