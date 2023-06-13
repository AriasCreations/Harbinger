package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordMessageComponent implements IJsonSerializable {
	public DiscordMessageComponent ( ) {
	}

	public DiscordMessageComponent ( final JSONObject obj ) {

	}

	@Override
	public JSONObject serialize ( ) throws DiscordEmbedLimitsException {
		final JSONObject obj = new JSONObject ( );


		return obj;
	}
}
