package dev.zontreck.harbinger.commands.simulation;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.commands.Commands;

public class SimulationCommands
{
	public static final String BASE_COMMAND = "sim";

	public enum SimSubCommands
	{
		setBaseURL("set_base_url", "Sets the base URL for Harbinger. This is used when constructing Simulator Endpoints in responses."),
		getBaseURL("get_base_url", "Returns the currently set base URL"),
		setGridStatus("set_grid", "Arg [ bool ]:  Enables or disables the grid service"),
		setSimulator("set_sim", "Arg [bool]: Enables or disables simulator functions");


		public String cmd;
		public String usage;

		private SimSubCommands(String cmd, String usage)
		{
			this.cmd = cmd;
			this.usage = usage;
		}


		@Override
		public String toString() {
			return (this.cmd + "\t\t-\t\t" + this.usage);
		}

		public static String print() {
			String ret = "";
			for (final SimSubCommands commands : values()) {
				ret += commands.toString();
				ret += "\n";
			}
			return ret;
		}
	}


	@Subscribe
	public static void onCommand(CommandEvent ev)
	{
		if(ev.command.equals(BASE_COMMAND))
		{
			if(ev.arguments.size() == 0)
			{
				// Print Usage
				String usage = SimSubCommands.print();

				CommandRegistry.LOGGER.info("\n{}", usage);
			}
		}
	}
}
