package dev.zontreck.harbinger.events;

import dev.zontreck.ariaslib.events.Event;
import org.json.JSONObject;

import java.util.List;

public class HarbingerCommandEvent extends Event
{
	public String command;
	public List<String> arguments;

	public JSONObject response;
	@Override
	public boolean isCancellable ( ) {
		return true;
	}

	public HarbingerCommandEvent(String command)
	{
		this.command=command;
	}

	@Override
	public String toString()
	{
		return response.toString(4);
	}
}
