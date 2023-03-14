package dev.zontreck.harbinger.commands;


import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.annotations.Subscribe;

public class HelpCommand
{
    public static final String Help = "help";

    @Subscribe
    public static void onHelp(CommandEvent ev)
    {
        if(ev.command.equals("help"))
        {
            CommandRegistry.LOGGER.info("\n"+Commands.print());
            CommandRegistry.LOGGER.info("No more commands found");
        }
    }
    

}
