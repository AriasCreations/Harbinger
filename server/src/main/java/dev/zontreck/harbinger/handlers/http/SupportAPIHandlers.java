package dev.zontreck.harbinger.handlers.http;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.PermissionLevel;
import dev.zontreck.harbinger.data.types.Person;
import dev.zontreck.harbinger.events.APIRequestEvent;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import org.json.JSONObject;

import java.util.UUID;

public class SupportAPIHandlers
{
	@Subscribe
	public static void onSupport(APIRequestEvent event)
	{
		if(event.request_object.getString("type").equals("support"))
		{
			JSONObject response = new JSONObject();
			// Validate PSK
			boolean auth = false; // This is to control if tasks that require admin should be executed or not
			if(Persist.serverSettings.PSK.validate(event.request_object.getString("psk")))
			{
				auth = true;
			}
			event.setCancelled(true);
			UUID ID = UUID.fromString(event.request_object.getString("id"));
			switch(event.request_object.getString("sub_command"))
			{
				case "delete":
				{
					if(!auth)
					{
						response.put("result", "Admin access required");
						break;
					}
					// Deletes the support rep if possible
					if(SupportReps.hasID(ID))
					{
						SupportReps.remove(SupportReps.get(ID));
						response.put("result", "Support representative deleted.");
						EventBus.BUS.post(new MemoryAlteredEvent());
					}else response.put("result", "No such support rep. No changes have been made.");
					break;
				}
				case "get":
				{
					// Gets the support level
					if(SupportReps.hasID(ID))
					{
						Person p = SupportReps.get(ID);
						response.put("result", "Found");
						response.put("level", p.Permissions.getFlag());
						response.put("level_id", p.Permissions.name());
						response.put("id", ID.toString());
					}else {
						response.put("result", "No user");
						response.put("level", 0);
						response.put("level_id", "None");
						response.put("id", ID.toString());
					}
					break;
				}
				case "add":
				{
					// Set a new support rep
					if(!auth)
					{
						response.put("result", "Admin access required");
						break;
					}
					Person rep = new Person(ID, event.request_object.getString("name"), PermissionLevel.valueOf(event.request_object.getString("level")));
					if(SupportReps.hasID(ID))
						SupportReps.remove(SupportReps.get(ID));

					SupportReps.add(rep);
					response.put("result", "Representative added or updated successfully");

					EventBus.BUS.post(new MemoryAlteredEvent());
					break;
				}
			}

			event.response_object=response;
		}
	}
}
