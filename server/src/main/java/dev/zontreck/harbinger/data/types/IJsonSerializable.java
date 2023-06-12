package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public interface IJsonSerializable {
	JSONObject serialize() throws DiscordEmbedLimitsException;
}
