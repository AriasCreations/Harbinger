package dev.zontreck.harbinger.commands.simulation;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.commands.CommandResponse;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class SimulationCommands {

	public static final String BASE_COMMAND = "sim";

	@Subscribe
	public static void onCommand ( HarbingerCommandEvent ev ) {
		if ( ev.command.equals ( BASE_COMMAND ) ) {
			if ( 0 == ev.arguments.size ( ) ) {
				// Print Usage
				String usage = SimSubCommand.print ( );

				CommandRegistry.LOGGER.info ( "\n{}" , usage );
			}
			else {
				SimSubCommand cmd = SimSubCommand.valueOfCommand ( ev.arguments.get ( 0 ) );

				switch ( cmd ) {
					case setBaseURL -> {
						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply the URL" );
							return;
						}
						CommandResponse.OK.addToResponse ( ev.response , "BASE URL updated" );
						Persist.simulatorSettings.BASE_URL = ev.arguments.get ( 1 );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case getBaseURL -> {
						CommandResponse.OK.addToResponse ( ev.response , Persist.simulatorSettings.BASE_URL );
						break;
					}
					case setGridName -> {

						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply the grid name" );
							return;
						}

						List<String> sublist = new ArrayList<> ( );
						for ( int i = 1 ; i < ev.arguments.size ( ) ; i++ ) {
							sublist.add ( ev.arguments.get ( i ) );
						}

						Persist.simulatorSettings.GRID_NAME = String.join ( " " , sublist );
						CommandResponse.OK.addToResponse ( ev.response , "success" );

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case setGridNick -> {

						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply the grid nickname" );
							return;
						}

						List<String> sublist = new ArrayList<> ( );
						for ( int i = 1 ; i < ev.arguments.size ( ) ; i++ ) {
							sublist.add ( ev.arguments.get ( i ) );
						}

						Persist.simulatorSettings.GRID_NICK = String.join ( " " , sublist );

						CommandResponse.OK.addToResponse ( ev.response , "success" );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case setGridStatus -> {
						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You need to supply a true or false" );
							return;
						}
						if ( ev.arguments.get ( 1 ).equalsIgnoreCase ( "false" ) || ev.arguments.get ( 1 ).equals ( "0" ) ) {

							CommandResponse.OK.addToResponse ( ev.response , "Grid services have been disabled." );
							ev.response.put ( "restart_needed" , true );

							Persist.simulatorSettings.GRID_ON = false;
						}
						else {

							CommandResponse.OK.addToResponse ( ev.response , "Grid services have been enabled" );
							ev.response.put ( "restart_needed" , true );

							Persist.simulatorSettings.GRID_ON = true;
						}

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case setSimulator -> {
						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You need to supply a true or false" );
							return;
						}

						if ( ev.arguments.get ( 1 ).equalsIgnoreCase ( "false" ) || ev.arguments.get ( 1 ).equals ( "0" ) ) {

							CommandResponse.OK.addToResponse ( ev.response , "Simulator services have been disabled" );
							ev.response.put ( "restart_needed" , true );

							Persist.simulatorSettings.SIM_ON = false;
						}
						else {

							CommandResponse.OK.addToResponse ( ev.response , "Simulator services have been enabled" );
							ev.response.put ( "restart_needed" , true );

							Persist.simulatorSettings.SIM_ON = true;
						}

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case updateTos -> {
						Persist.simulatorSettings.LAST_TOS_UPDATE = Instant.now ( );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						CommandResponse.OK.addToResponse ( ev.response , "success" );
						break;
					}
					case updatePatch -> {
						Persist.simulatorSettings.LAST_PATCHNOTES_UPDATE = Instant.now ( );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						CommandResponse.OK.addToResponse ( ev.response , "success" );
						break;
					}
				}
			}
		}
	}


}
