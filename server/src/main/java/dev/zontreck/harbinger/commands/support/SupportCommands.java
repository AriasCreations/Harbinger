package dev.zontreck.harbinger.commands.support;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.ConsolePrompt;
import dev.zontreck.ariaslib.terminal.Terminal;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.PermissionLevel;
import dev.zontreck.harbinger.data.types.Person;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

import java.util.UUID;

public enum SupportCommands {
	;
	public static final String LIST_SUPPORT = "list";
	public static final String SUPPORT_ADD = "add"; // Starts a interactive prompt
	public static final String SUPPORT = "support";


	@Subscribe
	public static void onListSupport ( final CommandEvent ev ) {
		if ( ev.command.equals ( SupportCommands.SUPPORT ) ) {
			if ( 0 == ev.arguments.size ( ) ) {
				CommandRegistry.LOGGER.info ( "The following are the accepted subcommands\n \n" +

						SupportCommands.LIST_SUPPORT + "\t\t- Lists all support representatives\n" +
						SupportCommands.SUPPORT_ADD + "\t\t- Adds a new support rep\n"
				);
			}
			else {
				if ( ev.arguments.get ( 0 ).equals ( SupportCommands.LIST_SUPPORT ) ) {
					CommandRegistry.LOGGER.info ( "The following are the support reps: \n{}" , SupportReps.dump ( ) );
				}
				else if ( ev.arguments.get ( 0 ).equals ( SupportCommands.SUPPORT_ADD ) ) {
					ev.setCancelled ( true );

					ConsolePrompt.console.printf ( "\nPlease enter the Rep's UUID > " );
					final String input = ConsolePrompt.console.readLine ( );
					ConsolePrompt.console.printf ( "\nPlease enter the Rep's Second Life User Name (Not display) > " );
					final String name = ConsolePrompt.console.readLine ( );
					ConsolePrompt.console.printf ( "\nWhat level is this user? \n" );
					for ( final PermissionLevel lPermissionLevel : PermissionLevel.values ( ) ) {
						System.out.println ( lPermissionLevel.getFlag ( ) + "\t\t-\t" + lPermissionLevel.name ( ) );

					}
					ConsolePrompt.console.printf ( "\nChoose a level > " );
					final String lvl = ConsolePrompt.console.readLine ( );
					final PermissionLevel perm = PermissionLevel.of ( Integer.parseInt ( lvl ) );

					final Person p = new Person ( UUID.fromString ( input ) , name , perm );
					SupportReps.add ( p );

					Terminal.startTerminal ( );

					EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

				}
			}
		}
	}


}
