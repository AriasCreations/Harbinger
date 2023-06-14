package dev.zontreck.harbinger.simulator.types;

import java.time.Instant;
import java.util.UUID;

public class Presence
{
	public UUID AccountID;
	public UUID SessionID;

	public int GlobalX;
	public int GlobalY;

	public int CircuitCode;


	/**
	 * Used to indicate to the watchdog service when this user has DC'd.
	 *
	 * A tolerance of about 15 seconds is allowed before the Presence is marked stale and removed.
	 */
	public Instant lastPacket;

	public void ping()
	{
		lastPacket=Instant.now ();
	}


	public Presence(Account id)
	{
		AccountID = UUID.fromString ( id.UserID );
		SessionID = UUID.randomUUID ();
		GlobalX = 0;
		GlobalY = 0;
	}
}
