package dev.zontreck.harbinger.events.discord;

import dev.zontreck.ariaslib.events.Event;

public class DiscordBotTokenUpdatedEvent extends Event {
	@Override
	public boolean isCancellable ( ) {
		return false;
	}
}
