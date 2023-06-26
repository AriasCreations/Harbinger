package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.database.DatabaseCommands;
import dev.zontreck.harbinger.commands.http.HTTPServerCommands;
import dev.zontreck.harbinger.commands.simulation.SimulationCommands;
import dev.zontreck.harbinger.commands.support.SupportCommands;
import dev.zontreck.harbinger.commands.udp.UDPServerCommands;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class CommandRegistry {

	public static final Logger LOGGER = LoggerFactory.getLogger ( CommandRegistry.class.getSimpleName ( ) );

	public static void register ( final EventBus bus ) {


		bus.register ( SupportCommands.class );
		bus.register ( HTTPServerCommands.class );
		bus.register ( SetPresharedKeyCommand.class );
		bus.register ( SetSignature.class );
		bus.register ( SimulationCommands.class );
		bus.register ( UDPServerCommands.class );


		bus.register ( StopCommand.class );
		bus.register ( HelpCommand.class );
		bus.register( DatabaseCommands.class);
		bus.register ( CommandRegistry.class );
	}


	@Subscribe
	public static void onCommand ( final CommandEvent ev ) {
		String args = "";
		final Iterator<String> it = ev.arguments.iterator ( );

		while ( it.hasNext ( ) ) {
			final String str = it.next ( );
			args += str;

			if ( it.hasNext ( ) ) args += ", ";

		}
		CommandRegistry.LOGGER.debug ( "Command executed: {}; args: [{}]" , ev.command , args );
	}
}
