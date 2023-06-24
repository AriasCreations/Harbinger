package dev.zontreck.harbinger.commands.udp;

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

public class UDPServerCommands {
	public static final String BASE_COMMAND = "udpserver";

	public enum UDPSubCommand {
		Enable ( "enable" , "Enables the UDP Server on next restart" , "[none]" ),
		Disable ( "disable" , "Disables the UDP Server on next restart" , "[none]" ),
		SetUDPPort ( "setport" , "Sets the UDP Port" , "[int:port]" ),
		GetUDPPort ( "getport" , "Prints out the UDP Port" , "[none]" ),
		SetExtPort ( "set_ext_port" , "Sets the external inbound port to this UDP Server" , "[int:port]" ),
		GetExtPort ( "get_ext_port" , "Gets the external port number" , "[none]" );


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
		if ( ev.command.equalsIgnoreCase ( BASE_COMMAND ) ) {
			ev.setCancelled ( true );

			if ( ev.arguments.size ( ) == 0 ) {
				CommandResponse.NOARG.addToResponse ( ev.response , "failed" );

				var tableContainer = new HTMLElementBuilder ( "div" );
				tableContainer.addChild ( UDPSubCommand.render ( ) );

				ev.html = CommandHTMLPage.makePage ( "UDP Server Commands" , tableContainer , ev.response );

				ev.response.put ( "usage" , UDPSubCommand.print ( ) );
			}
			else {
				UDPSubCommand cmd = UDPSubCommand.valueOfCommand ( ev.arguments.get ( 0 ) );
				var tbl = new HTMLElementBuilder ( "div" );

				switch ( cmd ) {
					case GetUDPPort -> {
						CommandResponse.OK.addToResponse ( ev.response , "okay!" );
						ev.response.put ( "port" , Persist.serverSettings.udp_settings.UDPPort );

						ev.html = CommandHTMLPage.makePage ( "UDP Server - Get Port" , tbl.addChild ( CommandMessage.buildMessage ( Color.Primary , "Port Number : " + Persist.serverSettings.udp_settings.UDPPort ) ) , ev.response );

						break;
					}
					case SetUDPPort -> {
						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You need to supply the port number" );

							ev.html = CommandHTMLPage.makePage ( "UDP Server - Set Port" , tbl.addChild ( CommandMessage.buildMessage ( Color.Danger , "Port number not changed because no number was supplied" ) ) , ev.response );

						}
						else {
							Persist.serverSettings.udp_settings.UDPPort = Integer.parseInt ( ev.arguments.get ( 1 ) );
							CommandResponse.OK.addToResponse ( ev.response , "Port number has been changed" );

							ev.html = CommandHTMLPage.makePage ( "UDP Server - Set Port" , tbl.addChild ( CommandMessage.buildMessage ( Color.Success , "Port number changed" ) ) , ev.response );


							EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						}
						break;
					}
					case Enable -> {
						Persist.serverSettings.udp_settings.UDPServerEnabled = true;
						CommandResponse.OK.addToResponse ( ev.response , "UDP Server enabled" );
						ev.response.put ( "restart_needed" , true );

						ev.html = CommandHTMLPage.makePage ( "UDP Server" , tbl.addChild ( CommandMessage.buildMessage ( Color.Success , "Service enabled, a restart is needed." ) ) , ev.response );

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case Disable -> {
						Persist.serverSettings.udp_settings.UDPServerEnabled = false;
						CommandResponse.OK.addToResponse ( ev.response , "UDP Server disabled" );
						ev.response.put ( "restart_needed" , true );

						ev.html = CommandHTMLPage.makePage ( "UDP Server" , tbl.addChild ( CommandMessage.buildMessage ( Color.Success , "Service disabled, a restart is needed." ) ) , ev.response );

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case GetExtPort -> {
						CommandResponse.OK.addToResponse ( ev.response , "External Port " + Persist.serverSettings.udp_settings.UDPExtPort );
						ev.html = CommandHTMLPage.makePage ( "UDP Server - External Port" , CommandMessage.buildMessage ( Color.Primary , "External Port is " + Persist.serverSettings.udp_settings.UDPExtPort ) , ev.response );

						break;
					}
					case SetExtPort -> {
						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "Not enough arguments" );
							ev.html = CommandHTMLPage.makePage ( "UDP Server - Set External Port" , CommandMessage.buildMessage ( Color.Danger , "You need to supply the port number" ) , ev.response );
							return;
						}
						else {
							CommandResponse.OK.addToResponse ( ev.response , "External port set" );
							Persist.serverSettings.udp_settings.UDPExtPort = Integer.parseInt ( ev.arguments.get ( 1 ) );
							EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

							ev.html = CommandHTMLPage.makePage ( "UDP Server - Set External Port" , CommandMessage.buildMessage ( Color.Success , "External port number set. This is primarily used in combination with port forwarding, where the outward port is not the same as the internal one. (Example: Containers).   To undo this set the external port to 0, and it will default to the primary UDP Port." ) , ev.response );
						}
						break;
					}
				}
			}

		}
	}
}
