package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.types.DiscordEmbed;
import dev.zontreck.harbinger.data.types.DiscordEmbedColor;
import dev.zontreck.harbinger.data.types.DiscordWebhookMessage;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.events.discord.DiscordBotTokenUpdatedEvent;
import dev.zontreck.harbinger.exceptions.DiscordEmbedLimitsException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Special Endpoint for Discord
 * <p>
 * This interacts with the Harbinger Discord Bot
 * Validation via PSK required.
 */
public class DiscordHandler implements HttpHandler {
	@Override
	public void handle ( final HttpExchange httpExchange ) throws IOException {

		final JSONObject replyObj = new JSONObject ( );

		// Validate PSK
		final JSONObject request = new JSONObject ( new String ( httpExchange.getRequestBody ( ).readAllBytes ( ) , StandardCharsets.UTF_8 ) );

		if ( ! Persist.serverSettings.PSK.validate ( request.getString ( "psk" ) ) ) {
			replyObj.put ( "result" , "DENY" );
		}
		else {
			// Process request here
			replyObj.put ( "result" , "ACCEPT" );
			final String operation = request.getString ( "type" );
			switch ( operation ) {
				case "update_token": {
					replyObj.put ( "token" , "updated" );
					Persist.discordSettings.BOT_TOKEN = request.getString ( "token" );
					EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
					EventBus.BUS.post ( new DiscordBotTokenUpdatedEvent ( ) );
					break;
				}
				case "set_webhook": {
					// Simply set the webhook URL and flush the settings
					Persist.discordSettings.WEBHOOKS.put ( request.getString ( "nick" ) , request.getString ( "url" ) );
					EventBus.BUS.post ( new MemoryAlteredEvent ( ) );


					break;
				}
				case "del_webhook": {
					// Delete the webhook and flush
					Persist.discordSettings.WEBHOOKS.remove ( request.getString ( "nick" ) );
					EventBus.BUS.post ( new MemoryAlteredEvent ( ) );
					break;
				}
				case "send_webhook": {
					// Send a stylized message
					final DiscordEmbed emb = new DiscordEmbed ( request.getString ( "title" ) , request.getString ( "desc" ) );
					emb.color = DiscordEmbedColor.valueOf ( request.getString ( "color" ) );
					final DiscordWebhookMessage msg = new DiscordWebhookMessage ( );
					msg.embeds = new ArrayList<> ( );
					msg.embeds.add ( emb );
					String embMsg = null;
					try {
						embMsg = msg.serialize ( ).toString ( );
					} catch (
							final
							DiscordEmbedLimitsException e ) {
						throw new RuntimeException ( e );
					}

					// Get the URL of the target hook
					final String hookURL = Persist.discordSettings.WEBHOOKS.get ( request.getString ( "nick" ) );
					final HttpClient cli = HttpClient.newHttpClient ( );
					try {
						final HttpRequest req = HttpRequest.newBuilder ( new URI ( hookURL ) ).POST ( HttpRequest.BodyPublishers.ofString ( embMsg ) ).setHeader ( "Content-Type" , "application/json" ).build ( );
						cli.send ( req , HttpResponse.BodyHandlers.discarding ( ) );
					} catch ( final URISyntaxException e ) {
						throw new RuntimeException ( e );
					} catch ( final InterruptedException e ) {
						throw new RuntimeException ( e );
					}

					break;
				}
			}
		}

		final String reply = replyObj.toString ( );
		final byte[] replyBytes = reply.getBytes ( StandardCharsets.UTF_8 );
		httpExchange.getResponseHeaders ( ).add ( "Content-Type" , "application/json" );
		httpExchange.sendResponseHeaders ( 200 , replyBytes.length );

		final DataOutputStream dos = new DataOutputStream ( httpExchange.getResponseBody ( ) );
		dos.write ( replyBytes );
		dos.close ( );
	}
}
