package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.html.DOM;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.APIRequestEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class APIHandler implements HttpHandler {
	private boolean htmlRender = false;
	public APIHandler(boolean html)
	{
		htmlRender=!html;
	}
	@Override
	public void handle ( final HttpExchange httpExchange ) throws IOException {


		final APIRequestEvent ARE = new APIRequestEvent ( new JSONObject ( new String ( httpExchange.getRequestBody ( ).readAllBytes ( ) , StandardCharsets.UTF_8 ) ) );
		EventBus.BUS.post ( ARE );

		httpExchange.getResponseHeaders ().add ( "Server", "Harbinger/" + Persist.HARBINGER_VERSION );


		if ( ! ARE.isCancelled ( ) ) {
			httpExchange.sendResponseHeaders ( 404 , 0 );
			httpExchange.close ( );
		}
		else {
			if(htmlRender){
				String reply = "";
				HTMLElementBuilder builder = DOM.beginBootstrapDOM ( httpExchange.getRequestURI ().getPath () );
				builder.getChildByTagName ( "html" ).getChildByTagName ( "body" ).addChild ( ARE.HTMLContent );
				reply = builder.build ().generateHTML();
				byte[] bRep = reply.getBytes ( StandardCharsets.UTF_8 );
				httpExchange.getResponseHeaders ().add ( "Content-Type", "text/html" );
				httpExchange.sendResponseHeaders ( ARE.response_status, bRep.length );
				OutputStream os = httpExchange.getResponseBody ();
				os.write ( bRep );
				os.close ();
			}else {

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
}