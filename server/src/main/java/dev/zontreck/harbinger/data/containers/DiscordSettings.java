package dev.zontreck.harbinger.data.containers;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the settings for the Harbinger Discord Bot
 */
public class DiscordSettings {
	public static final String TAG = "discord";
	public String BOT_TOKEN = "";
	public Map<String, String> WEBHOOKS = new HashMap<> ( );

	public DiscordSettings ( ) {
	}


	public DiscordSettings ( final OSD tag ) {
		if ( tag instanceof OSDMap map ) {
			BOT_TOKEN = map.get ( "token" ).AsString ( );
			OSDMap hooks = ( OSDMap ) map.get ( "hooks" );
			for (
					Map.Entry<String, OSD> entry :
					hooks.entrySet ( )
			) {
				WEBHOOKS.put ( entry.getKey ( ) , entry.getValue ( ).AsString ( ) );
			}
		}
	}

	public OSD save ( ) {
		OSDMap map = new OSDMap ( );
		OSDMap hooks = new OSDMap ( );
		map.put ( "token" , OSD.FromString ( BOT_TOKEN ) );
		for (
				Map.Entry<String, String> entry :
				WEBHOOKS.entrySet ( )
		) {
			hooks.put ( entry.getKey ( ) , OSD.FromString ( entry.getValue ( ) ) );
		}
		map.put ( "hooks" , hooks );
		return map;
	}
}
