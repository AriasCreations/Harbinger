package dev.zontreck.harbinger.data.containers;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryType;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.types.PresharedKey;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import dev.zontreck.harbinger.utils.Key;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class HTTPServerSettings {
	public static final String TAG = "http_server";

	public boolean enabled;
	public int port = 7768;

	public PresharedKey PSK;

	public HTTPServerSettings ( ) {
		try {
			PSK = new PresharedKey ( "changeme" );
		} catch ( Exception e ) {
			e.printStackTrace ( );
		}
	}

	public HTTPServerSettings ( final OSD tag ) {
		if(tag instanceof OSDMap map )
		{
			enabled = map.get("enable").AsBoolean ();
			port = map.get ( "port" ).AsInteger ();
			PSK = new PresharedKey ( map.get("psk") );
		}
	}


	public OSD save ( ) {
		OSDMap map = new OSDMap (  );
		map.put("enable", OSD.FromBoolean ( enabled ));
		map.put("port", OSD.FromInteger ( port ));
		map.put("psk", PSK.save ());
		return map;
	}
}
