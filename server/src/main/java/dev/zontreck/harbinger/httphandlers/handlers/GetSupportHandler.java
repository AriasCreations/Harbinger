package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.Person;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GetSupportHandler implements HttpHandler {
	@Override
	public void handle(final HttpExchange httpExchange) throws IOException {
		final List<String> items = new ArrayList<>();
		for (final Person per : SupportReps.REPS) {
			items.add(per.ID.toString());
			items.add(String.valueOf(per.Permissions.getFlag()));
		}
		final String reply = "GetSupport;;" + String.join("~", items);
		final byte[] bRep = reply.getBytes(StandardCharsets.UTF_8);
		httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
		httpExchange.sendResponseHeaders(200, bRep.length);
		final OutputStream os = httpExchange.getResponseBody();
		os.write(bRep);
		os.close();
	}
}
