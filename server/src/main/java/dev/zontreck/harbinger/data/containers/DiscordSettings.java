package dev.zontreck.harbinger.data.containers;

import com.mongodb.client.MongoCollection;
import dev.zontreck.harbinger.data.containers.types.DiscordWebhook;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.data.types.GenericClass;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contains the settings for the Harbinger Discord Bot
 */
public class DiscordSettings {
	public static final String TAG = "discord";
	public String BOT_TOKEN = "";
	public Map<UUID, DiscordWebhook> WEBHOOKS = new HashMap<> ( );

	public DiscordSettings ( ) {
	}


	public DiscordSettings ( final OSD tag ) {
		if ( tag instanceof OSDMap map ) {
			BOT_TOKEN = map.get ( "token" ).AsString ( );

			DBSession discordSettingsLoadUp = MongoDriver.makeSession();

			GenericClass<DiscordWebhook> hookClz = new GenericClass<>(DiscordWebhook.class);
			MongoCollection<DiscordWebhook> hookz = discordSettingsLoadUp.getTableFor("discord_webhooks", hookClz);

			for (DiscordWebhook hook :
					hookz.find()) {
				WEBHOOKS.put(hook.getID(), hook);
			}
		}
	}

	public OSD save ( ) {
		OSDMap map = new OSDMap ( );
		map.put ( "token" , OSD.FromString ( BOT_TOKEN ) );
		return map;
	}
}
