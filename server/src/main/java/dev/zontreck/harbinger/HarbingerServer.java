/**
 * We are Harbinger
 */
package dev.zontreck.harbinger;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.terminal.Task;
import dev.zontreck.ariaslib.terminal.TaskBus;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.ariaslib.util.EnvironmentUtils;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.daemons.DiscordBot;
import dev.zontreck.harbinger.daemons.HTTPBackupServer;
import dev.zontreck.harbinger.daemons.HTTPServer;
import dev.zontreck.harbinger.daemons.plugins.PluginLoader;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.containers.Products;
import dev.zontreck.harbinger.data.containers.Servers;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.mongo.DBSettings;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.data.types.*;
import dev.zontreck.harbinger.events.GridInitializationEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.events.ServerTickEvent;
import dev.zontreck.harbinger.handlers.EventsRegistry;
import dev.zontreck.harbinger.httphandlers.HTTPEvents;
import dev.zontreck.harbinger.simulator.services.ServiceRegistry;
import dev.zontreck.harbinger.simulator.services.simulator.SimulatorUDPService;
import dev.zontreck.harbinger.utils.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HarbingerServer {

	public static final Path PLUGINS;
	private static final Logger LOGGER = LoggerFactory.getLogger ( HarbingerServer.class.getSimpleName ( ) );

	static {
		PLUGINS = Path.of ( "plugins" );

		if ( ! HarbingerServer.PLUGINS.toFile ( ).exists ( ) ) {
			HarbingerServer.PLUGINS.toFile ( ).mkdir ( );
		}
	}

	public static final Path BASE_PATH;
	public static boolean DOCKER = false;


	static {

		if ( EnvironmentUtils.isRunningInsideDocker ( ) ) {
			BASE_PATH = Path.of ( "/app/data" );
			DOCKER = true;
		}
		else {
			BASE_PATH = Path.of ( "data" );
		}
	}

	public static void main ( final String[] args ) {

		final UUID ID = Product.makeProductID ( 0x9f , 0xcd );

		HarbingerServer.LOGGER.info ( "We are Harbinger" );

		final ScheduledThreadPoolExecutor exec;
		DelayedExecutorService.start ( );

		exec = DelayedExecutorService.getExecutor ( );
		TaskBus.register ( );

		if ( DOCKER ) {
			LOGGER.info ( "Environment: Docker" );
		}

		TaskBus.tasks.add ( new Task ( "Connect to DB" ) {
			@Override
			public void run ( ) {
				DBSettings.LOAD ( );
				Future<Boolean> task = CompletableFuture.supplyAsync ( ( ) -> MongoDriver.tryConnect ( ) );
				try {
					task.get ( 10 , TimeUnit.SECONDS );
					setSuccess ( );
				} catch ( Exception e ) {
					setFail ( );
					task.cancel ( true );
					MongoDriver.can_connect = false;
				}
			}
		} );

		TaskBus.tasks.add ( new Task ( "Register Events" ) {
			@Override
			public void run ( ) {

				EventBus.BUS.register ( Persist.class );
				EventBus.BUS.register ( Product.class );
				EventBus.BUS.register ( Products.class );
				EventBus.BUS.register ( Servers.class );
				EventBus.BUS.register ( SupportReps.class );
				EventBus.BUS.register ( Server.class );
				EventBus.BUS.register ( Version.class );
				EventBus.BUS.register ( Person.class );
				EventBus.BUS.register ( PermissionLevel.class );
				EventBus.BUS.register ( HTTPEvents.class );
				EventsRegistry.register ( EventBus.BUS );

				CommandRegistry.register ( EventBus.BUS );
				EventBus.BUS.register ( DiscordBot.class );

				ServiceRegistry.register ( EventBus.BUS );

				final Task run = new Task ( "server-tick" , true ) {
					@Override
					public void run ( ) {
						EventBus.BUS.post ( new ServerTickEvent ( ) );
					}
				};
				DelayedExecutorService.getInstance ( ).scheduleRepeating ( run , ServerTickEvent.FREQUENCY );


				this.setSuccess ( );
			}
		} );

		// Start up the server
		// Read the NBT Files for the database
		// This is designed to work without mysql
		if ( 0 == Persist.MEMORY.size ( ) ) {
			HarbingerServer.LOGGER.info ( "No settings exist yet!" );
			// Save defaults
			EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

		}


		TaskBus.tasks.add ( new Task ( "Start HTTP Server" ) {
			@Override
			public void run ( ) {
				if ( HTTPServer.startServer ( ) ) {
					this.setSuccess ( );
				}
				else this.setFail ( );

			}
		} );

		TaskBus.tasks.add ( new Task ( "Start Emergency Remote-Admin server" ) {
			@Override
			public void run ( ) {
				if ( HTTPBackupServer.startServer ( ) )
					this.setSuccess ( );
				else this.setFail ( );
			}
		} );

		TaskBus.tasks.add ( new Task ( "Scan plugins" ) {
			@Override
			public void run ( ) {
				try {
					PluginLoader.scan ( );
					this.setSuccess ( );
				} catch ( final Exception E ) {
					this.setFail ( );
				}
			}
		} );

		TaskBus.tasks.add ( new Task ( "Activate plugins" ) {
			@Override
			public void run ( ) {
				PluginLoader.activate ( );
				this.setSuccess ( );
			}
		} );

		TaskBus.tasks.add ( new Task ( "Load Version from Jar" ) {
			@Override
			public void run ( ) {
				Persist.HARBINGER_VERSION = getClass ( ).getPackage ( ).getImplementationVersion ( );

				this.setSuccess ( );
			}
		} );

		TaskBus.tasks.add ( new Task ( "Load Patch Notes from Jar" ) {
			@Override
			public void run ( ) {
				Persist.PATCH_NOTES = DataUtils.ReadAllBytesFromResource ( "patch.notes" );

				this.setSuccess ( );
			}
		} );


		TaskBus.tasks.add ( new Task ( "Activate Grid Services" ) {
			@Override
			public void run ( ) {
				if ( Persist.simulatorSettings.GRID_ON ) {
					EventBus.BUS.post ( new GridInitializationEvent ( ) );


					setSuccess ( );
				}
				else {
					setFail ( );
				}
			}
		} );

		TaskBus.tasks.add ( new Task ( "Start Simulator Services" ) {
			@Override
			public void run ( ) {
				if ( Persist.simulatorSettings.SIM_ON ) {
					SimulatorUDPService.startService ( );

					setSuccess ( );
				}
				else setFail ( );
			}
		} );


		Task obtainIPTask = new Task ( "Determine outward facing IP Address" ) {
			@Override
			public void run ( ) {
				try {
					URL url = new URL ( "http://checkip.amazonaws.com" );
					BufferedReader BIS = new BufferedReader ( new InputStreamReader ( url.openStream ( ) ) );
					Persist.HARBINGER_EXTERNAL_IP = BIS.readLine ( );

					setSuccess ( );
					return;
				} catch ( MalformedURLException e ) {
					setFail ( );

				} catch ( IOException e ) {
					setFail ( );
				}

				LOGGER.info ( "Failed to obtain the IP Address, the services used may be offline currently. If you set a backup using the commands, that will be used for now." );
				throw new RuntimeException ( );
			}
		};
		TaskBus.tasks.add ( obtainIPTask );

		TaskBus.tasks.add ( new Task ( "Startup Completed" , true ) {
			@Override
			public void run ( ) {
				HarbingerServer.LOGGER.info ( "Server is running" );


				if ( HarbingerServer.DOCKER ) {
					// If we are in docker, ensure the base file path is /data
					if ( BASE_PATH.toAbsolutePath ( ).toString ( ).startsWith ( "/app/data" ) ) {
						LOGGER.info ( "Successfully verified docker data storage status" );
					}
					else {
						LOGGER.error ( "Docker data path is set incorrectly" );
					}
				}
			}
		} );

		Servers.registerServerHandler ( );

		try {
			exec.wait ( );
		} catch ( final InterruptedException e ) {
			throw new RuntimeException ( e );
		}
	}
}
