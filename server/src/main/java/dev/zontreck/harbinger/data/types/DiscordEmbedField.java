package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

public class DiscordEmbedField implements IJsonSerializable {
	public String name;
	public String value;
	public boolean inline;


	public DiscordEmbedField ( ) {
	}

	public DiscordEmbedField ( final JSONObject obj ) {
		this.name = obj.getString ( "name" );
		this.value = obj.getString ( "value" );
		this.inline = obj.getBoolean ( "inline" );
	}

	@Override
	public JSONObject serialize ( ) throws DiscordEmbedLimitsException {
		final JSONObject obj = new JSONObject ( );

		if ( ! this.name.isEmpty ( ) ) {
			if ( 256 < name.length ( ) )
				throw new DiscordEmbedLimitsException ( "Field name must be less than 256 characters" );
			obj.put ( "name" , this.name );
		}

		if ( ! this.value.isEmpty ( ) ) {
			if ( 4096 < value.length ( ) )
				throw new DiscordEmbedLimitsException ( "Field value must be less than 4096 characters" );
			obj.put ( "value" , this.value );
		}

		obj.put ( "inline" , this.inline );

		return obj;
	}
}
