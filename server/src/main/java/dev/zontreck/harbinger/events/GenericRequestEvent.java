package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;

public class GenericRequestEvent extends Event {
	public String path;
	public String method;
	public byte[] body;


	public byte[] response;
	public String responseText;

	public boolean responseIsBinary;

	public String contentType = "text/html";
	public int responseCode = 404;

	public GenericRequestEvent ( final String path , final String method , final byte[] body ) {
		this.path = path;
		this.method = method;
		this.body = body;
	}

	@Override
	public boolean isCancellable ( ) {
		return true;
	}
}
