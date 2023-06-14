package dev.zontreck.harbinger.simulator.services;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.simulator.services.grid.PresenceService;

public class ServiceRegistry
{
	public static void register( EventBus bus )
	{
		bus.register ( PresenceService.class );
	}
}
