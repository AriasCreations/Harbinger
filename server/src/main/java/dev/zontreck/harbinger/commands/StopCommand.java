package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
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
			HTTPServer.stopServer ( );
			DelayedExecutorService.stop ( );
			EventBus.BUS.post ( new ServerStoppingEvent ( ) );
			EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

			CommandResponse.OK.addToResponse ( event.response , "ok" );

			System.exit ( 0 );
		}
		else if ( "save".equals ( event.command ) ) {
			CommandResponse.OK.addToResponse ( event.response , "ok" );
			EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
		}
	}
}
