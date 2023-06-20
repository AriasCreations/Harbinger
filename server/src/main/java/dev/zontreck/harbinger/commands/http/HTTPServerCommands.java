package dev.zontreck.harbinger.commands.http;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandResponse;
import dev.zontreck.harbinger.daemons.HTTPServer;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public class HTTPServerCommands {

	public static final String HTTPCommands = "httpserver";


	public enum HTTPSubCommands {
		SetPort ( "setport" , "Sets the port number for the server" ),
		Start ( "start" , "Starts the server" ),
		Stop ( "stop" , "Stops the server" ),
		SetExtPort ( "set_ext_port" , "Sets the external port number" );


		public String cmd;
		public String usage;

		HTTPSubCommands ( String cmd , String usage ) {
			this.cmd = cmd;
			this.usage = usage;
		}

		public static HTTPSubCommands valueOfCommand ( String commandText ) {
			for (
					HTTPSubCommands c :
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
			for ( final HTTPSubCommands commands : values ( ) ) {
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
	public static void onCommand ( final HarbingerCommandEvent ev ) {
		if ( ev.command.equals ( HTTPServerCommands.HTTPCommands ) ) {
			if ( 0 == ev.arguments.size ( ) ) {
				CommandResponse.NOARG.addToResponse ( ev.response , HTTPSubCommands.print ( ) );
			}
			else {

				HTTPSubCommands cmd = HTTPSubCommands.valueOfCommand ( ev.arguments.get ( 0 ) );
				switch ( cmd ) {
					case SetPort -> {
						if ( ev.arguments.size ( ) == 2 ) {

							Persist.serverSettings.port = Integer.parseInt ( ev.arguments.get ( 1 ) );
							EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

							CommandResponse.OK.addToResponse ( ev.response , "Port number changed. This will not take effect until next restart" );
						}
						else {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply the port number" );
						}

						break;
					}
					case Start -> {


						if ( Persist.serverSettings.enabled ) {
							CommandResponse.DENY.addToResponse ( ev.response , "Server is already running" );
							return;
						}
						Persist.serverSettings.enabled = true;
						CommandResponse.OK.addToResponse ( ev.response , "Server started!" );
						HTTPServer.startServer ( );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case Stop -> {

						if ( ! Persist.serverSettings.enabled ) {
							CommandResponse.DENY.addToResponse ( ev.response , "The server is already stopped" );
							return;
						}
						Persist.serverSettings.enabled = false;
						CommandResponse.OK.addToResponse ( ev.response , "The server is being stopped" );
						HTTPServer.stopServer ( );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case SetExtPort -> {

						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply a port number" );
							return;
						}
						else {
							CommandResponse.OK.addToResponse ( ev.response , "Value changed to " + ev.arguments.get ( 1 ) );
						}

						String x = ev.arguments.get ( 1 );
						if ( x.isEmpty ( ) || "0".equals ( x ) ) {
							Persist.serverSettings.ExternalPortNumber = 0;
						}
						else {
							Persist.serverSettings.ExternalPortNumber = Integer.parseInt ( x );
						}

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
				}
			}
		}
	}
}
