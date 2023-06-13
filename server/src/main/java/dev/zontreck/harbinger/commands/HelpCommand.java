package dev.zontreck.harbinger.commands;


import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.annotations.Subscribe;

public enum HelpCommand {
	;
	public static final String Help = "help";

	@Subscribe
	public static void onHelp(final CommandEvent ev) {
		if ("help".equals(ev.command)) {
			CommandRegistry.LOGGER.info("\n{}", Commands.print());
			CommandRegistry.LOGGER.info("No more commands found");
		}
	}


}
