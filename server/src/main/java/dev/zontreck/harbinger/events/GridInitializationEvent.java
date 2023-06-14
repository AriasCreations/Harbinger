package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;

public class GridInitializationEvent extends Event {
	@Override
	public boolean isCancellable ( ) {
		return false;
	}
}
