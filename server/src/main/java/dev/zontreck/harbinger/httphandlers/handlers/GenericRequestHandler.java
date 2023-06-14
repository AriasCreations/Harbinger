package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.events.GenericRequestEvent;
import dev.zontreck.harbinger.httphandlers.HTTPEvents;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class GenericRequestHandler implements HttpHandler {

	@Override
	public void handle ( final HttpExchange httpExchange ) throws IOException {
		final GenericRequestEvent GRE = new GenericRequestEvent ( httpExchange.getRequestURI ( ).getPath ( ) , httpExchange.getRequestMethod ( ) , httpExchange.getRequestBody ( ).readAllBytes ( ) );
		if ( ! EventBus.BUS.post ( GRE ) ) {
			GRE.responseText = "Not Found";
			GRE.responseCode = 404;
			GRE.contentType = "text/plain";
			GRE.responseText = "";


			HTTPEvents.LOGGER.info ( "[ERROR] Path: " + GRE.path + " [ " + GRE.responseCode + "/" + GRE.body + " ]" );
		}
		else {

			HTTPEvents.LOGGER.info ( "[ACCESS] Path: " + GRE.path + " [ " + GRE.responseCode + " ]" );
		}


		final byte[] response;
		if ( GRE.responseIsBinary ) {
			response = GRE.response;
		}
		else {
			response = GRE.responseText.getBytes ( StandardCharsets.UTF_8 );
		}

		httpExchange.getResponseHeaders ( ).add ( "Content-Type" , GRE.contentType );
		httpExchange.sendResponseHeaders ( GRE.responseCode , response.length );

		OutputStream os = httpExchange.getResponseBody ( );
		os.write ( GRE.responseText.getBytes ( ) );
		os.close ( );
	}
}
