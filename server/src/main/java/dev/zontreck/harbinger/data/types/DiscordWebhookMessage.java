package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DiscordWebhookMessage implements IJsonSerializable {
	public String content;
	public String username;
	public String avatar_url;
	public boolean tts;
	public List<DiscordEmbed> embeds;
	/**
	 * This is not currently implemented as i do not yet understand the structure behind this. I'll tinker some later
	 * DO NOT INITIALIZE THIS
	 * IT WILL BREAK THE PROGRAM
	 * <p>
	 * TODO: Fix this with its own class/serializers
	 */
	public Object allowed_mentions;

	/**
	 * This does not work on a normal webhook, and requires usage through the bot.
	 */
	public Object components;

	public int flags = 0;


	@Override
	public JSONObject serialize() throws DiscordEmbedLimitsException {
		JSONObject obj = new JSONObject();

		if (!content.isEmpty()) {
			if (content.length() > 2000)
				throw new DiscordEmbedLimitsException("Content length cannot be greater than 2000 characters");
			obj.put("content", content);
		}

		if (!username.isEmpty())
			obj.put("username", username);
		if (!avatar_url.isEmpty())
			obj.put("avatar_url", avatar_url);
		obj.put("tts", tts);
		if (embeds != null) {
			JSONArray arr = new JSONArray();
			for (DiscordEmbed embed : embeds) {
				arr.put(embed.serialize());
			}
			obj.put("embeds", arr);
		}

		return obj;
	}
}
