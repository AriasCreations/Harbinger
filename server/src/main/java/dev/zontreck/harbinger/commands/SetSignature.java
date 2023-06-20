package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.types.Signature;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public class SetSignature {

	public static final String SETSIG = "setsig";

	@Subscribe
	public static void onSetSignature ( final HarbingerCommandEvent event ) {
		if ( event.command.equals ( SetSignature.SETSIG ) ) {


			if ( event.arguments.size ( ) == 0 ) {
				CommandResponse.NOARG.addToResponse ( event.response , "No arguments supplied" );
				event.response.put ( "usage" , "op1: [y]" );
				event.response.put ( "usage2" , "op2: sig1  sig2" );
				return;
			}
			else {
				if ( event.arguments.size ( ) == 1 ) {
					if ( event.arguments.get ( 0 ).equalsIgnoreCase ( "y" ) ) {
						Signature sig = Signature.makeNew ( );
						Persist.SIGNATURE = sig;

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						CommandResponse.OK.addToResponse ( event.response , "success" );
					}
					else {
						CommandResponse.NOARG.addToResponse ( event.response , "must be 'y' or signatures" );
						return;
					}
				}
				else {
					if ( event.arguments.size ( ) == 2 ) {
						CommandResponse.OK.addToResponse ( event.response , "done" );

						long sig1 = Long.parseLong ( event.arguments.get ( 0 ) );
						long sig2 = Long.parseLong ( event.arguments.get ( 1 ) );


						Signature sig = new Signature ( );
						sig.v1 = sig1;
						sig.v2 = sig2;

						Persist.SIGNATURE = sig;
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
					}
					else {
						CommandResponse.NOARG.addToResponse ( event.response , "You must supply 2 arguments" );
						return;
					}
				}
			}
		}
	}
}
