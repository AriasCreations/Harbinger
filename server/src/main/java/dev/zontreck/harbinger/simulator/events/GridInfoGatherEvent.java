package dev.zontreck.harbinger.simulator.events;

import dev.zontreck.ariaslib.events.Event;
import dev.zontreck.harbinger.simulator.types.GridInfo;

public class GridInfoGatherEvent extends Event {
	public GridInfo info;

	public GridInfoGatherEvent ( GridInfo base ) {
		info = base.clone ( );
	}

	@Override
	public boolean isCancellable ( ) {
		return true;
	}
}
