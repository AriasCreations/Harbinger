package dev.zontreck.harbinger.commands;


import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import org.json.JSONObject;

public class HelpCommand {
	public static final String Help = "help";

	@Subscribe
	public static void onHelp ( final HarbingerCommandEvent ev ) {
		if ( Help.equals ( ev.command ) ) {
			CommandResponse.OK.addToResponse ( ev.response , "ok" );
			ev.setCancelled ( true );

			var CommandTable = new HTMLElementBuilder ( "div" ).addClass ( "table-responsive" );
			CommandTable.addChild ( Commands.render () );

			ev.html = CommandHTMLPage.makePage ( "Command Index", CommandTable, ev.response );

			ev.response.put ( "usage" , Commands.print ( ) );

		}
	}


}
