package dev.zontreck.harbinger.commands.udp;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.harbinger.commands.CommandHTMLPage;
import dev.zontreck.harbinger.commands.CommandResponse;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public class UDPServerCommands {
	public static final String BASE_COMMAND = "udpserver";

	public enum UDPSubCommand {
		Enable ( "enable" , "Enables the UDP Server on next restart" , "[none]" ),
		Disable ( "disable" , "Disables the UDP Server on next restart" , "[none]" ),
		SetUDPPort ( "setport" , "Sets the UDP Port" , "[int:port]" ),
		GetUDPPort ( "getport" , "Prints out the UDP Port" , "[none]" );


		public String cmd;
		public String description;
		public String use;


		UDPSubCommand ( final String command , final String desc , String usage ) {
			this.cmd = command;
			this.description = desc;
			this.use = usage;
		}

		public static UDPSubCommand valueOfCommand ( String commandText ) {
			for (
					UDPSubCommand c :
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
			for ( final UDPSubCommand commands : values ( ) ) {
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
					UDPSubCommand cmd :
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

	@Subscribe
	public static void onCommand ( HarbingerCommandEvent ev ) {
		if ( BASE_COMMAND.equalsIgnoreCase ( ev.command ) ) {
			ev.setCancelled ( true );

			if ( ev.arguments.size ( ) == 0 ) {
				CommandResponse.NOARG.addToResponse ( ev.response , "failed" );

				var tableContainer = new HTMLElementBuilder ( "div" ).addClass ( "table-responsive" );
				tableContainer.addChild ( UDPSubCommand.render ( ) );

				ev.html = CommandHTMLPage.makePage ( "UDP Server Commands" , tableContainer , ev.response );

				ev.response.put ( "usage" , UDPSubCommand.print ( ) );
			}
			else {
				UDPSubCommand cmd = UDPSubCommand.valueOfCommand ( ev.arguments.get ( 0 ) );

				switch ( cmd ) {
					case GetUDPPort -> {
						CommandResponse.OK.addToResponse ( ev.response , "okay!" );
						ev.response.put ( "port" , Persist.serverSettings.udp_settings.UDPPort );

						break;
					}
					case SetUDPPort -> {
						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You need to supply the port number" );
						}
						else {
							Persist.serverSettings.udp_settings.UDPPort = Integer.parseInt ( ev.arguments.get ( 1 ) );
							CommandResponse.OK.addToResponse ( ev.response , "Port number has been changed" );

							EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						}
						break;
					}
					case Enable -> {
						Persist.serverSettings.udp_settings.UDPServerEnabled = true;
						CommandResponse.OK.addToResponse ( ev.response , "UDP Server enabled" );
						ev.response.put ( "restart_needed" , true );


						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case Disable -> {
						Persist.serverSettings.udp_settings.UDPServerEnabled = false;
						CommandResponse.OK.addToResponse ( ev.response , "UDP Server disabled" );
						ev.response.put ( "restart_needed" , true );


						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
				}
			}

		}
	}
}
