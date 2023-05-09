package dev.zontreck.harbinger.handlers;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.handlers.http.ProductsAPIHandlers;
import dev.zontreck.harbinger.handlers.http.SupportAPIHandlers;

public class EventsRegistry
{
	public static void register(EventBus bus)
	{
		bus.register(ProductsAPIHandlers.class);
		bus.register(SupportAPIHandlers.class);
	}
}
