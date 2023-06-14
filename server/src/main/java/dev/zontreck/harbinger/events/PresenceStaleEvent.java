package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;
import dev.zontreck.harbinger.simulator.types.Presence;

/**
 * This event is used to signal when a Grid Presence has gone stale.
 *
 * If the event is cancelled, the presence is not deleted and will have the LastPacket flag updated.
 */
public class PresenceStaleEvent extends Event
{
	public Presence presence;

	public PresenceStaleEvent(Presence presence)
	{
		this.presence=presence;
	}

	@Override
	public boolean isCancellable ( ) {
		return true;
	}
}
