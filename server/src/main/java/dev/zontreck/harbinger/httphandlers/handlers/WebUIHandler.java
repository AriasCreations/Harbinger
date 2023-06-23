package dev.zontreck.harbinger.httphandlers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.html.DOM;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.harbinger.data.Persist;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WebUIHandler implements HttpHandler {
	@Override
	public void handle ( HttpExchange httpExchange ) throws IOException {
		HTMLElementBuilder builder = DOM.beginBootstrapDOM ( "WebUI" );
		var body = builder.getChildByTagName ( "html" ).getChildByTagName ( "body" );
		builder.getChildByTagName ( "html" ).addChild ( "script" ).withText ( "\n" +
				"    \n" +
				"      function submitCommand()\n" +
				"      {\n" +
				"        var cmd = $(\"#cmd\")[0].value;\n" +
				"        var RawArgs = cmd.split(\" \");\n" +
				"        cmd = RawArgs[0];\n" +
				"        var args = RawArgs.slice(1);\n" +
				"\n" +
				"        var postData = {\n" +
				"          type: \"command\",\n" +
				"          psk: $(\"#psk\")[0].value,\n" +
				"          command: cmd,\n" +
				"          args: args\n" +
				"        };\n" +
				"        $.ajax({\n" +
				"          type: \"POST\",\n" +
				"          url: \"/api/html\",\n" +
				"          data: JSON.stringify(postData),\n" +
				"          success: function(dat){\n" +
				"            $(\"#cfooter\").html(\"<br/><br/>\"+dat);\n" +
				"          }\n" +
				"        })\n" +
				"      }\n" +
				"    " );

		body.addClass ( "text-bg-dark" );
		var webUIMain = body.addChild ( "div" );
		webUIMain.addClass ( "border rounded-4 shadow text-bg-secondary bg-gradient p-3 border-info text-auto card" );
		webUIMain.withAttribute ( "style", "position:absolute;left:50%;top:50%;width:75vw" );
		webUIMain.addChild ( "div" ).addClass ( "card-header" ).addChild ( "h4" ).withText ( "Harbinger WebUI" );
		var cardBody = webUIMain.addChild ( "div" ).addClass ( "card-body" );

		cardBody.addChild ( "label" ).addClass ( "form-label" ).withAttribute ( "for" , "psk" ).withText ( "Pre-Shared Key" );
		cardBody.addChild ( "input" ).addClass ( "form-control" ).withAttribute ( "type" , "password" ).withAttribute ( "id" , "psk" ).withAttribute ( "aria-labelledby" , "pskInfo" );
		cardBody.addChild ( "div" ).addClass ( "form-text" ).withAttribute ( "id" , "pskInfo" ).withText ( "Preshared key is the Administrator access code for this command interface" );

		cardBody.addChild ( "br" );
		cardBody.addChild ( "br" );


		cardBody.addChild ( "label" ).addClass ( "form-label" ).withAttribute ( "for" , "cmd" ).withText ( "Command" );
		cardBody.addChild ( "input" ).addClass ( "form-control" ).withAttribute ( "type" , "text" ).withAttribute ( "id" , "cmd" ).withAttribute ( "aria-labelledby" , "cmdInfo" );
		cardBody.addChild ( "div" ).addClass ( "form-text" ).withAttribute ( "id" , "cmdInfo" ).withText ( "Command to execute" );

		cardBody.addChild ( "br" );

		cardBody.addChild ( "button" ).addClass ( "btn btn-danger" ).withAttribute ( "onclick" , "submitCommand()" ).withText ( "Submit Admin Command" );

		var footer = webUIMain.addChild ( "div" ).addClass ( "card-footer" );
		footer.withAttribute ( "id" , "cfooter" );


		httpExchange.getResponseHeaders ( ).add ( "Server" , "Harbinger/" + Persist.HARBINGER_VERSION );
		httpExchange.getResponseHeaders ( ).add ( "Content-Type" , "text/html" );

		byte[] bRep = builder.build ( ).generateHTML ( ).getBytes ( StandardCharsets.UTF_8 );
		httpExchange.sendResponseHeaders ( 200 , bRep.length );
		httpExchange.getResponseBody ( ).write ( bRep );
		httpExchange.getResponseBody ( ).close ( );

	}
}
