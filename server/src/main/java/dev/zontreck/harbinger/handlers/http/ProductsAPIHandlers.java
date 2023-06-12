package dev.zontreck.harbinger.handlers.http;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.events.APIRequestEvent;

public class ProductsAPIHandlers {
	@Subscribe
	public static void onProductsEndpoint(APIRequestEvent event) {
		if (event.request_object.getString("type").equals("products")) {
			if (event.request_object.getString("sub_command").equals("make")) {
			}
		}
	}
}
