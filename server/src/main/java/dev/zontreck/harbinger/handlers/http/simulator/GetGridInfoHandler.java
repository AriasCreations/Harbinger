package dev.zontreck.harbinger.handlers.http.simulator;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.GenericRequestEvent;
import dev.zontreck.harbinger.simulator.events.GridInfoGatherEvent;
import dev.zontreck.harbinger.simulator.types.GridInfo;

public class GetGridInfoHandler {


	@Subscribe
	public static void onRequest ( final GenericRequestEvent GRE ) {
		if ( "/get_grid_info".equalsIgnoreCase ( GRE.path ) || "/cgi-bin/login.cgi".equalsIgnoreCase ( GRE.path ) ) {
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
