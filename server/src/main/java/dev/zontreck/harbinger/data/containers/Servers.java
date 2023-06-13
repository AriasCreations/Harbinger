package dev.zontreck.harbinger.data.containers;

import com.google.common.collect.Maps;
import dev.zontreck.ariaslib.terminal.Task;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.harbinger.data.types.Server;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

import java.util.HashMap;
import java.util.Map;

public class Servers {
	public static final String TAG = "servers";
	public Map<String, Server> servers = Maps.newHashMap ( );

	public Servers ( ) {

	}

	public Servers ( OSD lst ) {

		servers = new HashMap<> ( );
		OSDMap servs = ( OSDMap ) lst;
		for (
				Map.Entry<String, OSD> serv :
				servs.entrySet ( )
		) {
			if ( serv.getValue ( ) instanceof OSDMap server ) {
				servers.put ( serv.getKey ( ) , new Server ( server ) );
			}
		}
	}

	public static void registerServerHandler ( ) {
		final Task watchdog = new Task ( "server_check_watchdog" , true ) {
			@Override
			public void run ( ) {

			}
		};

		DelayedExecutorService.scheduleRepeatingTask ( watchdog , 60 );
	}

	public OSD save ( ) {
		OSDMap servs = new OSDMap ( );
		for (
				Map.Entry<String, Server> entry : servers.entrySet ( )
		) {
			servs.put ( entry.getKey ( ) , entry.getValue ( ).save ( ) );
		}

		return servs;
	}

	public void add ( final Server server ) {
		this.servers.put ( server.serverNick , server );
	}

	public void remove ( final String nick ) {
		this.servers.remove ( nick );
	}

	public Server retrieve ( final String nick ) {
		return this.servers.get ( nick );
	}
}
