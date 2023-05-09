package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.Person;
import dev.zontreck.harbinger.data.types.PresharedKey;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StopServerHandler implements HttpHandler
{
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		InputStream IS = httpExchange.getRequestBody();
		String reqJson = new String(IS.readAllBytes());

		JSONObject obj = new JSONObject(reqJson);
		String reply = "Stop;;";
		if(Persist.serverSettings.PSK.validate(obj.getString("psk")))
		{
			reply += "OK";
			CommandEvent ce = new CommandEvent("stop");
			EventBus.BUS.post(ce);
		}else reply += "FAIL";

		byte[] bRep = reply.getBytes();
		httpExchange.sendResponseHeaders(200, bRep.length);
		OutputStream os = httpExchange.getResponseBody();
		os.write(bRep);
		os.close();
	}
}