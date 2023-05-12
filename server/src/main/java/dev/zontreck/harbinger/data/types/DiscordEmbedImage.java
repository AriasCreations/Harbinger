package dev.zontreck.harbinger.data.types;

import org.json.JSONObject;

public class DiscordEmbedImage implements IJsonSerializable
{
	public String url;
	public String proxy_url;
	public int height;
	public int width;

	@Override
	public JSONObject serialize() {
		JSONObject obj = new JSONObject();

		if(!url.isEmpty())
			obj.put("url", url);

		if(!proxy_url.isEmpty())
			obj.put("proxy_url", proxy_url);

		obj.put("height", height);
		obj.put("width", width);

		return obj;
	}

	public DiscordEmbedImage(){
	}

	public DiscordEmbedImage(JSONObject obj)
	{

		url = obj.getString("url");
		proxy_url = obj.getString("proxy_url");
		height = obj.getInt("height");
		width = obj.getInt("width");
	}
}
