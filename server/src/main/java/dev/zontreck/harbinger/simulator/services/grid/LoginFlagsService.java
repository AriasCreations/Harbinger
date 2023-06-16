package dev.zontreck.harbinger.simulator.services.grid;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.events.GridFeatureQueryEvent;
import dev.zontreck.harbinger.simulator.types.LoginFlags;

public class LoginFlagsService
{
	@Subscribe
	public static void onGridFeatureQuery( GridFeatureQueryEvent ev )
	{
		if(ev.options.contains ( "login-flags" ))
		{
			ev.reply.put ( "login-flags", new LoginFlags (ev.userAccount).save() );
		}
	}
}
