package dev.zontreck.harbinger.commands;


import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;

public class HelpCommand {
	public static final String Help = "help";

	@Subscribe
	public static void onHelp ( final HarbingerCommandEvent ev ) {
		if ( Help.equals ( ev.command ) ) {
			CommandResponse.OK.addToResponse ( ev.response , "ok" );
			ev.response.put ( "usage" , Commands.print ( ) );

		}
	}


}
