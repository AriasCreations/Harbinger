package dev.zontreck.harbinger.handlers.http;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.commands.CommandResponse;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.APIRequestEvent;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import org.json.JSONArray;

import java.util.ArrayList;

public class CommandAPIHandler {
	@Subscribe
	public static void onCommand ( APIRequestEvent ev ) {
		if ( ev.request_object.getString ( "type" ).equalsIgnoreCase ( "command" ) && ev.admin ) {
			ev.response_status = 200;
			ev.setCancelled ( true );
			HarbingerCommandEvent evt = new HarbingerCommandEvent ( ev.request_object.getString ( "command" ) );
			evt.arguments = new ArrayList<> ( );

			CommandRegistry.LOGGER.info ( "Commend [" + evt.command + "] " + ev.request_object.getJSONArray ( "args" ).join ( " " ) );

			JSONArray arr = ev.request_object.getJSONArray ( "args" );
			for (
					Object st :
					arr
			) {
				if ( st instanceof String stt ) {
					evt.arguments.add ( stt );
				}
			}

			ev.HTMLContent = evt.html;
			evt.response.remove ( "html" );

			if ( EventBus.BUS.post ( evt ) )
				ev.response_object = evt.response;
			else {
				ev.response_status = 404;

			}

		}
		else {
			ev.response_status = 400;
			CommandResponse.DENY.addToResponse ( ev.response_object , "Access Denied" );
		}
	}
}
