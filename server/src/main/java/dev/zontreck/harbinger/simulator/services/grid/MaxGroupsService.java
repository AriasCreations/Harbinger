package dev.zontreck.harbinger.simulator.services.grid;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.events.GridFeatureQueryEvent;
import dev.zontreck.harbinger.simulator.types.Account;

/**
 * Provides Group Optional Service: max-groups
 */
public class MaxGroupsService {

	@Subscribe
	public static void onGridFeatureQuery( GridFeatureQueryEvent ev )
	{
		if(!ev.options.contains ( "max-agent-groups" )) return;
		if(ev.currentResponse.UnlimitedGroups)
			return;

		ev.setCancelled ( true );
		ev.reply.put ( "max-agent-groups", ev.currentResponse.MaximumGroups );
	}
}
