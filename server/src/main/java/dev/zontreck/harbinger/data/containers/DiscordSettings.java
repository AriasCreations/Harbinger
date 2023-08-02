package dev.zontreck.harbinger.data.containers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOptions;
import dev.zontreck.harbinger.data.containers.types.DiscordWebhook;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.data.types.GenericClass;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonValue;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.conversions.Bson;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Contains the settings for the Harbinger Discord Bot
 */
public class DiscordSettings {
	public static final String TAG = "discord_webhooks";
	public static final String MAIN_TAG = "discordsettings";

	public String BOT_TOKEN = "";

	@BsonIgnore
	protected Map<UUID, DiscordWebhook> WEBHOOKS = new HashMap<> ( );

	public DiscordSettings ( ) {

		DBSession discordSettingsLoadUp = MongoDriver.makeSession();
		GenericClass<DiscordSettings> mainClz = new GenericClass<>(DiscordSettings.class);
		MongoCollection<DiscordSettings> mainTable = discordSettingsLoadUp.getTableFor(MAIN_TAG, mainClz);

		var pojo = mainTable.find().first();
		if(pojo != null)
		{
			BOT_TOKEN = pojo.BOT_TOKEN;
		}else BOT_TOKEN = "";

		GenericClass<DiscordWebhook> hookClz = new GenericClass<>(DiscordWebhook.class);
		MongoCollection<DiscordWebhook> hookz = discordSettingsLoadUp.getTableFor(TAG, hookClz);

		for (DiscordWebhook hook :
				hookz.find()) {
			WEBHOOKS.put(hook.getID(), hook);
		}

		MongoDriver.closeSession(discordSettingsLoadUp);
	}


	public void addNewWebhook(DiscordWebhook hook)
	{
		DBSession session = MongoDriver.makeSession();
		MongoCollection<DiscordWebhook> hooks = session.getTableFor(TAG, (new GenericClass<>(DiscordWebhook.class)));
		hooks.insertOne(hook);

		WEBHOOKS.put(hook.getID(), hook);

		MongoDriver.closeSession(session);
	}

	public void removeHook(DiscordWebhook hook)
	{
		DBSession session = MongoDriver.makeSession();
		MongoCollection<DiscordWebhook> hooks = session.getTableFor(TAG, (new GenericClass<>(DiscordWebhook.class)));

		BsonDocument query = new BsonDocument();
		query.put("id0", new BsonInt64(hook.MSB));
		query.put("id1", new BsonInt64(hook.LSB));

		hooks.deleteOne(query);
		WEBHOOKS.remove(hook.getID());


		MongoDriver.closeSession(session);
	}

	public DiscordWebhook getHook(String nick)
	{
		return WEBHOOKS.values().stream().filter(x->x.WebHookName.equals(nick)).collect(Collectors.toList()).get(0);
	}
}
