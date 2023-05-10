package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.Person;
import dev.zontreck.harbinger.events.APIRequestEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class APIHandler  implements HttpHandler
{
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {


		APIRequestEvent ARE = new APIRequestEvent(new JSONObject(new String(httpExchange.getRequestBody().readAllBytes())));
		EventBus.BUS.post(ARE);

		if(!ARE.isCancelled())
		{
			httpExchange.sendResponseHeaders(404, 0);
			httpExchange.close();
		}else {
			String reply = ARE.response_object.toString();
			byte[] bRep = reply.getBytes();
			httpExchange.getResponseHeaders().add("Content-Type", "application/json");
			httpExchange.sendResponseHeaders(ARE.response_status, bRep.length);
			OutputStream os = httpExchange.getResponseBody();
			os.write(bRep);
			os.close();
		}
	}
}