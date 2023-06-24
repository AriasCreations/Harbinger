package dev.zontreck.harbinger.commands.simulation;


import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.harbinger.commands.support.SupportCommands;

public enum SimSubCommand {
	setBaseURL ( "set_base_url" , "Sets the base URL for Harbinger. This is used when constructing Simulator Endpoints in responses." , "[string:url]" ),
	getBaseURL ( "get_base_url" , "Returns the currently set base URL" , "[none]" ),
	setGridStatus ( "set_grid" , "Enables or disables the grid service" , "[bool]" ),
	setSimulator ( "set_sim" , "Enables or disables simulator functions" , "[bool]" ),
	setGridName ( "set_grid_name" , "Sets the grid name" , "[string:Grid name]" ),
	setGridNick ( "set_grid_nick" , "Sets the grid nickname" , "[string:Grid nick]" ),
	updateTos ( "update_tos" , "Updates the Terms Of Service last changed timestamp, forcing all users to agree again" , "[none]" ),
	updatePatch ( "update_patch" , "Updates the Patch Notes last changed timestamp, forcing all users to read again" , "[none]" );


	public String cmd;
	public String description;
	public String use;

	SimSubCommand ( String cmd , String desc , String usage ) {
		this.cmd = cmd;
		description = desc;
		this.use = usage;
	}

	public static SimSubCommand valueOfCommand ( String commandText ) {
		for (
				SimSubCommand c :
				values ( )
		) {
			if ( c.cmd.equals ( commandText ) ) {
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
		return ( this.cmd + "\t\t-\t\t" + this.use + " - " + this.description );
	}

	public static HTMLElementBuilder render ( ) {
		HTMLElementBuilder root = new HTMLElementBuilder ( "table" );
		root.addClass ( "table-primary" ).addClass ( "text-center" ).addClass ( "table-bordered" ).addClass ( "border-black" ).addClass ( "table" ).addClass ( "rounded-4" ).addClass ( "shadow" ).addClass ( "table-striped" );
		var tableHead = root.addChild ( "thead" );
		var row = tableHead.addChild ( "tr" );
		row.addChild ( "th" ).withAttribute ( "scope" , "col" ).withText ( "Command" );
		row.addChild ( "th" ).withAttribute ( "scope" , "col" ).withText ( "Description" );

		var tableBody = root.addChild ( "tbody" );
		for (
				SimSubCommand cmd :
				values ( )
		) {
			var entry = tableBody.addChild ( "tr" );
			entry.withAttribute ( "data-bs-toggle" , "popover" ).withAttribute ( "data-bs-title" , "Usage" ).withAttribute ( "data-bs-custom-class" , "command-popover" ).withAttribute ( "data-bs-content" , cmd.use ).withAttribute ( "data-bs-container" , "body" ).withAttribute ( "data-bs-placement" , "left" ).withAttribute ( "data-bs-trigger" , "hover focus" );

			entry.addChild ( "td" ).withText ( cmd.cmd );
			entry.addChild ( "td" ).withText ( cmd.description );

		}

		return root;
	}
}
