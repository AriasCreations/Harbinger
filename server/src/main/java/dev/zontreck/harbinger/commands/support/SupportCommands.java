package dev.zontreck.harbinger.commands.support;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandResponse;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.PermissionLevel;
import dev.zontreck.harbinger.data.types.Person;
import dev.zontreck.harbinger.events.HarbingerCommandEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

import java.util.UUID;

public class SupportCommands {

	public enum SubCommand {
		list ( "list" , "Lists support members" ),
		add ( "add" , "Adds support member" );


		public String cmd;
		public String usage;

		SubCommand ( String cmd , String usage ) {
			this.cmd = cmd;
			this.usage = usage;
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
			return ( this.cmd + "\t\t-\t\t" + this.usage );
		}
	}

	public static final String SUPPORT = "support";


	@Subscribe
	public static void onListSupport ( final HarbingerCommandEvent ev ) {
		if ( ev.command.equals ( SupportCommands.SUPPORT ) ) {
			if ( 0 == ev.arguments.size ( ) ) {
				CommandResponse.NOARG.addToResponse ( ev.response , "no arguments supplied" );
				ev.response.put ( "usage" , SubCommand.print ( ) );
			}
			else {
				SubCommand cmd = SubCommand.valueOfCommand ( ev.arguments.get ( 0 ) );
				switch ( cmd ) {
					case list -> {
						CommandResponse.OK.addToResponse ( ev.response , "ok" );
						ev.response.put ( "reps" , SupportReps.dump ( ) );
						break;
					}
					case add -> {

						if ( ev.arguments.size ( ) != 4 ) {
							CommandResponse.NOARG.addToResponse ( ev.response , "Insufficient arguments." );
							ev.response.put ( "usage" , "[uuid] [first.last] [level]" );
							return;
						}

						CommandResponse.OK.addToResponse ( ev.response , "success" );
						String id = ev.arguments.get ( 1 );
						String name = ev.arguments.get ( 2 );
						String level = ev.arguments.get ( 3 );

						Person p = new Person ( UUID.fromString ( id ) , name , PermissionLevel.of ( Integer.parseInt ( level ) ) );
						SupportReps.add ( p );
						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						break;
					}
				}
			}
		}
	}


}
