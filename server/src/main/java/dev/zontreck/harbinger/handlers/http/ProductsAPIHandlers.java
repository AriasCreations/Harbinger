package dev.zontreck.harbinger.handlers.http;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.events.APIRequestEvent;

public class ProductsAPIHandlers {


	@Subscribe
	public static void onProductsEndpoint ( final APIRequestEvent event ) {
		if ( "products".equals ( event.request_object.getString ( "type" ) ) ) {
			if ( "make".equals ( event.request_object.getString ( "sub_command" ) ) ) {
			}
		}
	}
}
