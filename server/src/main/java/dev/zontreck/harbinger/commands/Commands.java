package dev.zontreck.harbinger.commands;

import dev.zontreck.harbinger.commands.http.HTTPServerCommands;
import dev.zontreck.harbinger.commands.simulation.SimulationCommands;
import dev.zontreck.harbinger.commands.support.SupportCommands;

public enum Commands {
	help(HelpCommand.Help, "Displays the available commands"),
	stop(StopCommand.Stop, "Stops the server immediately"),
	support(SupportCommands.SUPPORT, "Manipulates the support representative list"),
	save(StopCommand.Save, "Saves the memory file immediately"),
	httpserver(HTTPServerCommands.HTTPCommands, "HTTP Server commands"),
	setpsk(SetPresharedKeyCommand.SETPSK, "Sets the HTTP Preshared Key"),
	setsig(SetSignature.SETSIG, "Sets the signature"),

	simulation(SimulationCommands.BASE_COMMAND, "Simulator commands");


	public String cmd;
	public String usage;

	Commands(final String command, final String usage) {
		this.cmd = command;
		this.usage = usage;
	}

	@Override
	public String toString() {
		return (this.cmd + "\t\t-\t\t" + this.usage);
	}

	public static String print() {
		String ret = "";
		for (final Commands commands : values()) {
			ret += commands.toString();
			ret += "\n";
		}
		return ret;
	}

}
