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

public class HTTPServerCommands {
	public static final String HTTPCommands = "httpserver";

	public static final String SET_PORT = "setport";
	public static final String START = "start";
	public static final String STOP = "stop";


	@Subscribe
	public static void onCommand(CommandEvent ev) {
		if (ev.command.equals(HTTPCommands)) {
			if (ev.arguments.size() == 0) {
				CommandRegistry.LOGGER.info("The following are the accepted subcommands: \n\n"
						+ START + "\t\tStarts the server\n"
						+ STOP + "\t\tStops the server\n"
						+ SET_PORT + "\t\tSets the port number for the server"
				);
			} else {
				if (ev.arguments.get(0).equals(SET_PORT)) {
					ev.setCancelled(true);

					ConsolePrompt.console.printf("What should the port be changed to? [" + Persist.serverSettings.port + "] ");
					Persist.serverSettings.port = Integer.parseInt(ConsolePrompt.console.readLine());
					EventBus.BUS.post(new MemoryAlteredEvent());

					Terminal.startTerminal();
				} else if (ev.arguments.get(0).equals(START)) {
					CommandRegistry.LOGGER.info("Starting up server...");
					if (Persist.serverSettings.enabled) {
						CommandRegistry.LOGGER.info("Fatal: The server is already running");
						return;
					}
					Persist.serverSettings.enabled = true;
					HTTPServer.startServer();
					EventBus.BUS.post(new MemoryAlteredEvent());
				} else if (ev.arguments.get(0).equals(STOP)) {
					CommandRegistry.LOGGER.info("Stopping server...");
					if (!Persist.serverSettings.enabled) {
						CommandRegistry.LOGGER.info("Fatal: The server is already not running");
						return;
					}
					Persist.serverSettings.enabled = false;
					HTTPServer.stopServer();
					EventBus.BUS.post(new MemoryAlteredEvent());
				}
			}
		}
	}
}
