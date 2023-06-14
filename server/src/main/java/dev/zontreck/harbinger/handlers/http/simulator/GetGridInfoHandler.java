package dev.zontreck.harbinger.handlers.http.simulator;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.simulation.SimulationCommands;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.APIRequestEvent;
import dev.zontreck.harbinger.events.GenericRequestEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.simulator.events.GridInfoGatherEvent;
import dev.zontreck.harbinger.simulator.types.GridInfo;

import java.time.Instant;

public class GetGridInfoHandler {

	@Subscribe
	public static void onAPIRequest( final APIRequestEvent ev)
	{
		if("update_critical_info".equals ( ev.request_object.getString ( "type" ) ))
		{

			if(Persist.serverSettings.PSK.validate ( ev.request_object.getString ( "psk" ) ))
			{
				Persist.simulatorSettings.LAST_PATCHNOTES_UPDATE = Instant.now ();
				EventBus.BUS.post ( new MemoryAlteredEvent () );

				ev.response_status=200;
				ev.response_object.put("status", "Success");
				ev.setCancelled ( true );
			}
		}
	}

	@Subscribe
	public static void onRequest ( final GenericRequestEvent GRE ) {
		if ( "/get_grid_info".equalsIgnoreCase ( GRE.path ) ) {
			GRE.responseIsBinary = false;
			GRE.responseCode = 200;
			GRE.contentType = "text/plain";
			GridInfo info = GridInfo.consume ( );

			GRE.responseText = info.toString ( );
			GRE.setCancelled ( true );
		}
		else if ( "/favicon.ico".equalsIgnoreCase ( GRE.path ) ) {
			// Just so it stops spamming the console if testing
			GRE.responseCode = 404;
			GRE.contentType = "text/plain";
			GRE.responseIsBinary = false;
			GRE.responseText = "Not Found";
			GRE.setCancelled ( true );
		}
	}


	@Subscribe
	public static void onGatherGridInfo ( GridInfoGatherEvent ev ) {
		ev.info.LoginURI = ev.info.LoginURI.replace ( "$SELF$" , Persist.simulatorSettings.BASE_URL );
		ev.info.Economy = ev.info.Economy.replace ( "$SELF$" , Persist.simulatorSettings.BASE_URL );
		ev.info.Register = ev.info.Register.replace ( "$SELF$" , Persist.simulatorSettings.BASE_URL );

		ev.info.GridName = Persist.simulatorSettings.GRID_NAME;
		ev.info.GridNick = Persist.simulatorSettings.GRID_NICK;
	}
}
