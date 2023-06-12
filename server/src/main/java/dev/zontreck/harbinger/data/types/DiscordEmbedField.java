package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordEmbedField implements IJsonSerializable {
	public String name;
	public String value;
	public boolean inline;


	@Override
	public JSONObject serialize() throws DiscordEmbedLimitsException {
		JSONObject obj = new JSONObject();

		if (!name.isEmpty()) {
			if (name.length() > 256)
				throw new DiscordEmbedLimitsException("Field name must be less than 256 characters");
			obj.put("name", name);
		}

		if (!value.isEmpty()) {
			if (value.length() > 4096)
				throw new DiscordEmbedLimitsException("Field value must be less than 4096 characters");
			obj.put("value", value);
		}

		obj.put("inline", inline);

		return obj;
	}

	public DiscordEmbedField() {
	}

	public DiscordEmbedField(JSONObject obj) {
		name = obj.getString("name");
		value = obj.getString("value");
		inline = obj.getBoolean("inline");
	}
}
