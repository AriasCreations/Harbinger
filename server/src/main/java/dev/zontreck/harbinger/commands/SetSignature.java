package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.ConsolePrompt;
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

			if(yn.equals("Y"))
			{
				Signature sig = Signature.makeNew();
				Persist.SIGNATURE=sig;

				EventBus.BUS.post(new MemoryAlteredEvent());
			}
		}
	}
}
