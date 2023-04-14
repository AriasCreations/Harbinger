package dev.zontreck.harbinger.handlers;

import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.APIRequestEvent;
import org.json.JSONObject;

import java.util.UUID;

public class ModifyProduct
{
	public static void onAPIRequest(APIRequestEvent event)
	{
		if(event.request_object.getString("type").equals("modify_product"))
		{
			switch(event.request_object.getString("action"))
			{
				case "update":
				{
					if(Persist.products.hasProduct(UUID.fromString(event.request_object.getString("product"))))
					{
					}
					break;
				}
				case "make":
				{
					UUID ID = UUID.randomUUID();

					break;
				}
			}
		}
	}
}
