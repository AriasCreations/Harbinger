package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;
import dev.zontreck.harbinger.simulator.types.Account;
import dev.zontreck.harbinger.simulator.types.LLoginResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridFeatureQueryEvent extends Event
{
	public List<String> options;
	public Map<String,Object> reply = new HashMap<> (  );
	public Account userAccount;

	public LLoginResponse currentResponse;

	public GridFeatureQueryEvent ( List<String> opts, LLoginResponse current_response )
	{
		options=opts;
		userAccount = current_response.cached;
		currentResponse = current_response;
	}




	@Override
	public boolean isCancellable ( ) {
		return true;
	}
}
