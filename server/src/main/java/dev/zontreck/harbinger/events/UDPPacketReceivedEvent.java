package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;

public class UDPPacketReceivedEvent extends Event
{

	@Override
	public boolean isCancellable ( ) {
		return true;
	}
}
