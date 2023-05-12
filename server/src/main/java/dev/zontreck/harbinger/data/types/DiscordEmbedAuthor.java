package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordEmbedAuthor implements IJsonSerializable
{
	public String name;
	public String url;
	public String icon_url;
	public String proxy_icon_url;


	@Override
	public JSONObject serialize() throws DiscordEmbedLimitsException {
		JSONObject obj = new JSONObject();

		if(!name.isEmpty()) {
			if(name.length() > 256)throw new DiscordEmbedLimitsException("The author name is limited to 256 characters");
			obj.put("name", name);
		}

		if(!url.isEmpty())
			obj.put("url", url);

		if(!icon_url.isEmpty())
			obj.put("icon_url", icon_url);

		if(!proxy_icon_url.isEmpty())
			obj.put("proxy_icon_url", proxy_icon_url);

		return obj;
	}

	public DiscordEmbedAuthor (){
	}

	public DiscordEmbedAuthor(JSONObject obj)
	{
		name = obj.getString("name");
		url = obj.getString("url");
		icon_url = obj.getString("icon_url");
		proxy_icon_url = obj.getString("proxy_icon_url");
	}
}
