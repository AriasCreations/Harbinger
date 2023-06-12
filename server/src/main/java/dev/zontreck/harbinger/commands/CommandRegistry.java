package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.http.HTTPServerCommands;
import dev.zontreck.harbinger.commands.support.SupportCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class CommandRegistry {
	public static final Logger LOGGER = LoggerFactory.getLogger(CommandRegistry.class.getSimpleName());

	public static void register(EventBus bus) {
		bus.register(HelpCommand.class);
		bus.register(StopCommand.class);
		bus.register(SupportCommands.class);
		bus.register(HTTPServerCommands.class);
		bus.register(SetPresharedKeyCommand.class);
		bus.register(SetSignature.class);


		bus.register(CommandRegistry.class);
	}


	@Subscribe
	public static void onCommand(CommandEvent ev) {
		String args = "";
		Iterator<String> it = ev.arguments.iterator();

		while (it.hasNext()) {
			String str = it.next();
			args += str;

			if (it.hasNext()) args += ", ";

		}
		LOGGER.debug("Command executed: " + ev.command + "; args: [" + args + "]");
	}
}
