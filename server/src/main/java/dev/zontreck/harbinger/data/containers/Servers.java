package dev.zontreck.harbinger.data.containers;

import com.google.common.collect.Maps;
import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.ariaslib.terminal.Task;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.harbinger.data.types.Server;

import java.util.List;
import java.util.Map;

public class Servers {
	public Map<String, Server> servers = Maps.newHashMap ( );

	public static Servers deserialize ( final Entry<List<Entry>> lst ) {
		try {

			final Servers servers = new Servers ( );
			for ( int i = 0 ; i < lst.value.size ( ) ; i++ ) {
				final Entry<?> eX = lst.value.get ( i );
				final Server serv = Server.deserialize ( ( Entry<List<Entry>> ) eX );
				servers.servers.put ( serv.serverNick , serv );
			}

			return servers;
		} catch ( final Exception e ) {
			return new Servers ( );
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

	public Entry<List<Entry>> save ( ) {
		final Entry<List<Entry>> tag = Folder.getNew ( "servers" );
		for ( final Map.Entry<String, Server> entry : this.servers.entrySet ( ) ) {
			tag.value.add ( entry.getValue ( ).save ( ) );
		}

		return tag;
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
