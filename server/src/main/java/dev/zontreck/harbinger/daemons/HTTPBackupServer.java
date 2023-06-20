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
 *
 * This is an emergency HTTP server for command access.
 *
 * It operates on a fixed port number of 7767
 * This instance of the server cannot be disabled, and will be available for recovery of the server if something goes wrong.
 */
public class HTTPBackupServer {
	private static final Logger LOGGER = LoggerFactory.getLogger ( HTTPBackupServer.class.getSimpleName ( ) );
	private static final HTTPBackupServer instance = new HTTPBackupServer ( );
	public boolean running;
	public HttpServer server;

	public static final int REMOTE_ADMIN_PORT=7767;

	public HTTPBackupServer ( ) {
	}

	public static boolean startServer (  ) {
		if ( HTTPBackupServer.instance.running ) {
			// Server is already running
			HTTPBackupServer.LOGGER.info ( "Server is already running" );
			return true;
		}
		else {
			// Start up server
			HTTPBackupServer.LOGGER.info ( "Starting server" );
			try {
				HTTPBackupServer.instance.server = HttpServer.create ( new InetSocketAddress ( REMOTE_ADMIN_PORT ) , 60 );
				final HTTPStartingEvent HSE = new HTTPStartingEvent ( REMOTE_ADMIN_PORT );
				EventBus.BUS.post ( HSE );

				for ( final Map.Entry<String, HttpHandler> entry : HSE.contexts.entrySet ( ) ) {
					HTTPBackupServer.instance.server.createContext ( entry.getKey ( ) , entry.getValue ( ) );
				}
				HTTPBackupServer.instance.server.setExecutor ( new ScheduledThreadPoolExecutor ( 1024 ) );
				HTTPBackupServer.instance.server.start ( );
				HTTPBackupServer.instance.running = true;
				return true;

			} catch ( final IOException e ) {
				return false;
			}
		}
	}

	public static void stopServer ( ) {
		if ( ! HTTPBackupServer.instance.running ) {
			// Server is already stopped
			HTTPBackupServer.LOGGER.info ( "Server is already stopped!" );
		}
		else {
			HTTPBackupServer.LOGGER.info ( "Stopping server in 1 seconds" );
			HTTPBackupServer.instance.server.stop ( 1 );
			HTTPBackupServer.instance.running = false;
			HTTPBackupServer.instance.server = null;
		}
	}


}
