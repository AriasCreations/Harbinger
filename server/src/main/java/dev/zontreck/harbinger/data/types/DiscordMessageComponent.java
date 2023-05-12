package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONArray;
import org.json.JSONObject;

public class DiscordMessageComponent implements IJsonSerializable
{
	@Override
	public JSONObject serialize() throws DiscordEmbedLimitsException {
		JSONObject obj = new JSONObject();



		return obj;
	}

	public DiscordMessageComponent (){}

	public DiscordMessageComponent(JSONObject obj){

	}
}
