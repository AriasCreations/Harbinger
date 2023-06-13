package dev.zontreck.harbinger.httphandlers;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.events.HTTPStartingEvent;
import dev.zontreck.harbinger.httphandlers.handlers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPEvents {


	public static final Logger LOGGER = LoggerFactory.getLogger ( HTTPEvents.class.getSimpleName ( ) );

	@Subscribe
	public static void onHttpServerStarting ( final HTTPStartingEvent hse ) {
		hse.contexts.put ( "/get_support" , new GetSupportHandler ( ) );
		hse.contexts.put ( "/stop" , new StopServerHandler ( ) );
		hse.contexts.put ( "/api" , new APIHandler ( ) );
		hse.contexts.put ( "/version" , new VersionCheckHandler ( ) );
		hse.contexts.put ( "/discord" , new DiscordHandler ( ) );
		hse.contexts.put ( "/" , new GenericRequestHandler ( ) );
	}
}
