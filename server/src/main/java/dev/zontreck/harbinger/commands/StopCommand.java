package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.Terminal;

public class StopCommand {
    public static final String Stop = "stop";


    @Subscribe
    public static void onStop(CommandEvent event)
    {
        if(event.command.equals(Stop))
        {
            CommandRegistry.LOGGER.info("Server is stopping...");
            Terminal.setRunning(false);

            event.setCancelled(true);
        }
    }
}
