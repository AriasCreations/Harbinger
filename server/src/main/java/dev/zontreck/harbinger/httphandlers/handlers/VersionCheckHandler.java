package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.harbinger.data.Persist;

import java.io.IOException;
import java.io.OutputStream;

public class VersionCheckHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String reply = Persist.HARBINGER_VERSION;


		byte[] bRep = reply.getBytes();
		httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
		httpExchange.sendResponseHeaders(200, bRep.length);
		OutputStream os = httpExchange.getResponseBody();
		os.write(bRep);
		os.close();
	}
}
