package dev.zontreck.harbinger.handlers;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.handlers.http.ProductsAPIHandlers;

public class EventsRegistry
{
	public static void register(EventBus bus)
	{
		bus.register(ProductsAPIHandlers.class);
	}
}
