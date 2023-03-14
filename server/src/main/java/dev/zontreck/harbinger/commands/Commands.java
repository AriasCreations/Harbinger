package dev.zontreck.harbinger.commands;

import java.util.function.Consumer;

import dev.zontreck.ariaslib.events.EventBus;

public enum Commands {
    help(HelpCommand.Help, "Displays the available commands"),
    stop(StopCommand.Stop, "Stops the server immediately");

    public String cmd;
    public String usage;
    Commands(String command, String usage)
    {
        cmd=command;
        this.usage=usage;
    }

    @Override
    public String toString()
    {
        return (cmd + "\t\t-\t\t" + usage);
    }

    public static String print()
    {
        String ret = "";
        for (Commands commands : Commands.values()) {
            ret += commands.toString();
            ret += "\n";
        }
        return ret;
    }

}
