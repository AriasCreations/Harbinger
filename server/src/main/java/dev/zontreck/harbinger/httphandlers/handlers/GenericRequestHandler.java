package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.GenericRequestEvent;
import dev.zontreck.harbinger.httphandlers.HTTPEvents;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericRequestHandler implements HttpHandler {

	@Override
	public void handle ( final HttpExchange httpExchange ) throws IOException {
		final GenericRequestEvent GRE = new GenericRequestEvent ( httpExchange.getRequestURI ( ).getPath ( ) , httpExchange.getRequestMethod ( ) , httpExchange.getRequestBody ( ).readAllBytes ( ) );

		httpExchange.getResponseHeaders ().add ( "Server", "Harbinger/" + Persist.HARBINGER_VERSION );

		if ( ! EventBus.BUS.post ( GRE ) ) {
			GRE.responseCode = 404;
			GRE.contentType = "text/html";
			GRE.responseText = "Not Found";


			HTTPEvents.LOGGER.info ( "[ERROR] Path: " + GRE.path + " [ " + GRE.responseCode + "/" + new String(GRE.body) + " ]" );

			String headers_str = "";
			for (
					Map.Entry<String, List<String>> header :
					httpExchange.getRequestHeaders ().entrySet ()
			) {
				List<String> append = new ArrayList<> (  );
				for (
						String e :
						header.getValue ( )
				) {
					append.add ( header.getKey ()+": "+e );
				}
				headers_str+= "\n" + String.join ( "\n", append );
			}

			HTTPEvents.LOGGER.debug ( "\nHEADERS: " + headers_str );
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
