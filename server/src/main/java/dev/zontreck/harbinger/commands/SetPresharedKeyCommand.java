package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
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

			if ( event.arguments.size ( ) != 1 ) {
				CommandResponse.NOARG.addToResponse ( event.response , "You need to supply the new PSK" );
				return;
			}
			else {
				try {
					Persist.serverSettings.PSK = Key.computeSecuredKey ( event.arguments.get ( 0 ) );

					CommandResponse.OK.addToResponse ( event.response , "ok" );
					EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
				} catch ( NoSuchAlgorithmException e ) {
					CommandResponse.FAIL.addToResponse ( event.response , e.getMessage ( ) );

					throw new RuntimeException ( e );

				}
			}
		}
	}
}
