package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordEmbedProvider implements IJsonSerializable {
	public String name;
	public String url;


	public DiscordEmbedProvider ( ) {
	}

	public DiscordEmbedProvider ( final JSONObject obj ) {
		this.name = obj.getString ( "name" );
		this.url = obj.getString ( "url" );
	}

	@Override
	public JSONObject serialize ( ) throws DiscordEmbedLimitsException {
		final JSONObject obj = new JSONObject ( );

		if ( ! this.name.isEmpty ( ) )
			obj.put ( "name" , this.name );

		if ( ! this.url.isEmpty ( ) )
			obj.put ( "url" , this.url );

		return obj;
	}
}
