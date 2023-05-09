package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.ConsolePrompt;
import dev.zontreck.ariaslib.terminal.Terminal;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.utils.Key;

import java.security.NoSuchAlgorithmException;

public class SetPresharedKeyCommand
{
	public static final String SETPSK = "setpsk";


	@Subscribe
	public static void onSetPSK(CommandEvent event)
	{
		if(event.command.equals(SETPSK))
		{
			event.setCancelled(true);

			ConsolePrompt.console.printf("What should the new PSK be? ");
			char[] pwd = ConsolePrompt.console.readPassword();
			String psk = new String(pwd);

			Terminal.startTerminal();

			try {
				Persist.serverSettings.PSK= Key.computeSecuredKey(psk);
			} catch (NoSuchAlgorithmException e) {
				ConsolePrompt.console.printf("Error while setting PSK: "+e.getMessage());
			}
			EventBus.BUS.post(new MemoryAlteredEvent());
		}
	}
}
