package dev.zontreck.harbinger.commands.database;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.ariaslib.html.bootstrap.Color;
import dev.zontreck.ariaslib.terminal.Task;
import dev.zontreck.ariaslib.terminal.TaskBus;
import dev.zontreck.harbinger.commands.CommandHTMLPage;
import dev.zontreck.harbinger.commands.CommandMessage;
import dev.zontreck.harbinger.commands.CommandResponse;
import dev.zontreck.harbinger.data.mongo.DBSettings;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DatabaseCommands {
	public static final String BASE_COMMAND = "db";

	public enum SubCommand {
		SetDBHost ( "sethost" , "Sets the database host" , "[string:host FQDN or IP]" ),
		SetDBPort ( "setport" , "Sets the database port number" , "[int:port]" ),
		SetDBUser ( "setuser" , "Sets the database username" , "[string:username]" ),
		SetDBPwd ( "setpass" , "Sets the database password" , "[string:pwd]" ),
		SetNoAuth ( "setnoauth" , "Turns off the requirement to supply username and password. (Insecure mode)" , "[none]" ),
		Connect ( "connect" , "Attempts to connect to the database" , "[none]" );


		public String cmd;
		public String description;
		public String use;

		SubCommand ( String cmd , String desc , String usage ) {
			this.cmd = cmd;
			description = desc;
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

	@Subscribe
	public static void onDBCommand ( final HarbingerCommandEvent ev ) {
		if ( ev.command.equalsIgnoreCase ( BASE_COMMAND ) ) {
			ev.setCancelled ( true );
			if ( ev.arguments.size ( ) == 0 ) {
				CommandResponse.NOARG.addToResponse ( ev.response , "A subcommand was not provided" );
				HTMLElementBuilder tbl = new HTMLElementBuilder ( "div" );
				tbl.addChild ( CommandMessage.buildMessage ( Color.Danger , "No subcommand given" ) );
				tbl.addChild ( new HTMLElementBuilder ( "br" ) ).addChild ( SubCommand.render ( ) );
				ev.html = CommandHTMLPage.makePage ( "Database Commands - Usage" , tbl , ev.response );

				return;

			}
			else {
				SubCommand cmd = SubCommand.valueOfCommand ( ev.arguments.get ( 0 ) );
				try {

					Color msgColor = Color.Warning;
					String msg = "Unknown error in processing";

					switch ( cmd ) {
						case SetDBHost -> {
							msgColor = Color.Success;
							msg = "Host has been set";

							try {
								DBSettings.instance.HOST = ev.arguments.get ( 1 );
								DBSettings.SAVE ( );
								CommandResponse.OK.addToResponse ( ev.response , "ok" );
							} catch ( Exception e ) {
								throw new IllegalArgumentException ( "Host parameter not supplied" );
							}

							break;
						}
						case SetDBPort -> {
							CommandResponse.OK.addToResponse ( ev.response , "ok" );

							msgColor = Color.Success;
							msg = "Port number changed for database host";

							try {
								DBSettings.instance.PORT = Integer.parseInt ( ev.arguments.get ( 1 ) );
								DBSettings.SAVE ( );

							} catch ( Exception e ) {
								throw new IllegalArgumentException ( "Port number must be supplied as a argument to the command" );
							}
							break;
						}
						case SetNoAuth -> {
							CommandResponse.OK.addToResponse ( ev.response , "ok" );
							msgColor = Color.Info;
							msg = "The authentication method has been set to insecure.";
							DBSettings.instance.USER = "";
							DBSettings.instance.PASSWORD = "";
							DBSettings.SAVE ( );
							break;
						}
						case SetDBUser -> {
							CommandResponse.OK.addToResponse ( ev.response , "ok" );
							msgColor = Color.Success;
							msg = "User changed.";
							try {
								DBSettings.instance.USER = ev.arguments.get ( 1 );
								DBSettings.SAVE ( );
							} catch ( Exception e ) {
								throw new IllegalArgumentException ( "Username not supplied" );
							}
							break;
						}
						case SetDBPwd -> {
							CommandResponse.OK.addToResponse ( ev.response , "ok" );
							msgColor = Color.Success;
							msg = "Password updated";
							try {
								DBSettings.instance.PASSWORD = ev.arguments.get ( 1 );
								DBSettings.SAVE ( );
							} catch ( Exception e ) {
								throw new IllegalArgumentException ( "Password not supplied" );
							}
							break;
						}
						case Connect -> {

							Future<Boolean> task = CompletableFuture.supplyAsync ( ( ) -> MongoDriver.tryConnect ( ) );
							try {
								task.get ( 10 , TimeUnit.SECONDS );
								CommandResponse.OK.addToResponse ( ev.response , "success" );
								msgColor = Color.Success;
								msg = "Connection test successful. Database connection updated. A restart may be needed.";

								ev.response.put ( "restart_needed" , true );

							} catch ( Exception e ) {
								task.cancel ( true );

								CommandResponse.FAIL.addToResponse ( ev.response , "error" );
								msgColor = Color.Danger;
								msg = "Connection test failed. Database connection was not successful";
							}
							break;
						}
					}

					ev.html = CommandHTMLPage.makePage ( "Database Command" , CommandMessage.buildMessage ( msgColor , msg ) , ev.response );
				} catch ( Exception e ) {
					CommandResponse.NOARG.addToResponse ( ev.response , "Not enough arguments" );

					ev.html = CommandHTMLPage.makePage ( "Database Commands" , CommandMessage.buildMessage ( Color.Danger , e.getMessage ( ) ) , ev.response );

				}
			}
		}
	}

}
