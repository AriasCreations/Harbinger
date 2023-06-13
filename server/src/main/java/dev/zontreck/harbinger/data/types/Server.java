package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

public class Server {
	public String serverNick;
	public String serverURL;

	public Server ( ) {

	}

	public Server ( String nick , String url ) {
		this.serverNick = nick;
		this.serverURL = url;
	}

	public Server ( OSD entry ) {
		if ( entry instanceof OSDMap map ) {
			serverNick = map.get ( "nick" ).AsString ( );
			serverURL = map.get ( "url" ).AsString ( );
		}
	}

	public OSD save ( ) {
		OSDMap map = new OSDMap ( );
		map.put ( "nick" , OSD.FromString ( serverNick ) );
		map.put ( "url" , OSD.FromString ( serverURL ) );
		return map;
	}
}
