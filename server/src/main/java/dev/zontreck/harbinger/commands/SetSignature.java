package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.ConsolePrompt;
import dev.zontreck.ariaslib.terminal.Terminal;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.types.Signature;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public class SetSignature
{
	public static final String SETSIG = "setsig";

	@Subscribe
	public static void onSetSignature(CommandEvent event)
	{
		if(event.command.equals(SETSIG))
		{
			event.setCancelled(true);

			ConsolePrompt.console.printf("Generate a random signature? [Y/n] ");
			String yn = ConsolePrompt.console.readLine();

			if(yn.toLowerCase().equals("y") || yn.equals(""))
			{
				Signature sig = Signature.makeNew();
				Persist.SIGNATURE=sig;

				EventBus.BUS.post(new MemoryAlteredEvent());

			}else {
				Signature sig = new Signature();

				ConsolePrompt.console.printf("Please enter the first signature: ");
				String A1 = ConsolePrompt.console.readLine();

				ConsolePrompt.console.printf("Please enter the second signature: ");
				String A2 = ConsolePrompt.console.readLine();

				sig.v1 = Long.parseLong(A1);
				sig.v2 = Long.parseLong(A2);

				Persist.SIGNATURE=sig;
				EventBus.BUS.post(new MemoryAlteredEvent());
			}

			ConsolePrompt.console.printf("Signature has been set to %s, %s\n\n", Persist.SIGNATURE.v1, Persist.SIGNATURE.v2);
			Terminal.startTerminal();
		}
	}
}
