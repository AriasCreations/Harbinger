package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.harbinger.commands.http.HTTPServerCommands;
import dev.zontreck.harbinger.commands.simulation.SimulationCommands;
import dev.zontreck.harbinger.commands.support.SupportCommands;
import dev.zontreck.harbinger.commands.udp.UDPServerCommands;

public enum Commands {
	help ( HelpCommand.Help , "Displays the available commands" , "[none]" ),
	stop ( StopCommand.Stop , "Stops the server immediately" , "[none]" ),
	support ( SupportCommands.SUPPORT , "Manipulates the support representative list" , "[none:get usage]" ),
	save ( StopCommand.Save , "Saves the memory file immediately" , "[none]" ),
	httpserver ( HTTPServerCommands.HTTPCommands , "HTTP Server commands" , "[none:get usage]" ),
	setpsk ( SetPresharedKeyCommand.SETPSK , "Sets the HTTP Preshared Key" , "[string:new-key]" ),
	setsig ( SetSignature.SETSIG , "Sets the signature" , "[char:y] / [string:sig1] [string:sig2]" ),

	simulation ( SimulationCommands.BASE_COMMAND , "Simulator commands" , "[none:get usage]" ),
	udpserver ( UDPServerCommands.BASE_COMMAND , "UDP Server commands" , "[none:get usage]" );


	public String cmd;
	public String description;
	public String use;


	Commands ( final String command , final String desc , String usage ) {
		this.cmd = command;
		this.description = desc;
		this.use = usage;
	}

	public static String print ( ) {
		String ret = "";
		for ( final Commands commands : values ( ) ) {
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
				Commands cmd :
				values ( )
		) {
			var entry = tableBody.addChild ( "tr" );
			entry.withAttribute ( "data-bs-toggle" , "popover" ).withAttribute ( "data-bs-title" , "Usage" ).withAttribute ( "data-bs-custom-class" , "command-popover" ).withAttribute ( "data-bs-content" , cmd.use ).withAttribute ( "data-bs-container" , "body" ).withAttribute ( "data-bs-placement" , "left" ).withAttribute ( "data-bs-trigger" , "hover" );

			entry.addChild ( "td" ).withText ( cmd.cmd );
			entry.addChild ( "td" ).withText ( cmd.description );

		}

		return root;
	}

}
