package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.events.discord.DiscordBotTokenUpdatedEvent;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Special Endpoint for Discord
 *
 * This interacts with the Harbinger Discord Bot
 * Validation via PSK required.
 */
public class DiscordHandler implements HttpHandler
{
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		JSONObject replyObj = new JSONObject();

		// Validate PSK
		JSONObject request = new JSONObject(new String(httpExchange.getRequestBody().readAllBytes()));

		if(!Persist.serverSettings.PSK.validate(request.getString("psk")))
		{
			replyObj.put("result", "DENY");
		}else {
			// Process request here
			replyObj.put("result", "ACCEPT");
			String operation = request.getString("type");
			switch(operation)
			{
				case "update_token":
				{
					replyObj.put("token", "updated");
					Persist.discordSettings.BOT_TOKEN = request.getString("token");
					EventBus.BUS.post(new MemoryAlteredEvent());
					EventBus.BUS.post(new DiscordBotTokenUpdatedEvent());
					break;
				}
			}
		}

		String reply = replyObj.toString();
		byte[] replyBytes = reply.getBytes();
		httpExchange.getResponseHeaders().add("Content-Type", "application/json");
		httpExchange.sendResponseHeaders(200, replyBytes.length);

		DataOutputStream dos = new DataOutputStream(httpExchange.getResponseBody());
		dos.write(replyBytes);
		dos.close();
	}
}
