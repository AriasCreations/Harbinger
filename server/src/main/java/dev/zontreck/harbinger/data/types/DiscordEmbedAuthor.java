package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordEmbedAuthor implements IJsonSerializable {
	public String name;
	public String url;
	public String icon_url;
	public String proxy_icon_url;


	@Override
	public JSONObject serialize() throws DiscordEmbedLimitsException {
		final JSONObject obj = new JSONObject();

		if (!this.name.isEmpty()) {
			if (256 < name.length())
				throw new DiscordEmbedLimitsException("The author name is limited to 256 characters");
			obj.put("name", this.name);
		}

		if (!this.url.isEmpty())
			obj.put("url", this.url);

		if (!this.icon_url.isEmpty())
			obj.put("icon_url", this.icon_url);

		if (!this.proxy_icon_url.isEmpty())
			obj.put("proxy_icon_url", this.proxy_icon_url);

		return obj;
	}

	public DiscordEmbedAuthor() {
	}

	public DiscordEmbedAuthor(final JSONObject obj) {
		this.name = obj.getString("name");
		this.url = obj.getString("url");
		this.icon_url = obj.getString("icon_url");
		this.proxy_icon_url = obj.getString("proxy_icon_url");
	}
}
