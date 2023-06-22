package dev.zontreck.harbinger.simulator.services.grid;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.Task;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.harbinger.events.GridInitializationEvent;
import dev.zontreck.harbinger.events.PresenceStaleEvent;
import dev.zontreck.harbinger.events.ServerTickEvent;
import dev.zontreck.harbinger.simulator.types.Presence;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PresenceService {
	private static Map<UUID, Presence> presenceDB;

	public static void registerPresence(Presence pres)
	{
		presenceDB.put ( pres.AccountID, pres );
		pres.CircuitCode = getNextCircuitCode();
	}
	@Subscribe
	public static void onGridInitialization ( GridInitializationEvent ev ) {
		// Initialize a blank presence service database
		clear ( );
	}

	private static Instant LastCheck = Instant.now ();

	@Subscribe
	public static void onServerTick( ServerTickEvent ev)
	{
		if(presenceDB == null)return; // Simulator services not yet ready
		// Only check every 15 seconds
		if(LastCheck.plusSeconds ( 15 ).isAfter( Instant.now () ))
			return;

		for (
				Map.Entry<UUID, Presence> pres :
				presenceDB.entrySet ( )
		) {
			if ( pres.getValue ( ).lastPacket.isBefore ( LastCheck ) )
			{
				// Mark Presence as stale

				if( EventBus.BUS.post ( new PresenceStaleEvent ( pres.getValue () ) ))
				{
					pres.getValue ().ping ();
					continue;
				}else {
					// Event Was Not Cancelled.
					DelayedExecutorService.scheduleTask ( new Task ( "Remove stale presence", true ) {
						@Override
						public void run ( ) {
							PresenceService.presenceDB.remove ( pres.getKey () );
						}
					}, 3 );
				}
			}
		}
	}

	private static void clear ( ) {
		if ( presenceDB == null ) {
			presenceDB = new HashMap<> ( );
		}

		presenceDB.clear ( );
	}


	private static int current_circuit_code;
	private static int getNextCircuitCode()
	{
		return current_circuit_code++;
	}

}
