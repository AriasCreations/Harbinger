package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;
import dev.zontreck.harbinger.data.types.Server;

/**
 * Fired when a server is added to Harbinger's registry.
 *
 * This event is cancellable, if cancelled, the registry add request is denied.
 */
public class HarbingerClientAddedEvent extends Event
{
	public Server server;
	public HarbingerClientAddedEvent(Server x)
	{
		server=x;
	}
	@Override
	public boolean isCancellable() {
		return true;
	}
}
