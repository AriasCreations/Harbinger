package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.ConsolePrompt;
import dev.zontreck.ariaslib.terminal.Terminal;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.types.Signature;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public enum SetSignature {
	;
	public static final String SETSIG = "setsig";

	@Subscribe
	public static void onSetSignature(final CommandEvent event) {
		if (event.command.equals(SetSignature.SETSIG)) {
			event.setCancelled(true);

			ConsolePrompt.console.printf("Generate a random signature? [Y/n] ");
			final String yn = ConsolePrompt.console.readLine();

			if ("y".equalsIgnoreCase(yn) || "".equals(yn)) {
				final Signature sig = Signature.makeNew();
				Persist.SIGNATURE = sig;

				EventBus.BUS.post(new MemoryAlteredEvent());

			} else {
				final Signature sig = new Signature();

				ConsolePrompt.console.printf("Please enter the first signature: ");
				final String A1 = ConsolePrompt.console.readLine();

				ConsolePrompt.console.printf("Please enter the second signature: ");
				final String A2 = ConsolePrompt.console.readLine();

				sig.v1 = Long.parseLong(A1);
				sig.v2 = Long.parseLong(A2);

				Persist.SIGNATURE = sig;
				EventBus.BUS.post(new MemoryAlteredEvent());
			}

			ConsolePrompt.console.printf("Signature has been set to %s, %s\n\n", Persist.SIGNATURE.v1, Persist.SIGNATURE.v2);
			Terminal.startTerminal();
		}
	}
}
