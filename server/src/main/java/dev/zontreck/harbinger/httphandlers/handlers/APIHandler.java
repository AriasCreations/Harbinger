package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.APIRequestEvent;
import dev.zontreck.harbinger.httphandlers.HTTPEvents;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class APIHandler implements HttpHandler {
	private boolean htmlRender = false;

	public APIHandler ( boolean json ) {
		htmlRender = ! json;
	}

	@Override
	public void handle ( final HttpExchange httpExchange ) throws IOException {


		final APIRequestEvent ARE = new APIRequestEvent ( new JSONObject ( new String ( httpExchange.getRequestBody ( ).readAllBytes ( ) , StandardCharsets.UTF_8 ) ) );

		ARE.admin = Persist.serverSettings.PSK.validate ( ARE.request_object.getString ( "psk" ) );

		httpExchange.getResponseHeaders ( ).add ( "Server" , "Harbinger/" + Persist.HARBINGER_VERSION );

		HTTPEvents.LOGGER.info ( "API Request [" + ( htmlRender ? "/api/html" : "/api/json" ) + " : " + ( ARE.admin ? "admin" : "anonymous" ) + "] " + ARE.request_object.getString ( "type" ) );
		EventBus.BUS.post ( ARE );

		if ( ! ARE.isCancelled ( ) ) {
			httpExchange.sendResponseHeaders ( 404 , 0 );
			httpExchange.close ( );
		}
		else {
			if ( htmlRender ) {
				String reply = ARE.HTMLContent.build ( ).generateHTML ( );
				/*
				HTMLElementBuilder builder = DOM.beginBootstrapDOM ( httpExchange.getRequestURI ( ).getPath ( ) );
				builder.getChildByTagName ( "html" ).getChildByTagName ( "body" ).addChild ( ARE.HTMLContent );
				reply = builder.build ( ).generateHTML ( );
				 */

				byte[] bRep = reply.getBytes ( StandardCharsets.UTF_8 );
				httpExchange.getResponseHeaders ( ).add ( "Content-Type" , "text/html" );
				httpExchange.sendResponseHeaders ( ARE.response_status , bRep.length );
				OutputStream os = httpExchange.getResponseBody ( );
				os.write ( bRep );
				os.close ( );

			}
			else {

				String reply = ARE.response_object.toString ( );
				byte[] bRep = reply.getBytes ( StandardCharsets.UTF_8 );
				httpExchange.getResponseHeaders ( ).add ( "Content-Type" , "application/json" );
				httpExchange.sendResponseHeaders ( ARE.response_status , bRep.length );
				OutputStream os = httpExchange.getResponseBody ( );
				os.write ( bRep );
				os.close ( );
			}
		}
	}
}