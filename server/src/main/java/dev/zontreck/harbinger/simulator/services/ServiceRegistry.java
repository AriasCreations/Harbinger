package dev.zontreck.harbinger.simulator.services;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.simulator.services.grid.*;
import dev.zontreck.harbinger.simulator.services.simulator.SimulatorUDPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRegistry
{

	public static final Logger LOGGER = LoggerFactory.getLogger ( ServiceRegistry.class.getSimpleName ( ) );
	public static void register( EventBus bus )
	{
		bus.register ( PresenceService.class );
		bus.register ( SimulatorUDPService.class );
		bus.register ( MaxGroupsService.class );
		bus.register ( GridInventoryService.class );
		bus.register ( UserProfilesService.class );
		bus.register ( LoginFlagsService.class );
	}
}
