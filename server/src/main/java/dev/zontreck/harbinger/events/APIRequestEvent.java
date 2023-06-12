package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;
import org.json.JSONObject;

/**
 * This event is fired when a request comes in on the /api endpoint
 * <p>
 * This event is Cancelable and when canceled, indicates a handler for the endpoint was located and executed.
 * If canceled, the response will be sent to the client. If not canceled a 404 status will be returned.
 */
public class APIRequestEvent extends Event {

	public JSONObject request_object;
	public JSONObject response_object;
	public int response_status = 200;

	public APIRequestEvent(JSONObject req) {
		request_object = req;
	}


	@Override
	public boolean isCancellable() {
		return true;
	}
}
