package dev.zontreck.harbinger.events;

import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers context paths with handlers
 */
public class HTTPStartingEvent extends Event {
	public int port;
	public Map<String, HttpHandler> contexts;

	public HTTPStartingEvent(final int port) {
		this.port = port;
		contexts = new HashMap<>();
	}

	@Override
	public boolean isCancellable() {
		return false;
	}
}
