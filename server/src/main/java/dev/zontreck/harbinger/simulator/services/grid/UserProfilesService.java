package dev.zontreck.harbinger.simulator.services.grid;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.events.GridFeatureQueryEvent;
import dev.zontreck.harbinger.simulator.types.UIConfig;

public class UserProfilesService
{
	public static final Boolean allowProfiles = false; // In development, deny this
	@Subscribe
	public static void onGridQuery( GridFeatureQueryEvent ev )
	{
		ev.reply.put ( "ui-config", new UIConfig (allowProfiles).save () );

	}
}
