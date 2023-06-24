package dev.zontreck.harbinger.commands.http;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.ariaslib.util.EnvironmentUtils;
import dev.zontreck.harbinger.commands.CommandHTMLPage;
import dev.zontreck.harbinger.commands.CommandResponse;
import dev.zontreck.harbinger.daemons.HTTPServer;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public class HTTPServerCommands {

	public static final String HTTPCommands = "httpserver";


	public enum HTTPSubCommands {
		SetPort ( "setport" , "Sets the port number for the server" , "[int:port]" ),
		Start ( "start" , "Starts the server" , "[none]" ),
		Stop ( "stop" , "Stops the server" , "[none]" ),
		SetExtPort ( "set_ext_port" , "Sets the external port number" , "[int:port]" );


		public String cmd;
		public String description;
		public String use;

		HTTPSubCommands ( String cmd , String desc , String usage ) {
			this.cmd = cmd;
			description = desc;
			this.use = usage;
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
					HTTPSubCommands cmd :
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
	public static void onCommand ( final HarbingerCommandEvent ev ) {
		if ( ev.command.equals ( HTTPServerCommands.HTTPCommands ) ) {
			ev.setCancelled ( true );
			var tbl = new HTMLElementBuilder ( "div" );
			if ( 0 == ev.arguments.size ( ) ) {
				CommandResponse.NOARG.addToResponse ( ev.response , HTTPSubCommands.print ( ) );

				tbl.addChild ( HTTPSubCommands.render ( ) );

				ev.html = CommandHTMLPage.makePage ( "HTTP Server Command Index" , tbl , ev.response );
			}
			else {

				HTTPSubCommands cmd = HTTPSubCommands.valueOfCommand ( ev.arguments.get ( 0 ) );
				switch ( cmd ) {
					case SetPort -> {
						if ( EnvironmentUtils.isRunningInsideDocker ( ) ) {
							CommandResponse.DENY.addToResponse ( ev.response , "Action not allowed for environment" );
							tbl.addClass ( "text-bg-danger" ).withText ( "ERROR: You cannot change the port number using the command system when running inside Docker or a container. Please instead use the container's port forwarding to change the port" );

							ev.html = CommandHTMLPage.makePage ( "HTTP Server - Set Port", tbl, ev.response );

							return;
						}
						if ( ev.arguments.size ( ) == 2 ) {

							Persist.serverSettings.port = Integer.parseInt ( ev.arguments.get ( 1 ) );
							EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

							CommandResponse.OK.addToResponse ( ev.response , "Port number changed. This will not take effect until next restart" );
							ev.response.put ( "restart_needed" , true );

							ev.html = CommandHTMLPage.makePage ( "HTTP Server - Set Port" , tbl.addClass ( "text-bg-success" ).withText ( "Port successfully changed" ) , ev.response );
						}
						else {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply the port number" );

							ev.html = CommandHTMLPage.makePage ( "HTTP Server - Set Port" , tbl.addClass ( "text-bg-danger" ).withText ( "Port not changed because a port number was not supplied" ) , ev.response );
						}

						break;
					}
					case Start -> {


						if ( Persist.serverSettings.enabled ) {
							CommandResponse.DENY.addToResponse ( ev.response , "Server is already running" );
							ev.html = CommandHTMLPage.makePage ( "HTTP Server - Start" , tbl.addClass ( "text-bg-warning" ).withText ( "The server is already running. No action has been taken" ) , ev.response );
							return;
						}
						Persist.serverSettings.enabled = true;
						CommandResponse.OK.addToResponse ( ev.response , "Server started!" );
						HTTPServer.startServer ( );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						ev.response.put ( "restart_needed" , true );
						ev.html = CommandHTMLPage.makePage ( "HTTP Server - Start" , tbl.addClass ( "text-bg-success" ).withText ( "The server status has been changed. A restart may be needed." ) , ev.response );
						break;
					}
					case Stop -> {

						if ( ! Persist.serverSettings.enabled ) {
							CommandResponse.DENY.addToResponse ( ev.response , "The server is already stopped" );
							ev.html = CommandHTMLPage.makePage ( "HTTP Server - Stop" , tbl.addClass ( "text-bg-warning" ).withText ( "The server is already stopped. No action has been taken" ) , ev.response );
							return;
						}
						Persist.serverSettings.enabled = false;
						CommandResponse.OK.addToResponse ( ev.response , "The server is being stopped" );
						HTTPServer.stopServer ( );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						ev.response.put ( "restart_needed" , true );
						ev.html = CommandHTMLPage.makePage ( "HTTP Server - Stop" , tbl.addClass ( "text-bg-success" ).withText ( "The server status has been changed. A restart may be needed." ) , ev.response );
						break;
					}
					case SetExtPort -> {

						if ( ev.arguments.size ( ) != 2 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "You must supply a port number" );
							ev.html = CommandHTMLPage.makePage ( "HTTP Server - Set External Port" , tbl.addClass ( "text-bg-danger" ).withText ( "Port not changed because a port number was not supplied" ) , ev.response );
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

						ev.html = CommandHTMLPage.makePage ( "HTTP Server - Set External Port" , tbl.addClass ( "text-bg-success" ).withText ( "External port number changed" ) , ev.response );
						break;
					}
				}
			}
		}
	}
}
