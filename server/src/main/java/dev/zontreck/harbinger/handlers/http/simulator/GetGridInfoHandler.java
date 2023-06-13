package dev.zontreck.harbinger.handlers.http.simulator;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.events.GenericRequestEvent;
import dev.zontreck.harbinger.simulator.types.GridInfo;

import java.nio.charset.StandardCharsets;

public enum GetGridInfoHandler {
	;

	@Subscribe
	public static void onRequest ( final GenericRequestEvent GRE ) {
		if ( "/get_grid_info".equalsIgnoreCase ( GRE.path ) ) {
			GRE.responseIsBinary = false;
			GRE.responseCode = 200;
			GRE.contentType = "application/xml";
			GridInfo info = GridInfo.consume ( );

			GRE.response = info.toString ( ).getBytes ( StandardCharsets.UTF_8 );
		}
	}
}
