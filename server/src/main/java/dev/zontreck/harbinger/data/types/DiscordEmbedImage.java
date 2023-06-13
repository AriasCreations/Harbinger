package dev.zontreck.harbinger.data.types;

import org.json.JSONObject;

public class DiscordEmbedImage implements IJsonSerializable {
	public String url;
	public String proxy_url;
	public int height;
	public int width;

	public DiscordEmbedImage ( ) {
	}

	public DiscordEmbedImage ( final JSONObject obj ) {

		this.url = obj.getString ( "url" );
		this.proxy_url = obj.getString ( "proxy_url" );
		this.height = obj.getInt ( "height" );
		this.width = obj.getInt ( "width" );
	}

	@Override
	public JSONObject serialize ( ) {
		final JSONObject obj = new JSONObject ( );

		if ( ! this.url.isEmpty ( ) )
			obj.put ( "url" , this.url );

		if ( ! this.proxy_url.isEmpty ( ) )
			obj.put ( "proxy_url" , this.proxy_url );

		obj.put ( "height" , this.height );
		obj.put ( "width" , this.width );

		return obj;
	}
}
