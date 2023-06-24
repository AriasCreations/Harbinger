package dev.zontreck.harbinger.commands.simulation;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.ariaslib.html.bootstrap.Color;
import dev.zontreck.harbinger.commands.CommandHTMLPage;
import dev.zontreck.harbinger.commands.CommandMessage;
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
			ev.setCancelled ( true );
			var tbl = new HTMLElementBuilder ( "div" );
			if ( 0 == ev.arguments.size ( ) ) {
				// Print Usage
				CommandResponse.NOARG.addToResponse ( ev.response , "No arguments supplied" );
				String usage = SimSubCommand.print ( );
				tbl.addChild ( SimSubCommand.render ( ) );
				ev.html = CommandHTMLPage.makePage ( "Simulation Command Index" , tbl , ev.response );
			}
			else {
				SimSubCommand cmd = SimSubCommand.valueOfCommand ( ev.arguments.get ( 0 ) );

				switch ( cmd ) {
					case setBaseURL -> {
						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply the URL" );
							ev.html = CommandHTMLPage.makePage ( "Set Base URL" , tbl.addChild ( CommandMessage.buildMessage ( Color.Danger , "You must supply the URL" ) ) , ev.response );
							return;
						}
						CommandResponse.OK.addToResponse ( ev.response , "BASE URL updated" );
						Persist.simulatorSettings.BASE_URL = ev.arguments.get ( 1 );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						ev.html = CommandHTMLPage.makePage ( "Set Base URL" , tbl.addChild ( CommandMessage.buildMessage ( Color.Success , "URL has been successfully set" ) ) , ev.response );
						break;
					}
					case getBaseURL -> {
						CommandResponse.OK.addToResponse ( ev.response , Persist.simulatorSettings.BASE_URL );

						ev.html = CommandHTMLPage.makePage ( "Get Base URL" , tbl.addChild ( CommandMessage.buildMessage ( Color.Primary , "URL : " + Persist.simulatorSettings.BASE_URL ) ) , ev.response );
						break;
					}
					case setGridName -> {

						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply the grid name" );
							ev.html = CommandHTMLPage.makePage ( "Set Grid Name" , tbl.addChild ( CommandMessage.buildMessage ( Color.Danger , "Grid Name could not be set because it was not supplied" ) ) , ev.response );
							return;
						}

						List<String> sublist = new ArrayList<> ( );
						for ( int i = 1 ; i < ev.arguments.size ( ) ; i++ ) {
							sublist.add ( ev.arguments.get ( i ) );
						}

						Persist.simulatorSettings.GRID_NAME = String.join ( " " , sublist );
						CommandResponse.OK.addToResponse ( ev.response , "success" );

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

						ev.html = CommandHTMLPage.makePage ( "Set Grid Name" , tbl.addChild ( CommandMessage.buildMessage ( Color.Success , "Grid Name was successfully changed" ) ) , ev.response );
						break;
					}
					case setGridNick -> {

						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply the grid nickname" );
							ev.html = CommandHTMLPage.makePage ( "Set Grid Nickname" , tbl.addChild ( CommandMessage.buildMessage ( Color.Danger , "Grid nickname could not be set because it was not supplied" ) ) , ev.response );
							return;
						}

						List<String> sublist = new ArrayList<> ( );
						for ( int i = 1 ; i < ev.arguments.size ( ) ; i++ ) {
							sublist.add ( ev.arguments.get ( i ) );
						}

						Persist.simulatorSettings.GRID_NICK = String.join ( " " , sublist );

						CommandResponse.OK.addToResponse ( ev.response , "success" );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						ev.html = CommandHTMLPage.makePage ( "Set Grid Nick" , tbl.addChild ( CommandMessage.buildMessage ( Color.Success , "Grid nickname was successfully changed" ) ) , ev.response );
						break;
					}
					case setGridStatus -> {
						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You need to supply a true or false" );
							ev.html = CommandHTMLPage.makePage ( "Set Grid Service" , tbl.addChild ( CommandMessage.buildMessage ( Color.Danger , "No arguments were supplied. A value or true or false was expected" ) ) , ev.response );
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

						ev.html = CommandHTMLPage.makePage ( "Set Grid Service" , tbl.addChild ( CommandMessage.buildMessage ( Color.Success , "Service operation successful" ) ) , ev.response );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case setSimulator -> {
						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You need to supply a true or false" );
							ev.html = CommandHTMLPage.makePage ( "Set Simulator Service" , tbl.addChild ( CommandMessage.buildMessage ( Color.Danger , "No arguments were supplied. A value or true or false was expected" ) ) , ev.response );
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

						ev.html = CommandHTMLPage.makePage ( "Set Simulator Service" , tbl.addChild ( CommandMessage.buildMessage ( Color.Success , "Service operation successful" ) ) , ev.response );

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case updateTos -> {
						Persist.simulatorSettings.LAST_TOS_UPDATE = Instant.now ( );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						CommandResponse.OK.addToResponse ( ev.response , "success" );
						ev.html = CommandHTMLPage.makePage ( "Update Terms of Service" , tbl.addChild ( CommandMessage.buildMessage ( Color.Success , "Terms of service will be shown to everyone again" ) ) , ev.response );
						break;
					}
				}
			}
		}
	}


}
