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
		builder.addClass ( "position-relative border rounded-4 shadow text-bg-dark bg-gradient p-3 border-danger text-auto card" );
		var card_header = builder.addChild ( "div" ).addClass ( "card-header" );
		card_header.addChild ( "h4" ).withText ( title );
		var card_body = builder.addChild ( "div" ).addClass ( "card-body" );
		var card_body_child = card_body.addChild("div");
		card_body_child.withText ( " " ).addChild ( body );

		var card_footer = builder.addChild ( "div" ).addClass ( "card-footer" );
		var card_json = card_footer.addChild ( "div" ).addClass ( "card text-bg-danger rounded-4 shadow bg-gradient" );
		var card_json_title = card_json.addChild ( "div" ).addClass ( "card-header" ).withText ( "Raw Response" );
		var json_body = card_json.addChild ( "div" ).addClass ( "card-body" );
		json_body.withText ( json.toString ( 4 ) );


		return builder;
	}
}
