package dev.zontreck.harbinger.commands.udp;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.commands.http.HTTPServerCommands;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public class UDPServerCommands
{


	public enum UDPSubCommand
	{
		Enable("enable", "Enables the UDP Server on next restart"),
		Disable("disable", "Disables the UDP Server on next restart"),
		SetUDPPort("setport", "Sets the UDP Port"),
		GetUDPPort("getport", "Prints out the UDP Port");



		public String cmd;
		public String usage;

		UDPSubCommand ( String cmd , String usage ) {
			this.cmd = cmd;
			this.usage = usage;
		}

		public static UDPSubCommand valueOfCommand ( String commandText )
		{
			for (
					UDPSubCommand c :
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
			for ( final UDPSubCommand commands : values ( ) ) {
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

	@Subscribe
	public static void onCommand( CommandEvent ev )
	{
		if("udpserver".equalsIgnoreCase ( ev.command ))
		{
			ev.setCancelled ( true );

			if(ev.arguments.size () == 0)
			{
				UDPSubCommand.print ();
			}else {
				UDPSubCommand cmd = UDPSubCommand.valueOfCommand ( ev.arguments.get ( 0 ) );

				switch(cmd)
				{
					case GetUDPPort -> {
						CommandRegistry.LOGGER.info ( "UDP Port: " + Persist.serverSettings.udp_settings.UDPPort );

						break;
					}
					case SetUDPPort -> {
						if(ev.arguments.size () != 2)
						{
							CommandRegistry.LOGGER.info ( "You must supply the port number" );
						}else {
							Persist.serverSettings.udp_settings.UDPPort = Integer.parseInt ( ev.arguments.get ( 1 ) );
							CommandRegistry.LOGGER.info ( "Port has been set to "+ev.arguments.get ( 1 ) );

							EventBus.BUS.post ( new MemoryAlteredEvent () );
						}
						break;
					}
					case Enable -> {
						Persist.serverSettings.udp_settings.UDPServerEnabled = true;
						CommandRegistry.LOGGER.info ( "UDP Server has been enabled, this change will take effect at next Harbinger restart" );

						EventBus.BUS.post ( new MemoryAlteredEvent () );
						break;
					}
					case Disable -> {
						Persist.serverSettings.udp_settings.UDPServerEnabled=false;
						CommandRegistry.LOGGER.info ( "UDP Server has been disabled, this change will take effect at next Harbinger restart" );

						EventBus.BUS.post ( new MemoryAlteredEvent () );
						break;
					}
				}
			}

		}
	}
}
