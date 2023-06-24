package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.html.bootstrap.Color;
import dev.zontreck.ariaslib.terminal.Task;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.harbinger.daemons.HTTPServer;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.events.ServerStoppingEvent;

public class StopCommand {

	public static final String Stop = "stop";
	public static final String Save = "save";


	@Subscribe
	public static void onStop ( final HarbingerCommandEvent event ) {
		if ( event.command.equals ( StopCommand.Stop ) ) {
			event.setCancelled ( true );
			HTTPServer.stopServer ( );
			DelayedExecutorService.stop ( );
			EventBus.BUS.post ( new ServerStoppingEvent ( ) );
			EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

			CommandResponse.OK.addToResponse ( event.response , "ok" );


			event.html = CommandHTMLPage.makePage ( "Stop Server" , CommandMessage.buildMessage ( Color.Success , "Server will now stop" ) , event.response );


			DelayedExecutorService.scheduleTask ( new Task ( "Shutdown Server" , true ) {
				@Override
				public void run ( ) {
					CommandRegistry.LOGGER.info ( "Shutting down..." );
					System.exit ( 0 );
				}
			} , 2 );


		}
		else if ( "save".equals ( event.command ) ) {
			CommandResponse.OK.addToResponse ( event.response , "ok" );
			EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
		}
	}
}
