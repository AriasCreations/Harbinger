package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordEmbedFooter implements IJsonSerializable {
	public String text;
	public String icon_url;
	public String proxy_icon_url;

	public DiscordEmbedFooter ( final JSONObject obj ) {
		this.text = obj.getString ( "text" );
		this.icon_url = obj.getString ( "icon_url" );
		this.proxy_icon_url = obj.getString ( "proxy_icon_url" );
	}

	public DiscordEmbedFooter ( ) {
	}

	@Override
	public JSONObject serialize ( ) throws DiscordEmbedLimitsException {
		final JSONObject obj = new JSONObject ( );

		if ( ! this.text.isEmpty ( ) ) {
			if ( 2048 < text.length ( ) )
				throw new DiscordEmbedLimitsException ( "Footer text is limited to 2048 characters" );
			obj.put ( "text" , this.text );

		}

		if ( ! this.icon_url.isEmpty ( ) )
			obj.put ( "icon_url" , this.icon_url );

		if ( ! this.proxy_icon_url.isEmpty ( ) )
			obj.put ( "proxy_icon_url" , this.proxy_icon_url );

		return obj;
	}
}
