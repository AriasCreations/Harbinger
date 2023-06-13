package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;
import dev.zontreck.harbinger.data.types.Server;

/**
 * This event is fired when a server de-registers itself. This event cannot be cancelled as we must assume if it is deregistering, the URL is no longer valid
 */
public class HarbingerClientRemovedEvent extends Event {
	public Server server;

	public HarbingerClientRemovedEvent(final Server s) {
		this.server = s;
	}

	@Override
	public boolean isCancellable() {
		return false;
	}
}
