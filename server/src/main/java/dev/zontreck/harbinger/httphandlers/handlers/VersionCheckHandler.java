package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.harbinger.data.Persist;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class VersionCheckHandler implements HttpHandler {
	@Override
	public void handle(final HttpExchange httpExchange) throws IOException {
		final String reply = Persist.HARBINGER_VERSION;


		final byte[] bRep = reply.getBytes(StandardCharsets.UTF_8);
		httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
		httpExchange.sendResponseHeaders(200, bRep.length);
		final OutputStream os = httpExchange.getResponseBody();
		os.write(bRep);
		os.close();
	}
}
