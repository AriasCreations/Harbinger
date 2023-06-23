package dev.zontreck.harbinger.commands.support;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.harbinger.commands.CommandHTMLPage;
import dev.zontreck.harbinger.commands.CommandResponse;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.PermissionLevel;
import dev.zontreck.harbinger.data.types.Person;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

import java.util.UUID;

public class SupportCommands {

	public enum SubCommand {
		list ( "list" , "Lists support members" , "[none]" ),
		add ( "add" , "Adds support member" , "[string:uuid] [string:name] [int:permission level]" );


		public String cmd;
		public String description;
		public String use;


		SubCommand ( final String command , final String desc , String usage ) {
			this.cmd = command;
			this.description = desc;
			this.use = usage;
		}

		public static SubCommand valueOfCommand ( String commandText ) {
			for (
					SubCommand c :
					values ( )
			) {
				if ( c.cmd.equals ( commandText ) ) {
					return c;
				}
			}

			return null;
		}

		public static String print ( ) {
			String ret = "";
			for ( final SubCommand commands : values ( ) ) {
				ret += commands.toString ( );
				ret += "\n";
			}
			return ret;
		}

		@Override
		public String toString ( ) {
			return ( this.cmd + "\t\t-\t\t" + this.use + " - " + this.description );
		}

		public static HTMLElementBuilder render ( ) {
			HTMLElementBuilder root = new HTMLElementBuilder ( "table" );
			root.addClass ( "table-primary" ).addClass ( "text-center" ).addClass ( "table-bordered" ).addClass ( "border-black" ).addClass ( "table" ).addClass ( "rounded-4" ).addClass ( "shadow" ).addClass ( "table-striped" );
			var tableHead = root.addChild ( "thead" );
			var row = tableHead.addChild ( "tr" );
			row.addChild ( "th" ).withAttribute ( "scope" , "col" ).withText ( "Command" );
			row.addChild ( "th" ).withAttribute ( "scope" , "col" ).withText ( "Description" );

			var tableBody = root.addChild ( "tbody" );
			for (
					SubCommand cmd :
					values ( )
			) {
				var entry = tableBody.addChild ( "tr" );
				entry.withAttribute ( "data-bs-toggle" , "popover" ).withAttribute ( "data-bs-title" , "Usage" ).withAttribute ( "data-bs-custom-class" , "command-popover" ).withAttribute ( "data-bs-content" , cmd.use ).withAttribute ( "data-bs-container" , "body" ).withAttribute ( "data-bs-placement" , "left" ).withAttribute ( "data-bs-trigger" , "hover focus" );

				entry.addChild ( "td" ).withText ( cmd.cmd );
				entry.addChild ( "td" ).withText ( cmd.description );

			}

			return root;
		}
	}

	public static final String SUPPORT = "support";


	@Subscribe
	public static void onListSupport ( final HarbingerCommandEvent ev ) {
		if ( ev.command.equals ( SupportCommands.SUPPORT ) ) {
			ev.setCancelled ( true );
			var tbl = new HTMLElementBuilder ( "div" );
			if ( 0 == ev.arguments.size ( ) ) {
				CommandResponse.NOARG.addToResponse ( ev.response , "no arguments supplied" );

				tbl.addChild ( SubCommand.render ( ) );
				tbl.addChild ( "br" );
				tbl.addChild ( "br" );
				tbl.addChild ( PermissionLevel.render ( ) );
				ev.html = CommandHTMLPage.makePage ( "Support Command Index" , tbl , ev.response );

				ev.response.put ( "usage" , SubCommand.print ( ) );
			}
			else {
				SubCommand cmd = SubCommand.valueOfCommand ( ev.arguments.get ( 0 ) );
				switch ( cmd ) {
					case list -> {
						CommandResponse.OK.addToResponse ( ev.response , "ok" );
						ev.response.put ( "reps" , SupportReps.dump ( ) );


						ev.html = CommandHTMLPage.makePage ( "Support Command Index" , new HTMLElementBuilder ( "div" ) , ev.response );

						break;
					}
					case add -> {

						if ( ev.arguments.size ( ) != 4 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "Insufficient arguments." );
							ev.response.put ( "usage" , "[uuid] [first.last] [level]" );

							tbl.addChild ( SubCommand.render ( ) );
							tbl.addChild ( "br" );
							tbl.addChild ( "br" );
							tbl.addChild ( PermissionLevel.render ( ) );
							ev.html = CommandHTMLPage.makePage ( "Support Command Index" , tbl , ev.response );
							return;
						}

						CommandResponse.OK.addToResponse ( ev.response , "success" );
						String id = ev.arguments.get ( 1 );
						String name = ev.arguments.get ( 2 );
						String level = ev.arguments.get ( 3 );

						Person p = new Person ( UUID.fromString ( id ) , name , PermissionLevel.of ( Integer.parseInt ( level ) ) );
						SupportReps.add ( p );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

						ev.html = CommandHTMLPage.makePage ( "Add Support User" , tbl.addChild ( "p" ).withText ( "Action Successful" ) , ev.response );

						break;
					}
				}
			}
		}
	}


}
