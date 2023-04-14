package dev.zontreck.harbinger.handlers;

import dev.zontreck.ariaslib.events.EventBus;

public class HandlerRegistry
{
	public static void register(EventBus bus)
	{
		bus.register(ModifyProduct.class);
	}
}
