package dev.zontreck.harbinger.commands;

import dev.zontreck.ariaslib.html.Bootstrap;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.ariaslib.html.bootstrap.Color;

public class CommandMessage
{
	public static HTMLElementBuilder buildMessage( Color color, String message )
	{
		HTMLElementBuilder builder = new HTMLElementBuilder ( "div" ).addClass ( "p-3" ).addClass ( "rounded-3" );
		var c = new Bootstrap.Colors ().withColor ( color ).withPrefix ( "text-bg" );
		c.apply ( builder );

		return builder.withText ( message );
	}
}
