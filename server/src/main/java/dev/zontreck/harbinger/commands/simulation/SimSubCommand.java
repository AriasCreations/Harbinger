package dev.zontreck.harbinger.commands.simulation;


public enum SimSubCommand {
	setBaseURL ( "set_base_url" , "Sets the base URL for Harbinger. This is used when constructing Simulator Endpoints in responses." ),
	getBaseURL ( "get_base_url" , "Returns the currently set base URL" ),
	setGridStatus ( "set_grid" , "Arg [ bool ]:  Enables or disables the grid service" ),
	setSimulator ( "set_sim" , "Arg [bool]: Enables or disables simulator functions" ),
	setGridName ("set_grid_name", "Sets the grid name"),
	setGridNick("set_grid_nick", "Sets the grid nickname"),
	updateTos("update_tos", "Updates the Terms Of Service last changed timestamp, forcing all users to agree again"),
	updatePatch("update_patch", "Updates the Patch Notes last changed timestamp, forcing all users to read again");




	public String cmd;
	public String usage;

	SimSubCommand ( String cmd , String usage ) {
		this.cmd = cmd;
		this.usage = usage;
	}

	public static SimSubCommand valueOfCommand ( String commandText )
	{
		for (
				SimSubCommand c :
				values ( )
		) {
			if ( c.cmd.equals ( commandText ) )
			{
				return c;
			}
		}

		return null;
	}

	public static String print ( ) {
		String ret = "";
		for ( final SimSubCommand commands : values ( ) ) {
			ret += commands.toString ( );
			ret += "\n";
		}
		return ret;
	}

	@Override
	public String toString ( ) {
		return ( this.cmd + "\t\t-\t\t" + this.usage );
	}
}
