package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.events.GenericRequestEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GenericRequestHandler implements HttpHandler {

	@Override
	public void handle ( final HttpExchange httpExchange ) throws IOException {
		final GenericRequestEvent GRE = new GenericRequestEvent ( httpExchange.getRequestURI ( ).getPath ( ) , httpExchange.getRequestMethod ( ) , httpExchange.getRequestBody ( ).readAllBytes ( ) );
		EventBus.BUS.post ( GRE );

		final byte[] response;
		if ( GRE.responseIsBinary ) {
			response = GRE.response;
		}
		else {
			response = GRE.responseText.getBytes ( StandardCharsets.UTF_8 );
		}

		httpExchange.getResponseHeaders ( ).add ( "Content-Type" , GRE.contentType );
		httpExchange.sendResponseHeaders ( GRE.responseCode , response.length );
	}
}
