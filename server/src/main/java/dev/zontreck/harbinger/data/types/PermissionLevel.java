package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import dev.zontreck.harbinger.commands.Commands;

public enum PermissionLevel {
	NONE ( 0 ),
	Customer ( 1 ),
	Support ( 2 ),
	Mod ( 4 ),
	Developer ( 8 ),
	Admin ( 16 );

	private final int flag;

	PermissionLevel ( final int value ) {
		this.flag = value;
	}

	public static PermissionLevel of ( final int flags ) {
		final PermissionLevel p = NONE;

		for ( final PermissionLevel lvl :
				PermissionLevel.values ( ) ) {
			if ( lvl.flag == flags ) {
				return lvl;
			}
		}

		return p;
	}

	public int getFlag ( ) {
		return this.flag;
	}



	public static HTMLElementBuilder render ( ) {
		HTMLElementBuilder root = new HTMLElementBuilder ( "table" );
		root.addClass ( "table-primary" ).addClass ( "text-center" ).addClass ( "table-bordered" ).addClass ( "border-black" ).addClass ( "table" ).addClass ( "rounded-4" ).addClass ( "shadow" ).addClass ( "table-striped" );
		var tableHead = root.addChild ( "thead" );
		var row = tableHead.addChild ( "tr" );
		row.addChild ( "th" ).withAttribute ( "scope" , "col" ).withText ( "Title" );
		row.addChild ( "th" ).withAttribute ( "scope" , "col" ).withText ( "Level" );

		var tableBody = root.addChild ( "tbody" );
		for (
				PermissionLevel cmd :
				values ( )
		) {
			var entry = tableBody.addChild ( "tr" );

			entry.addChild ( "td" ).withText ( cmd.name () );
			entry.addChild ( "td" ).withText ( String.valueOf ( cmd.flag ) );

		}

		return root;
	}
}
