package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordEmbedFooter implements IJsonSerializable {
	public String text;
	public String icon_url;
	public String proxy_icon_url;

	public DiscordEmbedFooter(JSONObject obj) {
		text = obj.getString("text");
		icon_url = obj.getString("icon_url");
		proxy_icon_url = obj.getString("proxy_icon_url");
	}

	public DiscordEmbedFooter() {
	}

	@Override
	public JSONObject serialize() throws DiscordEmbedLimitsException {
		JSONObject obj = new JSONObject();

		if (!text.isEmpty()) {
			if (text.length() > 2048)
				throw new DiscordEmbedLimitsException("Footer text is limited to 2048 characters");
			obj.put("text", text);

		}

		if (!icon_url.isEmpty())
			obj.put("icon_url", icon_url);

		if (!proxy_icon_url.isEmpty())
			obj.put("proxy_icon_url", proxy_icon_url);

		return obj;
	}
}
