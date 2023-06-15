package dev.zontreck.harbinger.commands.http;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.ConsolePrompt;
import dev.zontreck.ariaslib.terminal.Terminal;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.daemons.HTTPServer;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public enum HTTPServerCommands {
	;
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
	public static void onCommand ( final CommandEvent ev ) {
		if ( ev.command.equals ( HTTPServerCommands.HTTPCommands ) ) {
			if ( 0 == ev.arguments.size ( ) ) {
				CommandRegistry.LOGGER.info ( HTTPSubCommands.print ( ) );
			}
			else {

				HTTPSubCommands cmd = HTTPSubCommands.valueOfCommand ( ev.arguments.get ( 0 ) );
				switch ( cmd ) {
					case SetPort -> {

						ev.setCancelled ( true );

						ConsolePrompt.console.printf ( "What should the port be changed to? [" + Persist.serverSettings.port + "] " );
						Persist.serverSettings.port = Integer.parseInt ( ConsolePrompt.console.readLine ( ) );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

						Terminal.startTerminal ( );
						break;
					}
					case Start -> {

						CommandRegistry.LOGGER.info ( "Starting up server..." );
						if ( Persist.serverSettings.enabled ) {
							CommandRegistry.LOGGER.info ( "Fatal: The server is already running" );
							return;
						}
						Persist.serverSettings.enabled = true;
						HTTPServer.startServer ( );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case Stop -> {

						CommandRegistry.LOGGER.info ( "Stopping server..." );
						if ( ! Persist.serverSettings.enabled ) {
							CommandRegistry.LOGGER.info ( "Fatal: The server is already not running" );
							return;
						}
						Persist.serverSettings.enabled = false;
						HTTPServer.stopServer ( );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
					case SetExtPort -> {
						ev.setCancelled ( true );

						ConsolePrompt.console.printf ( "What should the external port number be set to? (To unset the external port number, supply a blank entry or a 0)  [" + ( Persist.serverSettings.ExternalPortNumberSet ? Persist.serverSettings.ExternalPortNumber : Persist.serverSettings.port ) + "]" );

						String x = ConsolePrompt.console.readLine ( );
						if ( x.isEmpty ( ) || "0".equals ( x ) ) {
							Persist.serverSettings.ExternalPortNumber = 0;
						}
						else {
							Persist.serverSettings.ExternalPortNumber = Integer.parseInt ( x );
						}

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						Terminal.startTerminal ( );
						break;
					}
				}
			}
		}
	}
}
