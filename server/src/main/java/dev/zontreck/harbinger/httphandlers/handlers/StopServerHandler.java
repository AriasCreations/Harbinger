package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.data.Persist;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StopServerHandler implements HttpHandler {
	@Override
	public void handle ( final HttpExchange httpExchange ) throws IOException {

		final InputStream IS = httpExchange.getRequestBody ( );
		final String reqJson = new String ( IS.readAllBytes ( ) , StandardCharsets.UTF_8 );

		final JSONObject obj = new JSONObject ( reqJson );
		String reply = "Stop;;";
		if ( Persist.serverSettings.PSK.validate ( obj.getString ( "psk" ) ) ) {
			reply += "OK";
			final CommandEvent ce = new CommandEvent ( "stop" );
			EventBus.BUS.post ( ce );
		}
		else reply += "FAIL";

		final byte[] bRep = reply.getBytes ( StandardCharsets.UTF_8 );
		httpExchange.getResponseHeaders ( ).add ( "Content-Type" , "text/plain" );
		httpExchange.sendResponseHeaders ( 200 , bRep.length );
		final OutputStream os = httpExchange.getResponseBody ( );
		os.write ( bRep );
		os.close ( );
	}
}
