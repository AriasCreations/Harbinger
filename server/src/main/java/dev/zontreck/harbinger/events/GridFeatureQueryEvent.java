package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridFeatureQueryEvent extends Event
{
	public List<String> options;
	public Map<String,Object> reply = new HashMap<> (  );

	public GridFeatureQueryEvent (List<String> opts)
	{
		options=opts;
	}




	@Override
	public boolean isCancellable ( ) {
		return true;
	}
}
