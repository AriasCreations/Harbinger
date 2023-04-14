package dev.zontreck.harbinger.events;

import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers context paths with handlers
 */
public class HTTPStartingEvent extends Event
{
	public int port=0x00;
	public Map<String, HttpHandler> contexts;

	public HTTPStartingEvent(int port)
	{
		this.port=port;
		this.contexts = new HashMap<>();
	}
	@Override
	public boolean isCancellable() {
		return false;
	}
}
