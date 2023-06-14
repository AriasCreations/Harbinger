package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;

import java.util.List;

public class GridFeatureQueryEvent extends Event
{
	public List<String> options;

	public GridFeatureQueryEvent (List<String> opts)
	{
		options=opts;
	}




	@Override
	public boolean isCancellable ( ) {
		return true;
	}
}
