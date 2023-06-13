package dev.zontreck.harbinger.handlers.http;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.types.Server;
import dev.zontreck.harbinger.events.APIRequestEvent;
import dev.zontreck.harbinger.events.HarbingerClientAddedEvent;
import dev.zontreck.harbinger.events.HarbingerClientRemovedEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

public enum ServersAPI {
	;

	@Subscribe
	public static void onServersRequest ( final APIRequestEvent event ) {
		if ( "servers".equals ( event.request_object.getString ( "type" ) ) ) {
			final String subcmd = event.request_object.getString ( "sub_command" );
			event.setCancelled ( true );
			switch ( subcmd ) {
				case "register": {
					if ( ! Persist.serverSettings.PSK.validate ( event.request_object.getString ( "psk" ) ) ) {
						event.response_object.put ( "result" , "Admin access required" );
						event.response_object.put ( "success" , false );
						return;
					}
					final String srvName = event.request_object.getString ( "name" );
					final String srvUrl = event.request_object.getString ( "url" );
					final Server srv = new Server ( );
					srv.serverURL = srvUrl;
					srv.serverNick = srvName;

					// Add to server registry
					if ( EventBus.BUS.post ( new HarbingerClientAddedEvent ( srv ) ) ) {
						// Add request denied
						event.response_object.put ( "result" , "The server could not be added for a unknown reason" );
						event.response_object.put ( "success" , false );
						return;
					}
					else {
						Persist.servers.add ( srv );

						EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
						event.response_object.put ( "result" , "The server has been added" );
						event.response_object.put ( "success" , true );
					}

					break;
				}
				case "deregister": {
					if ( ! Persist.serverSettings.PSK.validate ( event.request_object.getString ( "psk" ) ) ) {
						event.response_object.put ( "result" , "Admin access required" );
						event.response_object.put ( "success" , false );
						return;
					}

					final Server srv = Persist.servers.retrieve ( event.request_object.getString ( "name" ) );

					EventBus.BUS.post ( new HarbingerClientRemovedEvent ( srv ) );

					Persist.servers.remove ( srv.serverNick );
					EventBus.BUS.post ( new MemoryAlteredEvent ( ) );

					event.response_object.put ( "success" , true );
					event.response_object.put ( "result" , "The server has been removed" );

					break;
				}
			}
		}
	}
}
