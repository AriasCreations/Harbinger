package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.html.Bootstrap;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.ariaslib.html.bootstrap.Color;
import org.json.JSONObject;

public class CommandHTMLPage
{
	public static HTMLElementBuilder makePage( String title, HTMLElementBuilder body, JSONObject json )
	{

		HTMLElementBuilder builder = new HTMLElementBuilder ("div");
		builder.addClass ( "position-absolute" );
		Bootstrap.Border.make ().withColor ( Bootstrap.Colors.make ().withColor ( Color.Danger ) ).apply ( builder );
		builder.addClass ( "rounded-4" ).addClass ( "text-bg-dark" ).addClass ( "bg-gradient" ).addClass ( "p-3" ).addClass ( "w-50" ).addClass ( "h-auto" ).addClass ( "top-50 start-50" ).addClass ( "translate-middle card" );
		var card_header = builder.addChild ( "div" ).addClass ( "card-header" );
		card_header.addChild ( "h4" ).withText ( title );
		var card_body = builder.addChild ( "div" ).addClass ( "card-body" );
		card_body.addChild ( body );

		var card_footer = builder.addChild ( "div" ).addClass ( "card-footer" );
		var card_json = card_footer.addChild ( "div" ).addClass ( "card text-bg-danger rounded-4 shadow bg-gradient" );
		var card_json_title = card_json.addChild ( "div" ).addClass ( "card-header" ).withText ( "Raw Response" );
		var json_body = card_json.addChild ( "div" ).addClass ( "card-body" );
		json_body.withText ( json.toString () );


		return builder;
	}
}
