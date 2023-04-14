package dev.zontreck.harbinger.data.containers;

import dev.zontreck.harbinger.data.types.URLCallback;

import java.util.ArrayList;
import java.util.List;

public class URLCallbacks {
	private static final List<URLCallback> callbacks = new ArrayList<>();

	public static void add(URLCallback cb)
	{
		callbacks.add(cb);
	}

	public static boolean hasCallback()
	{
		return (callbacks.size()>0);
	}

	public static URLCallback getNext()
	{
		URLCallback cb = callbacks.get(0);
		callbacks.remove(0);

		return cb;
	}
}
