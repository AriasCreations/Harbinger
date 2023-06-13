package dev.zontreck.harbinger.events.discord;

import dev.zontreck.ariaslib.events.Event;

public class DiscordBotSettingsUpdatedEvent extends Event {
	@Override
	public boolean isCancellable ( ) {
		return false;
	}
}
