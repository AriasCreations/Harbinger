package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.utils.Key;

import java.security.NoSuchAlgorithmException;

public class SetPresharedKeyCommand {
	public static final String SETPSK = "setpsk";


	@Subscribe
	public static void onSetPSK ( final HarbingerCommandEvent event ) {
		if ( event.command.equals ( SetPresharedKeyCommand.SETPSK ) ) {
			event.setCancelled ( true );

			if ( event.arguments.size ( ) != 1 ) {
				CommandResponse.NOARG.addToResponse ( event.response , "You need to supply the new PSK" );

				event.html = CommandHTMLPage.makePage ( "Set PSK", new HTMLElementBuilder ( "h4" ).withText ( "The pre-shared key could not be set due to not enough arguments being supplied." ), event.response );
				return;
			}
			else {
				try {
					Persist.serverSettings.PSK = Key.computeSecuredKey ( event.arguments.get ( 0 ) );

					CommandResponse.OK.addToResponse ( event.response , "ok" );
					EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

					event.html = CommandHTMLPage.makePage ( "Set PSK", new HTMLElementBuilder ( "h4" ).withText ( "The pre-shared key was changed successfully" ), event.response );
				} catch ( NoSuchAlgorithmException e ) {
					CommandResponse.FAIL.addToResponse ( event.response , e.getMessage ( ) );
					event.html = CommandHTMLPage.makePage ( "Set PSK", new HTMLElementBuilder ( "h4" ).withText ( "The pre-shared key could not be set due to an unknown reason" ), event.response );

					throw new RuntimeException ( e );

				}
			}
		}
	}
}
