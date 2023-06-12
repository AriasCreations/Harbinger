package dev.zontreck.harbinger.handlers.http.simulator;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.events.GenericRequestEvent;
import dev.zontreck.harbinger.simulator.types.GridInfo;

public class GetGridInfoHandler {
	@Subscribe
	public static void onRequest(GenericRequestEvent GRE) {
		if (GRE.path.equalsIgnoreCase("/get_grid_info")) {
			GRE.responseIsBinary = false;
			GRE.responseCode = 200;
			GRE.contentType = "application/xml";
			GRE.response = GridInfo.consume();
		}
	}
}
