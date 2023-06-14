package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.Terminal;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.harbinger.daemons.HTTPServer;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.events.ServerStoppingEvent;

public enum StopCommand {
	;
	public static final String Stop = "stop";
	public static final String Save = "save";


	@Subscribe
	public static void onStop ( final CommandEvent event ) {
		if ( event.command.equals ( StopCommand.Stop ) ) {
			HTTPServer.stopServer ( );
			CommandRegistry.LOGGER.info ( "Server is stopping..." );
			Terminal.setRunning ( false );
			DelayedExecutorService.stop ( );
			EventBus.BUS.post ( new ServerStoppingEvent ( ) );
			EventBus.BUS.post ( new MemoryAlteredEvent () );

			event.setCancelled ( true );

			System.exit(0);
		}
		else if ( "save".equals ( event.command ) ) {
			CommandRegistry.LOGGER.info ( "Saving data..." );
			EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
			CommandRegistry.LOGGER.info ( "Save completed" );
		}
	}
}
