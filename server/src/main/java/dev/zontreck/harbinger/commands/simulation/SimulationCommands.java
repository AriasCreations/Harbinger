package dev.zontreck.harbinger.commands.simulation;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;


public class SimulationCommands {

	public static final String BASE_COMMAND = "sim";

	@Subscribe
	public static void onCommand ( CommandEvent ev ) {
		if ( ev.command.equals ( BASE_COMMAND ) ) {
			if ( 0 == ev.arguments.size ( ) ) {
				// Print Usage
				String usage = SimSubCommand.print ( );

				CommandRegistry.LOGGER.info ( "\n{}" , usage );
			} else {
				SimSubCommand cmd = SimSubCommand.valueOf ( ev.arguments.get ( 0 ) );

				switch(cmd)
				{
					case setBaseURL -> {
						Persist.simulatorSettings.BASE_URL = ev.arguments.get ( 1 );
						EventBus.BUS.post ( new MemoryAlteredEvent () );
						break;
					}
					case getBaseURL -> {
						CommandRegistry.LOGGER.info ( "BASE URL -> " + Persist.simulatorSettings.BASE_URL );
						break;
					}
				}
			}
		}
	}



}
