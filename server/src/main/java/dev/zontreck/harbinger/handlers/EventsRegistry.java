package dev.zontreck.harbinger.handlers;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.handlers.http.CommandAPIHandler;
import dev.zontreck.harbinger.handlers.http.ProductsAPIHandlers;
import dev.zontreck.harbinger.handlers.http.ServersAPI;
import dev.zontreck.harbinger.handlers.http.SupportAPIHandlers;
import dev.zontreck.harbinger.handlers.http.simulator.GetGridInfoHandler;
import dev.zontreck.harbinger.handlers.http.simulator.SimulatorLoginHandler;

public class EventsRegistry {


	public static void register ( final EventBus bus ) {
		bus.register ( ProductsAPIHandlers.class );
		bus.register ( SupportAPIHandlers.class );
		bus.register ( ServersAPI.class );
		bus.register ( CommandAPIHandler.class );

		if( Persist.simulatorSettings.GRID_ON){
			bus.register ( GetGridInfoHandler.class );
			bus.register ( SimulatorLoginHandler.class );
		}
	}
}
