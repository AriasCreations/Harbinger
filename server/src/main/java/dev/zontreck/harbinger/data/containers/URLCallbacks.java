package dev.zontreck.harbinger.data.containers;

import dev.zontreck.harbinger.data.types.URLCallback;

import java.util.ArrayList;
import java.util.List;

public enum URLCallbacks {
	;
	private static final List<URLCallback> callbacks = new ArrayList<> ( );

	public static void add ( final URLCallback cb ) {
		URLCallbacks.callbacks.add ( cb );
	}

	public static boolean hasCallback ( ) {
		return ( 0 < callbacks.size ( ) );
	}

	public static URLCallback getNext ( ) {
		final URLCallback cb = URLCallbacks.callbacks.get ( 0 );
		URLCallbacks.callbacks.remove ( 0 );

		return cb;
	}
}
