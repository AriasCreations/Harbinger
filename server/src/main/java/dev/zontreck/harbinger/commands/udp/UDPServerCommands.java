package dev.zontreck.harbinger.commands.udp;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandResponse;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public class UDPServerCommands {
	public static final String BASE_COMMAND = "udpserver";

	public enum UDPSubCommand {
		Enable ( "enable" , "Enables the UDP Server on next restart" ),
		Disable ( "disable" , "Disables the UDP Server on next restart" ),
		SetUDPPort ( "setport" , "Sets the UDP Port" ),
		GetUDPPort ( "getport" , "Prints out the UDP Port" );


		public String cmd;
		public String usage;

		UDPSubCommand ( String cmd , String usage ) {
			this.cmd = cmd;
			this.usage = usage;
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
			return ( this.cmd + "\t\t-\t\t" + this.usage );
		}
	}

	@Subscribe
	public static void onCommand ( HarbingerCommandEvent ev ) {
		if ( BASE_COMMAND.equalsIgnoreCase ( ev.command ) ) {

			if ( ev.arguments.size ( ) == 0 ) {
				CommandResponse.NOARG.addToResponse ( ev.response , "failed" );
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
