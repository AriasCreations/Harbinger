package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;

/**
 * This event is fired manually when altering memory.
 * <p>
 * It should be fired in order to save the memory to disk.
 */
public class MemoryAlteredEvent extends Event {

	@Override
	public boolean isCancellable() {
		return false;
	}
}
