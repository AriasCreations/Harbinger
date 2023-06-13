package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordMessageComponent implements IJsonSerializable {
	@Override
	public JSONObject serialize() throws DiscordEmbedLimitsException {
		final JSONObject obj = new JSONObject();


		return obj;
	}

	public DiscordMessageComponent() {
	}

	public DiscordMessageComponent(final JSONObject obj) {

	}
}
