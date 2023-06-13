package dev.zontreck.harbinger.handlers.http;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.types.Product;
import dev.zontreck.harbinger.events.APIRequestEvent;
import org.json.JSONObject;

import java.util.UUID;

public enum ModifyProduct {
	;

	@Subscribe
	public static void onAPIRequest ( final APIRequestEvent event ) {
		if ( "modify_product".equals ( event.request_object.getString ( "type" ) ) ) {
			switch ( event.request_object.getString ( "action" ) ) {
				case "update": {
					if ( Persist.products.hasProduct ( UUID.fromString ( event.request_object.getString ( "product" ) ) ) ) {
					}
					break;
				}
				case "make": {
					final long prod_num = Product.SEQUENCE.getAndIncrement ( );
					final UUID ID = Product.makeProductID ( event.request_object.getLong ( "group" ) , prod_num );
					final JSONObject resp = new JSONObject ( );
					resp.put ( "product" , prod_num );

					break;
				}
			}
		}
	}
}
