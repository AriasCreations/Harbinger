package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.events.APIRequestEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class APIHandler implements HttpHandler {
	@Override
	public void handle ( final HttpExchange httpExchange ) throws IOException {


		final APIRequestEvent ARE = new APIRequestEvent ( new JSONObject ( new String ( httpExchange.getRequestBody ( ).readAllBytes ( ) , StandardCharsets.UTF_8 ) ) );
		EventBus.BUS.post ( ARE );

		if ( ! ARE.isCancelled ( ) ) {
			httpExchange.sendResponseHeaders ( 404 , 0 );
			httpExchange.close ( );
		}
		else {
			final String reply = ARE.response_object.toString ( );
			final byte[] bRep = reply.getBytes ( StandardCharsets.UTF_8 );
			httpExchange.getResponseHeaders ( ).add ( "Content-Type" , "application/json" );
			httpExchange.sendResponseHeaders ( ARE.response_status , bRep.length );
			final OutputStream os = httpExchange.getResponseBody ( );
			os.write ( bRep );
			os.close ( );
		}
	}
}