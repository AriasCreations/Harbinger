package dev.zontreck.harbinger.commands.simulation;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
				SimSubCommand cmd = SimSubCommand.valueOfCommand ( ev.arguments.get ( 0 ) );

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
					case setGridName -> {
						List<String> sublist = new ArrayList<> (  );
						for(int i = 1; i < ev.arguments.size (); i++)
						{
							sublist.add ( ev.arguments.get ( i ) );
						}

						Persist.simulatorSettings.GRID_NAME = String.join (" ", sublist );

						EventBus.BUS.post ( new MemoryAlteredEvent () );
						break;
					}
					case setGridNick -> {
						List<String> sublist = new ArrayList<> (  );
						for(int i = 1; i < ev.arguments.size (); i++)
						{
							sublist.add ( ev.arguments.get ( i ) );
						}

						Persist.simulatorSettings.GRID_NICK = String.join (" ", sublist );

						EventBus.BUS.post ( new MemoryAlteredEvent () );
						break;
					}
					case setGridStatus -> {
						if(ev.arguments.get ( 1 ).equalsIgnoreCase ( "false" ) || ev.arguments.get ( 1 ).equals ( "0" ))
						{
							CommandRegistry.LOGGER.info ( "Grid services have been disabled. HTTP service changes will not take effect until next Harbinger Restart" );

							Persist.simulatorSettings.GRID_ON = false;
						}else {
							CommandRegistry.LOGGER.info ( "Grid services have been enabled. HTTP service changes will not take effect until next Harbinger Restart" );

							Persist.simulatorSettings.GRID_ON = true;
						}

						EventBus.BUS.post ( new MemoryAlteredEvent () );
						break;
					}
				}
			}
		}
	}



}
