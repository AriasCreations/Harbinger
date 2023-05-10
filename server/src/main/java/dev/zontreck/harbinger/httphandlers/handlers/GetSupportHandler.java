package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.Person;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GetSupportHandler implements HttpHandler
{
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		List<String> items = new ArrayList<>();
		for(Person per : SupportReps.REPS)
		{
			items.add(per.ID.toString());
			items.add(String.valueOf(per.Permissions.getFlag()));
		}
		String reply = "GetSupport;;"+ String.join("~", items);
		byte[] bRep = reply.getBytes();
		httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
		httpExchange.sendResponseHeaders(200, bRep.length);
		OutputStream os = httpExchange.getResponseBody();
		os.write(bRep);
		os.close();
	}
}
