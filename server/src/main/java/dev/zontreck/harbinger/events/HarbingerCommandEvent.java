package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;
import dev.zontreck.ariaslib.html.HTMLElementBuilder;
import org.json.JSONObject;

import java.util.List;

/**
 * DANGER: Admin access is implied when this event is dispatched. The only source of this is from /api and the PSK is validated before this event is permitted to be fired.
 *
 * Use extreme caution if implementing a usage of this event elsewhere
 */
public class HarbingerCommandEvent extends Event {
	public String command;
	public List<String> arguments;

	public JSONObject response = new JSONObject (  );
	public HTMLElementBuilder html;

	@Override
	public boolean isCancellable ( ) {
		return true;
	}

	public HarbingerCommandEvent ( String command ) {
		this.command = command;
	}

	@Override
	public String toString ( ) {
		return response.toString ( 4 );
	}
}
