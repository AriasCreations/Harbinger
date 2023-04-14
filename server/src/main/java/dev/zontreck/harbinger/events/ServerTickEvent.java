package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;

/**
 * Gets fired 1 time every 5 seconds.
 */
public class ServerTickEvent extends Event
{
	public static final int FREQUENCY = 5;
	@Override
	public boolean isCancellable() {
		return false;
	}
}
