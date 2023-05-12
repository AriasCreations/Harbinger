package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordEmbedProvider implements IJsonSerializable
{
	public String name;
	public String url;


	@Override
	public JSONObject serialize() throws DiscordEmbedLimitsException {
		JSONObject obj = new JSONObject();

		if(!name.isEmpty())
			obj.put("name", name);

		if(!url.isEmpty())
			obj.put("url", url);

		return obj;
	}

	public DiscordEmbedProvider(){}
	public DiscordEmbedProvider(JSONObject obj)
	{
		name = obj.getString("name");
		url = obj.getString("url");
	}
}
