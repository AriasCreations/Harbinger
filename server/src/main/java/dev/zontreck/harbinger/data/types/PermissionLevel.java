package dev.zontreck.harbinger.data.types;

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
}
